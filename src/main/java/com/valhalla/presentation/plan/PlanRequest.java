package com.valhalla.presentation.plan;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public class PlanRequest {

  @NotBlank(message = "Plan name is required")
  private String name;

  private String description;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate eventDate;

  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime eventTime;

  private Boolean isPublic;

  @jakarta.validation.Valid
  private com.valhalla.domain.plan.PlanSettings settings =
    new com.valhalla.domain.plan.PlanSettings();

  public com.valhalla.domain.plan.PlanSettings getSettings() {
    return settings;
  }

  public void setSettings(com.valhalla.domain.plan.PlanSettings settings) {
    this.settings = settings;
  }

  private List<PlanPlaceRequest> places = new ArrayList<>();

  public PlanRequest() {}

  public PlanRequest(String name, String description, LocalDate eventDate, Boolean isPublic) {
    this.name = name;
    this.description = description;
    this.eventDate = eventDate;
    this.isPublic = isPublic;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getEventDate() {
    return eventDate;
  }

  public void setEventDate(LocalDate eventDate) {
    this.eventDate = eventDate;
  }

  public LocalTime getEventTime() {
    return eventTime;
  }

  public void setEventTime(LocalTime eventTime) {
    this.eventTime = eventTime;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public List<PlanPlaceRequest> getPlaces() {
    return places;
  }

  public void setPlaces(List<PlanPlaceRequest> places) {
    this.places = places;
  }
}
