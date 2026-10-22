package com.valhalla.domain.place;

import com.valhalla.domain.plan.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "places")
public class Place { // NOPMD — intentional: 11 fields vs maxfields=10; the 3 extra (openingHours, phone, website) are optional OSM-imported columns, one getter/setter pair each, no behaviour.

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @NotBlank(message = "Name is required")
  @Column(nullable = false)
  private String name;

  // Description comun, sin @Lob: en PostgreSQL ese mapeo es una columna oid (large object),
  // que exige transaccion para leerse (Large Objects may not be used in auto-commit mode).
  // Con length queda varchar(n) de toda la vida, legible en auto-commit en ambos dialectos
  // (HSQLDB de las suites, PostgreSQL de docker-dev).
  @Column(length = 2048)
  private String description;

  @NotNull(message = "Category is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlaceCategory category;

  private String address;

  @Column(name = "image_url")
  private String imageUrl;

  private Double latitude;
  private Double longitude;

  // Opcionales (nullable), como address: el importador de OpenStreetMap las trae y el ABM
  // las edita. Sin @Lob por la razon documentada arriba en description.
  @Column(name = "opening_hours")
  private String openingHours;

  private String phone;

  private String website;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public PlaceCategory getCategory() {
    return category;
  }

  public void setCategory(PlaceCategory category) {
    this.category = category;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
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
}
