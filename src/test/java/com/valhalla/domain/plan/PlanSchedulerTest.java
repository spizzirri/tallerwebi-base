package com.valhalla.domain.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.planplace.PlanPlace;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/**
 * [SCH] Tests de la derivacion del itinerario: es la unica puerta por la que se escribe el horario
 * de un plan, asi que aca se fijan las reglas del slice 3 — duraciones completas, pines respetados
 * e idempotencia de la mano de la configuracion del plan.
 *
 * <p>Las dos paradas comparten coordenadas a proposito: el traslado cae al piso de cinco minutos y
 * el horario esperado no depende de ninguna ciudad.
 */
public class PlanSchedulerTest {

  private static final LocalDate PLAN_DATE = LocalDate.of(2026, 6, 13);
  private static final LocalTime DAY_START = LocalTime.of(10, 0);
  private static final LocalTime DAY_END = LocalTime.of(23, 0);
  private static final Double LATITUDE = -34.6;
  private static final Double LONGITUDE = -58.4;
  private static final int MUSEUM_DURATION_MINUTES = 90;

  @Test
  public void encadenaDosParadasConLaDuracionDelLugarYElTraslado() {
    // given: dos museos pegados, sin duracion propia ni horas
    Plan plan = plan(PLAN_DATE, DAY_START);
    PlanPlace first = addStop(plan, museum(MUSEUM_DURATION_MINUTES));
    PlanPlace second = addStop(plan, museum(MUSEUM_DURATION_MINUTES));

    // when
    PlanScheduler.apply(plan);

    // then: 90 minutos de visita + 5 de traslado entre dos paradas pegadas
    assertThat(plan.getPlanPlaces(), hasSize(2));
    assertThat(first.getDurationMinutes(), is(equalTo(MUSEUM_DURATION_MINUTES)));
    assertThat(second.getDurationMinutes(), is(equalTo(MUSEUM_DURATION_MINUTES)));
    assertThat(first.getVisitDate(), is(equalTo(PLAN_DATE)));
    assertThat(first.getVisitTime(), is(equalTo(LocalTime.of(10, 0))));
    assertThat(second.getVisitDate(), is(equalTo(PLAN_DATE)));
    assertThat(second.getVisitTime(), is(equalTo(LocalTime.of(11, 35))));
  }

  @Test
  public void completaLaDuracionNulaYNoPisaLaQueYaExistia() {
    // given: la primera parada trae un override de 30 minutos
    Plan plan = plan(PLAN_DATE, DAY_START);
    PlanPlace overridden = addStop(plan, museum(MUSEUM_DURATION_MINUTES));
    overridden.setDurationMinutes(30);
    PlanPlace inherited = addStop(plan, museum(MUSEUM_DURATION_MINUTES));

    // when
    PlanScheduler.apply(plan);

    // then: el override sobrevive y la nula hereda la duracion del lugar
    assertThat(overridden.getDurationMinutes(), is(equalTo(30)));
    assertThat(inherited.getDurationMinutes(), is(equalTo(MUSEUM_DURATION_MINUTES)));
    assertThat(overridden.getVisitTime(), is(equalTo(LocalTime.of(10, 0))));
    assertThat(inherited.getVisitTime(), is(equalTo(LocalTime.of(10, 35))));
  }

  @Test
  public void respetaElPinDeHoraYRecalculaLasSiguientesDesdeAhi() {
    // given: la segunda parada tiene la hora fijada a mano a las 14:00
    Plan plan = plan(PLAN_DATE, DAY_START);
    addStop(plan, museum(60));
    PlanPlace pinned = addStop(plan, museum(60));
    pinned.setTimePinned(true);
    pinned.setVisitDate(PLAN_DATE);
    pinned.setVisitTime(LocalTime.of(14, 0));
    PlanPlace afterPin = addStop(plan, museum(60));

    // when
    PlanScheduler.apply(plan);

    // then: la cadena se corta en el pin y sigue desde su hora de fin
    assertThat(pinned.getVisitDate(), is(equalTo(PLAN_DATE)));
    assertThat(pinned.getVisitTime(), is(equalTo(LocalTime.of(14, 0))));
    assertThat(afterPin.getVisitTime(), is(equalTo(LocalTime.of(15, 5))));
  }

  @Test
  public void sinFechaNoHaceNadaYDejaLasParadasIntactas() {
    // given: un plan sin fecha base, con horas viejas y duraciones nulas
    Plan plan = plan(null, DAY_START);
    PlanPlace stop = addStop(plan, museum(MUSEUM_DURATION_MINUTES));
    stop.setVisitDate(LocalDate.of(2026, 1, 1));
    stop.setVisitTime(LocalTime.of(19, 0));

    // when
    PlanScheduler.apply(plan);

    // then: sin jornada donde ubicar las paradas no se completa ni se pisa nada
    assertThat(stop.getDurationMinutes(), is(nullValue()));
    assertThat(stop.getVisitDate(), is(equalTo(LocalDate.of(2026, 1, 1))));
    assertThat(stop.getVisitTime(), is(equalTo(LocalTime.of(19, 0))));
  }

  @Test
  public void sinParadasNoHaceNada() {
    // given: un plan con fecha pero sin itinerario
    Plan plan = plan(PLAN_DATE, DAY_START);

    // when
    PlanScheduler.apply(plan);

    // then: no hay paradas que derivar y el plan queda como estaba
    assertThat(plan.getPlanPlaces(), hasSize(0));
  }

  @Test
  public void sinHoraDeInicioCompletaLasDuracionesYDejaLasHorasViejas() {
    // given: un plan sin hora de arranque y una parada con hora vieja
    Plan plan = plan(PLAN_DATE, null);
    PlanPlace stop = addStop(plan, museum(MUSEUM_DURATION_MINUTES));
    stop.setVisitDate(LocalDate.of(2026, 1, 1));
    stop.setVisitTime(LocalTime.of(19, 0));

    // when
    PlanScheduler.apply(plan);

    // then: la duracion queda completa y el horario viejo no se toca
    assertThat(stop.getDurationMinutes(), is(equalTo(MUSEUM_DURATION_MINUTES)));
    assertThat(stop.getVisitDate(), is(equalTo(LocalDate.of(2026, 1, 1))));
    assertThat(stop.getVisitTime(), is(equalTo(LocalTime.of(19, 0))));
  }

  @Test
  public void unPlanDeUnaParadaTerminaEnLaHoraDeInicioMasLaDuracion() {
    // given: un cafe sin duracion de lugar: manda el default de su categoria (45 minutos)
    Plan plan = plan(PLAN_DATE, DAY_START);
    Place cafe = place(PlaceCategory.CAFE, null);
    PlanPlace stop = addStop(plan, cafe);

    // when
    PlanScheduler.apply(plan);

    // then
    assertThat(stop.getDurationMinutes(), is(equalTo(45)));
    assertThat(stop.getVisitTime(), is(equalTo(DAY_START)));
  }

  @Test
  public void respetaElTrasladoFijoDeLaPrimeraParada() {
    // given: dos museos pegados (el calculado seria el piso de 5 minutos) pero el tramo de la
    // primera esta fijado a mano en 45
    Plan plan = plan(PLAN_DATE, DAY_START);
    PlanPlace first = addStop(plan, museum(MUSEUM_DURATION_MINUTES));
    first.setTravelMinutesToNext(45);
    addStop(plan, museum(MUSEUM_DURATION_MINUTES));

    // when
    PlanScheduler.apply(plan);

    // then: 90 de visita + 45 del tramo fijo, no los 5 del calculo
    assertThat(first.getTravelMinutesToNext(), is(equalTo(45)));
    assertThat(plan.getPlanPlaces().get(1).getVisitTime(), is(equalTo(LocalTime.of(12, 15))));
  }

  private static Plan plan(LocalDate eventDate, LocalTime eventTime) {
    Plan plan = new Plan();
    plan.setName("Itinerario");
    plan.setEventDate(eventDate);
    plan.setEventTime(eventTime);
    plan.setDayEndTime(DAY_END);
    plan.setTransportMode(TransportMode.WALK);
    return plan;
  }

  private static Place museum(int durationMinutes) {
    return place(PlaceCategory.MUSEUM, durationMinutes);
  }

  private static Place place(PlaceCategory category, Integer durationMinutes) {
    Place place = new Place();
    place.setName("Lugar " + category);
    place.setCategory(category);
    place.setDefaultDurationMinutes(durationMinutes);
    place.setLatitude(LATITUDE);
    place.setLongitude(LONGITUDE);
    return place;
  }

  private static PlanPlace addStop(Plan plan, Place place) {
    PlanPlace stop = new PlanPlace();
    stop.setPlace(place);
    stop.setSortOrder(plan.getPlanPlaces().size() + 1);
    plan.addPlanPlace(stop);
    return stop;
  }
}
