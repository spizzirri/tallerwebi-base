package com.valhalla.presentation.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

/** One itinerary row of the create-plan form, bound from {@code places[i].*}. */
public class PlanPlaceRequest {

  private UUID placeId;

  private String description;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate visitDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
  private LocalTime visitTime;

  public PlanPlaceRequest() {}

  public PlanPlaceRequest(
    UUID placeId,
    String description,
    LocalDate visitDate,
    LocalTime visitTime
  ) {
    this.placeId = placeId;
    this.description = description;
    this.visitDate = visitDate;
    this.visitTime = visitTime;
  }

  public UUID getPlaceId() {
    return placeId;
  }

  public void setPlaceId(UUID placeId) {
    this.placeId = placeId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getVisitDate() {
    return visitDate;
  }

  public void setVisitDate(LocalDate visitDate) {
    this.visitDate = visitDate;
  }

  public LocalTime getVisitTime() {
    return visitTime;
  }

  public void setVisitTime(LocalTime visitTime) {
    this.visitTime = visitTime;
  }
}
