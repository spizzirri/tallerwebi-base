package com.valhalla.domain.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "places")
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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
}
