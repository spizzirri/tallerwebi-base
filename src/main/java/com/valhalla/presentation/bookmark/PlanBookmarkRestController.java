package com.valhalla.presentation.bookmark;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.exception.PlanNotFoundException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [MARK] Toggle del sidebar de {@code /explore}: el feed se renderiza con Vue, asi que el icono
 * pega contra este endpoint y repinta la card sin recargar.
 *
 * <p>Devuelve 204 sin cuerpo: el cliente ya sabe el estado nuevo. Guardar dos veces no rompe
 * (idempotente), igual que quitar.
 */
@RestController
@RequestMapping("/api/plans/{planId}/bookmarks")
public class PlanBookmarkRestController {

  private final PlanBookmarkService planBookmarkService;

  @Autowired
  public PlanBookmarkRestController(PlanBookmarkService planBookmarkService) {
    this.planBookmarkService = planBookmarkService;
  }

  @PostMapping
  public ResponseEntity<Void> save(@PathVariable UUID planId, Principal principal) {
    this.planBookmarkService.save(planId, principal.getName());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> remove(@PathVariable UUID planId, Principal principal) {
    this.planBookmarkService.remove(planId, principal.getName());
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(PlanNotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound() {
    // REST necesita un 404 real: el GlobalExceptionHandler redirige a los callers HTML.
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
  }
}
