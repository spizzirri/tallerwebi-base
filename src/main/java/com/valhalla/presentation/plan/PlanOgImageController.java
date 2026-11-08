package com.valhalla.presentation.plan;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * [OG-JPG] La portada del plan como JPG fijo para los scrapers: ruta publica sin sesion (los
 * bots de WhatsApp/Discord no la tienen), sin redirecciones y cacheada en memoria por el
 * servicio. El id inexistente responde 404 propio: el GlobalExceptionHandler redirige los
 * PlanNotFound al login, y un bot que recibe 302 descarta el preview.
 */
@Controller
@RequestMapping("/plans")
public class PlanOgImageController {

  private final PlanService planService;
  private final PlanOgImageService ogImages;

  public PlanOgImageController(PlanService planService, PlanOgImageService ogImages) {
    this.planService = planService;
    this.ogImages = ogImages;
  }

  @GetMapping("/{id}/og-image")
  public ResponseEntity<byte[]> ogImage(@PathVariable UUID id) {
    Plan plan = planService.getSharedPlan(id);
    return ogImages
      .ogImageFor(plan)
      .map(jpg ->
        ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_JPEG)
          .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
          .body(jpg)
      )
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @ExceptionHandler(PlanNotFoundException.class)
  public ResponseEntity<Void> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
