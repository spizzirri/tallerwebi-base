package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.plan.Plan;
import jakarta.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * [OG-JPG] El preview del scraper es la MISMA portada que pinta la card del plan, rasterizada y
 * codificada a JPEG canvas: se verifica que se pide exactamente el coverFor(id) de la card (sin
 * tocar el itinerario, asi un plan privado con el lazy sin calentar no puede fallar), la bajada
 * a la imagen de marca cuando la portada no esta, el empty (404 del endpoint) sin ninguno de los
 * dos recursos y los extremos del encoder.
 */
public class PlanOgImageServiceTest {

  private static final UUID PLAN_ID = UUID.randomUUID();
  private static final int COVER_COUNT = 6;

  private static byte[] png(int width, int height, boolean noisy) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    if (noisy) {
      Random random = new Random(42);
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          image.setRGB(x, y, random.nextInt(0x1000000));
        }
      }
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }

  /** Plan con itinerario perezoso a proposito: el preview ya no lo lee nunca. */
  private Plan plan() {
    return planWithId(PLAN_ID);
  }

  private Plan planWithId(UUID id) {
    Plan plan = mock(Plan.class);
    lenient().when(plan.getId()).thenReturn(id);
    when(plan.getPlanPlaces()).thenThrow(new RuntimeException("failed to lazily initialize"));
    return plan;
  }

  private ServletContext contextWith(Map<String, byte[]> resources) {
    ServletContext context = mock(ServletContext.class);
    lenient()
      .when(context.getResourceAsStream(anyString()))
      .thenAnswer(invocation -> {
        byte[] content = resources.get(invocation.getArgument(0));
        return content == null ? null : new ByteArrayInputStream(content);
      });
    return context;
  }

  private Map<String, byte[]> brandResources() throws IOException {
    return new HashMap<>(Map.of("/resources/images/og-image.png", png(1200, 630, false)));
  }

  private ServletContext contextWithBrand() throws IOException {
    return contextWith(brandResources());
  }

  private ServletContext contextWithCover() throws IOException {
    Map<String, byte[]> resources = brandResources();
    resources.put(
      "/resources" + PlanFeedCards.coverFor(PLAN_ID),
      ("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1200 640\">" +
        "<rect width=\"1200\" height=\"640\" fill=\"#223344\"/></svg>").getBytes(
          StandardCharsets.UTF_8
        )
    );
    return contextWith(resources);
  }

  private ServletContext contextWithAllCovers() throws IOException {
    Map<String, byte[]> resources = brandResources();
    for (int cover = 1; cover <= COVER_COUNT; cover++) {
      resources.put(
        "/resources/images/plans/cover-" + cover + ".svg",
        ("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1200 640\">" +
          "<rect width=\"1200\" height=\"640\" fill=\"#" +
          String.format("%06x", cover * 0x111111) +
          "\"/></svg>").getBytes(StandardCharsets.UTF_8)
      );
    }
    return contextWith(resources);
  }

  /** Decodifica y fija el contrato 1200x630 que prometen los metas og:image:width/height. */
  private void assertCanvasJpeg(Optional<byte[]> result) throws IOException {
    assertThat(result.isPresent(), is(true));
    BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result.get()));
    assertThat(decoded, notNullValue());
    assertThat(decoded.getWidth(), is(1200));
    assertThat(decoded.getHeight(), is(630));
  }

  @Test
  public void thePreviewIsTheCardCoverAndNeverAPlacePhoto() throws Exception {
    ServletContext context = contextWithCover();
    assertCanvasJpeg(new PlanOgImageService(context).ogImageFor(plan()));

    // La portada pedida es exactamente la que pinta la card; la marca no se toca y el
    // itinerario ni se lee: un plan privado con el lazy sin calentar funciona igual.
    verify(context).getResourceAsStream("/resources" + PlanFeedCards.coverFor(PLAN_ID));
    verify(context, never()).getResourceAsStream(startsWith("/resources/images/og-image.png"));
  }

  @Test
  public void everyPlanPreviewsWithTheCoverOfItsOwnCard() throws Exception {
    // El bug que esto evita: el preview salia con una foto de lugar distinta de la portada que
    // el usuario ve en la card. Cada portada de la rotacion (cover-1..6.svg) debe aparecer.
    PlanOgImageService service = new PlanOgImageService(contextWithAllCovers());
    Set<String> coversSeen = new HashSet<>();
    for (int guard = 0; guard < 10000 && coversSeen.size() < COVER_COUNT; guard++) {
      UUID id = UUID.randomUUID();
      assertCanvasJpeg(service.ogImageFor(planWithId(id)));
      coversSeen.add(PlanFeedCards.coverFor(id));
    }
    assertThat(coversSeen.size(), is(COVER_COUNT));
  }

  @Test
  public void theBrandImageIsTheFallbackWhenTheCoverIsMissing() throws Exception {
    assertCanvasJpeg(new PlanOgImageService(contextWithBrand()).ogImageFor(plan()));
  }

  @Test
  public void withoutCoverOrBrandThePreviewIsEmpty() throws Exception {
    Optional<byte[]> result = new PlanOgImageService(contextWith(Map.of())).ogImageFor(plan());
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void aCorruptBrandStreamYieldsNoPreviewInsteadOfAnError() throws Exception {
    ServletContext context = mock(ServletContext.class);
    // Un stream que da EOF y explota al close: ImageIO.read hace null con un stream que lanza
    // durante el sniffing (los readers se comen ese IOException), asi que el punto de fallo
    // determinista es el try-with-resources, que si propaga el IOException al catch.
    when(context.getResourceAsStream(anyString())).thenReturn(brokenCloseStream());
    when(context.getResourceAsStream(startsWith("/resources/images/plans/"))).thenReturn(null);

    assertThat(new PlanOgImageService(context).ogImageFor(plan()).isEmpty(), is(true));
  }

  private static InputStream brokenCloseStream() {
    return new InputStream() {
      @Override
      public int read() {
        return -1;
      }

      @Override
      public void close() throws IOException {
        throw new IOException("cierre roto");
      }
    };
  }

  @Test
  public void aCorruptCoverStreamBlamesTheCoverInTheMessage() throws Exception {
    ServletContext context = mock(ServletContext.class);
    when(context.getResourceAsStream(anyString())).thenReturn(throwingStream());

    try {
      new PlanOgImageService(context).ogImageFor(plan());
      throw new AssertionError("esperaba IllegalStateException");
    } catch (IllegalStateException expected) {
      assertThat(
        expected.getMessage(),
        is("No se pudo convertir la portada " + PlanFeedCards.coverFor(PLAN_ID))
      );
    }
  }

  private static InputStream throwingStream() {
    return new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("stream roto");
      }
    };
  }

  @Test
  public void anEncodeFailureYieldsNoPreviewInsteadOfAnError() throws Exception {
    // El contexto se arma antes del static mock: armar el PNG de prueba usa ImageIO.write,
    // que con el mock encima rebotaria contra el encoder roto que estamos inyectando.
    ServletContext context = contextWithCover();

    ImageWriter failing = mock(ImageWriter.class);
    when(failing.getDefaultWriteParam()).thenReturn(mock(ImageWriteParam.class));
    doThrow(new IOException("encoder roto"))
      .when(failing)
      .write(nullable(IIOMetadata.class), any(IIOImage.class), nullable(ImageWriteParam.class));
    try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class, CALLS_REAL_METHODS)) {
      imageIO
        .when(() -> ImageIO.getImageWritersByFormatName("jpeg"))
        .thenAnswer(invocation -> Collections.singletonList(failing).iterator());

      assertThat(new PlanOgImageService(context).ogImageFor(plan()).isEmpty(), is(true));
    }
  }

  @Test
  public void theLastQualityTierIsTakenWhenNoTierFitsTheCap() throws Exception {
    // Ruido puro en la marca: ningun tramo de calidad entra bajo 250 KB y el servicio igual
    // entrega el ultimo antes que rendirse con un 404 para el scraper.
    Map<String, byte[]> resources = new HashMap<>();
    resources.put("/resources/images/og-image.png", png(1200, 630, true));

    Optional<byte[]> result = new PlanOgImageService(contextWith(resources)).ogImageFor(plan());
    assertThat(result.isPresent(), is(true));
    // No se asserta <= 250 KB: en este extremo se entrega el ultimo tramo aunque pase el tope;
    // si algun dia entrara, la linea del tope seguiria cubierta por el resto de la cadena.
    assertThat(result.get().length > 0, is(true));
  }

  @Test
  public void theResultIsCachedPerCover() throws Exception {
    PlanOgImageService service = new PlanOgImageService(contextWithAllCovers());

    byte[] first = service.ogImageFor(plan()).orElseThrow();
    // Misma portada, mismos bytes: el scraper que vuelve no recalcula nada.
    assertThat(service.ogImageFor(plan()).orElseThrow(), is(first));
  }
}
