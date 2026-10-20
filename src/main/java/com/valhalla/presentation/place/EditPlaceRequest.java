package com.valhalla.presentation.place;

import com.valhalla.domain.place.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EditPlaceRequest {

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

  public EditPlaceRequest() {}

  public EditPlaceRequest(
    String name,
    PlaceCategory category,
    String description,
    String address,
    String openingHours,
    String phone,
    String website,
    String imageUrl,
    Double latitude,
    Double longitude
  ) {
    this.name = name;
    this.category = category;
    this.description = description;
    this.address = address;
    this.openingHours = openingHours;
    this.phone = phone;
    this.website = website;
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
  }

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
}
