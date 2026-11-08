package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * [OG-JPG] El contrato HTTP del endpoint para los scrapers: JPEG publico cacheable cuando el
 * servicio produce la imagen y 404 plano cuando no hay ni portada ni marca (nunca un redirect
 * al login: un bot que recibe 302 descarta el preview). El cuerpo y los headers viajan fijos,
 * lo demas lo cubre PlanOgImageServiceTest.
 */
public class PlanOgImageControllerTest {

  @Test
  public void servesTheJpegPubliclyWhenTheImageExists() {
    PlanService planService = mock(PlanService.class);
    PlanOgImageService ogImages = mock(PlanOgImageService.class);
    UUID id = UUID.randomUUID();
    Plan plan = mock(Plan.class);
    when(planService.getSharedPlan(id)).thenReturn(plan);
    when(ogImages.ogImageFor(plan)).thenReturn(Optional.of(new byte[] { 1, 2, 3 }));

    ResponseEntity<byte[]> response = new PlanOgImageController(planService, ogImages).ogImage(id);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders().getContentType(), is(MediaType.IMAGE_JPEG));
    assertThat(
      response.getHeaders().getCacheControl(),
      allOf(containsString("public"), containsString("max-age=604800"))
    );
    assertThat(response.getBody(), is(new byte[] { 1, 2, 3 }));
  }

  @Test
  public void answersPlainNotFoundWhenThereIsNoImageAtAll() {
    PlanService planService = mock(PlanService.class);
    PlanOgImageService ogImages = mock(PlanOgImageService.class);
    UUID id = UUID.randomUUID();
    Plan plan = mock(Plan.class);
    when(planService.getSharedPlan(id)).thenReturn(plan);
    when(ogImages.ogImageFor(plan)).thenReturn(Optional.empty());

    ResponseEntity<byte[]> response = new PlanOgImageController(planService, ogImages).ogImage(id);

    assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void anUnknownPlanIsANotFoundNotALoginRedirect() {
    PlanOgImageController controller = new PlanOgImageController(
      mock(PlanService.class),
      mock(PlanOgImageService.class)
    );

    // El @ExceptionHandler responde 404 plano: el manejo global redirige los PlanNotFound al
    // login y un bot que recibe 302 descarta el preview.
    assertThat(controller.notFound().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }
}
