package com.valhalla.presentation.plan;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans/{planId}/places")
public class PlanPlaceRestController {

  private final PlanPlaceService service;

  public PlanPlaceRestController(PlanPlaceService service) {
    this.service = service;
  }

  @GetMapping
  public List<ItineraryEntry> getItinerary(@PathVariable UUID planId, Principal principal) {
    return service
      .getItinerary(planId, principal == null ? null : principal.getName())
      .stream()
      .map(ItineraryEntry::from)
      .toList();
  }

  @PostMapping
  public ItineraryEntry addPlace(
    @PathVariable UUID planId,
    @RequestParam Long placeId,
    Principal principal
  ) {
    return ItineraryEntry.from(service.addPlaceToPlan(planId, placeId, principal.getName()));
  }

  @PutMapping("/{id}")
  public ItineraryEntry updatePlanPlace(
    @PathVariable UUID planId,
    @PathVariable Long id,
    @RequestBody PlanPlaceUpdateRequest request,
    Principal principal
  ) {
    return ItineraryEntry.from(
      service.updatePlanPlace(
        planId,
        id,
        request.visitDate() == null ? null : LocalDate.parse(request.visitDate()),
        request.visitTime() == null ? null : LocalTime.parse(request.visitTime()),
        principal.getName()
      )
    );
  }

  @DeleteMapping("/{id}")
  public void removePlace(@PathVariable UUID planId, @PathVariable Long id, Principal principal) {
    service.removePlaceFromPlan(planId, id, principal.getName());
  }

  @PostMapping("/reorder")
  public void reorderPlaces(
    @PathVariable UUID planId,
    @RequestBody List<Long> placeIds,
    Principal principal
  ) {
    service.reorderPlaces(planId, placeIds, principal.getName());
  }

  // GlobalExceptionHandler redirects HTML callers; REST callers need an actual 404 without revealing ownership.
  @ExceptionHandler(PlanNotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
  }

  @ExceptionHandler(
    {
      IllegalArgumentException.class,
      DateTimeParseException.class,
      org.springframework.http.converter.HttpMessageNotReadableException.class,
    }
  )
  public ResponseEntity<Map<String, String>> invalid() {
    return ResponseEntity.badRequest().body(Map.of("error", "Invalid itinerary request"));
  }

  // Returning entities would serialize Plan.administrator (including password) and lazy Hibernate proxies.
  public record ItineraryEntry(
    Long id,
    Long placeId,
    String name,
    Integer sortOrder,
    String visitDate,
    String visitTime,
    String description,
    String placeDescription,
    String address,
    String category,
    Double latitude,
    Double longitude
  ) {
    static ItineraryEntry from(PlanPlace entry) {
      return new ItineraryEntry(
        entry.getId(),
        entry.getPlace().getId(),
        entry.getPlace().getName(),
        entry.getSortOrder(),
        entry.getVisitDate() == null ? null : entry.getVisitDate().toString(),
        entry.getVisitTime() == null ? null : entry.getVisitTime().toString(),
        entry.getDescription(),
        entry.getPlace().getDescription(),
        entry.getPlace().getAddress(),
        entry.getPlace().getCategory() == null ? null : entry.getPlace().getCategory().name(),
        entry.getPlace().getLatitude(),
        entry.getPlace().getLongitude()
      );
    }
  }
}
