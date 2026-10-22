package com.valhalla.domain.bookmark;

import com.valhalla.domain.place.PlaceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * [MARK] Una parada del itinerario copiada al guardar el plan.
 *
 * <p>{@code placeId} es una columna plana, <strong>sin FK</strong>: el lugar puede desaparecer del
 * catalogo y el snapshot tiene que seguir existiendo. Al clonar desde el snapshot se omiten las
 * paradas cuyo lugar ya no existe; los datos copiados (nombre, categoria, direccion) sostienen la
 * card sin volver a consultar el catalogo.
 */
@Entity
@Table(name = "plan_bookmark_places")
public class PlanBookmarkPlace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "bookmark_id", nullable = false)
  private PlanBookmark bookmark;

  @Column(name = "place_id")
  private Long placeId;

  @Column(name = "place_name", nullable = false)
  private String placeName;

  @Enumerated(EnumType.STRING)
  @Column(name = "place_category")
  private PlaceCategory placeCategory;

  @Column(name = "place_address")
  private String placeAddress;

  @Column(length = 2048)
  private String description;

  @Column(name = "visit_date")
  private LocalDate visitDate;

  @Column(name = "visit_time")
  private LocalTime visitTime;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  public Long getId() {
    return id;
  }

  public PlanBookmark getBookmark() {
    return bookmark;
  }

  public void setBookmark(PlanBookmark bookmark) {
    this.bookmark = bookmark;
  }

  public Long getPlaceId() {
    return placeId;
  }

  public void setPlaceId(Long placeId) {
    this.placeId = placeId;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  public PlaceCategory getPlaceCategory() {
    return placeCategory;
  }

  public void setPlaceCategory(PlaceCategory placeCategory) {
    this.placeCategory = placeCategory;
  }

  public String getPlaceAddress() {
    return placeAddress;
  }

  public void setPlaceAddress(String placeAddress) {
    this.placeAddress = placeAddress;
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

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }
}
