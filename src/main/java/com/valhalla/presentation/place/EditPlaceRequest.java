package com.valhalla.presentation.place;

import com.valhalla.domain.place.PlaceCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EditPlaceRequest { // NOPMD — intencional: 12 campos vs maxfields=10; es el espejo del form del ABM, un getter/setter por campo y sin comportamiento.

  @NotBlank(message = "Name is required")
  private String name;

  @NotNull(message = "Category is required")
  private PlaceCategory category;

  private String description;

  private String address;

  private String openingHours;

  private String phone;

  private String website;

  private String imageUrl;

  private Double latitude;

  private Double longitude;

  // Identidad del POI de origen cuando el alta viene del importador; null en un alta manual.
  private String externalId;

  // [SCH] Duracion tipica de la visita, en minutos. Vacio (null) = "segun categoria": es lo que el
  // planificador resuelve con PlanScheduleCalculator cuando la parada no trae duracion propia.
  // El piso de 5 minutos repite el min="5" del input; no hay techo, el form usa step="5".
  @Min(value = 5, message = "Duration must be at least 5 minutes")
  private Integer defaultDurationMinutes;

  public EditPlaceRequest() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PlaceCategory getCategory() {
    return category;
  }

  public void setCategory(PlaceCategory category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getOpeningHours() {
    return openingHours;
  }

  public void setOpeningHours(String openingHours) {
    this.openingHours = openingHours;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public Integer getDefaultDurationMinutes() {
    return defaultDurationMinutes;
  }

  public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
    this.defaultDurationMinutes = defaultDurationMinutes;
  }
}
