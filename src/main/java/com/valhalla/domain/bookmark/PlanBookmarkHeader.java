package com.valhalla.domain.bookmark;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * [MARK] Encabezado congelado del plan al momento de guardarlo.
 *
 * <p>Vive embebido en {@link PlanBookmark} para que la entidad no acumule columnas sueltas: es una
 * foto del plan, no un conjunto de campos con comportamiento propio.
 */
@Embeddable
public class PlanBookmarkHeader {

  @Column(name = "plan_name", nullable = false)
  private String name;

  @Column(name = "plan_description")
  private String description;

  @Column(name = "event_date")
  private LocalDate eventDate;

  @Column(name = "event_time")
  private LocalTime eventTime;

  @Column(name = "place_count", nullable = false)
  private int placeCount;

  @Column(name = "administrator_id")
  private UUID administratorId;

  @Column(name = "administrator_name")
  private String administratorName;

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

  public int getPlaceCount() {
    return placeCount;
  }

  public void setPlaceCount(int placeCount) {
    this.placeCount = placeCount;
  }

  public UUID getAdministratorId() {
    return administratorId;
  }

  public void setAdministratorId(UUID administratorId) {
    this.administratorId = administratorId;
  }

  public String getAdministratorName() {
    return administratorName;
  }

  public void setAdministratorName(String administratorName) {
    this.administratorName = administratorName;
  }
}
