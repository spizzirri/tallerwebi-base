package com.valhalla.domain.planplace;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.TransportMode;
import com.valhalla.domain.plan.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "plan_places")
// [APL] Once columnas: la entidad mapea la tabla completa; bajar de 10 exigiria partir la entidad.
@SuppressWarnings("PMD.TooManyFields")
public class PlanPlace {

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  private Plan plan;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "place_id", nullable = false)
  private Place place;

  @Column
  private String description;

  @Column(name = "visit_date")
  private LocalDate visitDate;

  @Column(name = "visit_time")
  private LocalTime visitTime;

  // [SCH] Override de la duracion de la visita y del modo del tramo hacia la parada siguiente. Nulos
  // = el planificador usa la duracion del lugar y el modo por defecto del plan.
  @Column(name = "duration_minutes")
  private Integer durationMinutes;

  @Enumerated(EnumType.STRING)
  @Column(name = "transport_mode_to_next")
  private TransportMode transportModeToNext;

  // [SCH] Minutos de traslado hacia la parada siguiente, a mano. Nulo = el planificador estima el
  // tramo por distancia y modo. Un valor fijado se respeta tal cual (piso 1 minuto, sin el techo
  // de 90 del calculado): el usuario conocio el trayecto mejor que la estimacion.
  @Column(name = "travel_minutes_to_next")
  private Integer travelMinutesToNext;

  // [SCH] El usuario fijo esta hora a mano: la derivacion arranca de aca y recalcula las
  // siguientes, en vez de pisarla. La columna no figuraba en la tabla de la spec (que solo
  // describia cuatro columnas nuevas), pero sin la marca un pin solo se podria detectar adivinando
  // al comparar la hora guardada con la derivada, y esa comparacion confunde una hora vieja con una
  // elegida. Con la marca, D-6 ("un pin explicito corta la cadena") es implementable sin heuristica.
  @Column(name = "time_pinned")
  private Boolean timePinned = false;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Plan getPlan() {
    return plan;
  }

  public void setPlan(Plan plan) {
    this.plan = plan;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
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

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public TransportMode getTransportModeToNext() {
    return transportModeToNext;
  }

  public void setTransportModeToNext(TransportMode transportModeToNext) {
    this.transportModeToNext = transportModeToNext;
  }

  public Integer getTravelMinutesToNext() {
    return travelMinutesToNext;
  }

  public void setTravelMinutesToNext(Integer travelMinutesToNext) {
    this.travelMinutesToNext = travelMinutesToNext;
  }

  /**
   * [SCH] Marca de pin: la hora de esta parada la eligio el usuario y la derivacion tiene que
   * respetarla. La columna es la unica forma de distinguir un pin de una hora derivada en una
   * corrida anterior, sin comparar horas.
   *
   * @return {@code true} si la hora de la parada no se puede recalcular
   */
  public boolean isTimePinned() {
    return Boolean.TRUE.equals(timePinned);
  }

  public void setTimePinned(boolean timePinned) {
    this.timePinned = timePinned;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }
}
