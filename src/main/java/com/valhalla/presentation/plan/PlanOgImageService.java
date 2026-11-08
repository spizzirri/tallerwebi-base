package com.valhalla.presentation.plan;

import com.valhalla.domain.plan.Plan;
import jakarta.servlet.ServletContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * [OG-JPG] Imagen de preview del plan para los scrapers: SIEMPRE un JPG 1200x630 de menos de
 * 250 KB, servido desde nuestro propio dominio (los bots no leen SVG y hay que evitar el
 * hotlink). El preview es la MISMA portada que muestra la card del plan —la elegida por hash
 * del id entre las genericas de /images/plans— rasterizada aca; si la portada no esta, la
 * imagen de marca. Nunca se dibuja texto encima: solo se convierte y redimensiona la imagen
 * que ya existe, para que compartir y feed muestren siempre la misma cara del plan.
 *
 * <p>El resultado se cachea en memoria por portada: un plan que pasa a otra cover calcula la
 * entrada nueva y la vieja queda sin uso; no hace falta invalidar nada.
 */
@Service
public class PlanOgImageService {

  public static final int WIDTH_PX = 1200;
  public static final int HEIGHT_PX = 630;

  // WhatsApp pide content-length bajo 300 KB; se corta en 250 KB para tener margen.
  private static final int MAX_BYTES = 250_000;
  private static final float[] JPEG_TIERS = { 0.85f, 0.7f, 0.55f, 0.4f };

  private final ServletContext servletContext;
  private final Map<String, Optional<byte[]>> cache = new ConcurrentHashMap<>();

  @Autowired
  public PlanOgImageService(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  /**
   * JPG de preview del plan, o {@link Optional#empty()} si no hay ni portada generica ni imagen
   * de marca (el endpoint responde 404 en ese caso, que en la practica no ocurre).
   */
  public Optional<byte[]> ogImageFor(Plan plan) {
    String cover = PlanFeedCards.coverFor(plan.getId());
    return cache.computeIfAbsent(cover, this::fromCover);
  }

  private Optional<byte[]> fromCover(String coverPath) {
    BufferedImage cover = rasterizeCover(coverPath);
    if (cover == null) {
      cover = readBrandImage();
    }
    if (cover == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(encodeJpeg(cropToCanvas(cover)));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  /**
   * Rasteriza la portada SVG del plan. El SVG fuente trae solo {@code viewBox} (1200x640): sin
   * {@code width}/{@code height} propios Batik rasteriza al default de 400x400 y las hints
   * KEY_WIDTH/KEY_HEIGHT no lo pisan, así que se los declara en el root junto con
   * preserveAspectRatio=none (1.6% de squeeze vertical, la misma imagen, sin texto).
   */
  private BufferedImage rasterizeCover(String coverPath) {
    // /images/** se sirve desde /resources/images/** (ver BaseWebConfig.addResourceHandlers):
    // la ruta fisica dentro del webapp lleva el prefijo /resources.
    try (InputStream svg = servletContext.getResourceAsStream("/resources" + coverPath)) {
      if (svg == null) {
        return null;
      }
      String source = new String(svg.readAllBytes(), StandardCharsets.UTF_8)
        .replaceFirst(
          "<svg ",
          "<svg width=\"" +
          WIDTH_PX +
          "\" height=\"" +
          HEIGHT_PX +
          "\" preserveAspectRatio=\"none\" "
        );
      PNGTranscoder transcoder = new PNGTranscoder();
      ByteArrayOutputStream png = new ByteArrayOutputStream();
      transcoder.transcode(
        new TranscoderInput(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))),
        new TranscoderOutput(png)
      );
      return ImageIO.read(new ByteArrayInputStream(png.toByteArray()));
    } catch (IOException | TranscoderException exception) {
      throw new IllegalStateException("No se pudo convertir la portada " + coverPath, exception);
    }
  }

  private BufferedImage readBrandImage() {
    try (InputStream brand = servletContext.getResourceAsStream("/resources/images/og-image.png")) {
      return brand == null ? null : ImageIO.read(brand);
    } catch (IOException exception) {
      return null;
    }
  }

  /** Relleno centro (aspect fill) sobre un lienzo RGB 1200x630; aplana el alfa sobre blanco. */
  private BufferedImage cropToCanvas(BufferedImage source) {
    double scale = Math.max(
      (double) WIDTH_PX / source.getWidth(),
      (double) HEIGHT_PX / source.getHeight()
    );
    int scaledWidth = (int) Math.ceil(source.getWidth() * scale);
    int scaledHeight = (int) Math.ceil(source.getHeight() * scale);
    BufferedImage canvas = new BufferedImage(WIDTH_PX, HEIGHT_PX, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = canvas.createGraphics();
    graphics.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR
    );
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.drawImage(
      source,
      (WIDTH_PX - scaledWidth) / 2,
      (HEIGHT_PX - scaledHeight) / 2,
      scaledWidth,
      scaledHeight,
      null
    );
    graphics.dispose();
    return canvas;
  }

  /** Codifica en JPEG bajando la calidad por tramos hasta quedar bajo el tope de 250 KB. */
  private byte[] encodeJpeg(BufferedImage image) throws IOException {
    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
    try {
      ImageWriteParam params = writer.getDefaultWriteParam();
      params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      for (float quality : JPEG_TIERS) {
        out.reset();
        params.setCompressionQuality(quality);
        try (MemoryCacheImageOutputStream images = new MemoryCacheImageOutputStream(out)) {
          writer.setOutput(images);
          writer.write(null, new IIOImage(image, null, null), params);
        }
        if (out.size() <= MAX_BYTES) {
          return out.toByteArray();
        }
      }
      return out.toByteArray();
    } finally {
      writer.dispose();
    }
  }
}
