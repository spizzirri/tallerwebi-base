package com.valhalla.domain.plan;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.planplace.PlanPlace;
import java.util.List;

/**
 * [SCH] Derivacion del itinerario de un plan: completa las duraciones que faltan y escribe la fecha
 * y la hora de cada parada.
 *
 * <p>Es la unica forma de escribir el horario derivado de un plan. Los servicios y el backfill
 * llaman a {@link #apply(Plan)} en vez de repetir la derivacion: con una sola copia de la regla, el
 * alta, la edicion y la puesta al dia de la base no pueden discrepar. La calculadora
 * ({@link PlanScheduleCalculator}) queda pura y sin acceso a entidades; esta clase es la unica que
 * las toca.
 *
 * <p>Respeta los pines: una parada con {@link PlanPlace#isTimePinned()} arranca en la hora que fijo
 * el usuario y las siguientes se recalculan desde ahi (D-6). Es idempotente: derivar dos veces el
 * mismo plan da el mismo horario, porque el resultado no depende de las horas ya escritas sino de
 * las duraciones, los pines y la configuracion del plan.
 */
public final class PlanScheduler {

  private PlanScheduler() {}

  /**
   * Deriva el horario de {@code plan} y lo deja escrito en sus paradas.
   *
   * <p>Sin fecha no hay jornada donde ubicar las paradas y sin paradas no hay nada que ubicar: en
   * los dos casos no toca nada. Sin hora de inicio la calculadora devuelve una lista vacia; las
   * duraciones ya quedaron completas y las horas viejas se dejan intactas, en vez de quedar a medio
   * escribir.
   *
   * @param plan plan a derivar; se modifica en memoria, no se persiste
   */
  public static void apply(Plan plan) {
    if (plan.getEventDate() == null || plan.getPlanPlaces().isEmpty()) {
      return;
    }
    List<PlanPlace> stops = plan.getPlanPlaces();
    for (PlanPlace stop : stops) {
      if (stop.getDurationMinutes() == null) {
        Place place = stop.getPlace();
        stop.setDurationMinutes(
          PlanScheduleCalculator.effectiveDuration(
            null,
            place.getDefaultDurationMinutes(),
            place.getCategory()
          )
        );
      }
    }
    List<PlanScheduleCalculator.Slot> slots = PlanScheduleCalculator.schedule(
      plan.getEventDate(),
      plan.getEventTime(),
      plan.getDayEndTime(),
      plan.getTransportMode(),
      stops.stream().map(PlanScheduler::toStop).toList()
    );
    if (slots.size() != stops.size()) {
      return;
    }
    for (int index = 0; index < slots.size(); index++) {
      stops.get(index).setVisitDate(slots.get(index).date());
      stops.get(index).setVisitTime(slots.get(index).start());
    }
  }

  /**
   * Una parada tal como entra a la calculadora. Los pines salen de la marca y no de las horas
   * guardadas: una parada sin pin puede tener una hora vieja que la derivacion tiene que pisar.
   */
  private static PlanScheduleCalculator.Stop toStop(PlanPlace stop) {
    Place place = stop.getPlace();
    boolean pinned = stop.isTimePinned();
    return new PlanScheduleCalculator.Stop(
      pinned ? stop.getVisitDate() : null,
      pinned ? stop.getVisitTime() : null,
      stop.getDurationMinutes(),
      place.getDefaultDurationMinutes(),
      place.getCategory(),
      place.getLatitude(),
      place.getLongitude(),
      stop.getTransportModeToNext(),
      stop.getTravelMinutesToNext()
    );
  }
}
