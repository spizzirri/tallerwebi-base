package com.valhalla.domain.planplace;

import com.valhalla.domain.plan.TransportMode;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * [SCH] Ajuste manual de una parada del itinerario: duracion de la visita, horario fijado, modo y
 * minutos del tramo hacia la siguiente.
 *
 * <p>Una fecha u hora nula significa "no lo fijes, derivalo": el planificador vuelve a escribir ese
 * dato en la proxima pasada. Si llega cualquiera de las dos, la parada queda marcada como pin y las
 * siguientes se recalculan desde ella.
 *
 * @param durationMinutes override de la duracion de la visita; {@code null} usa la del lugar
 * @param visitDate dia fijado por el usuario; {@code null} lo deriva el planificador
 * @param visitTime hora fijada por el usuario; {@code null} la deriva el planificador
 * @param transportModeToNext modo del tramo hacia la parada siguiente; {@code null} usa el del plan
 * @param travelMinutesToNext minutos del tramo hacia la parada siguiente; {@code null} vuelve al
 *     calculo por distancia, con un minimo de 1 si el valor no lo alcanza
 */
public record PlanPlaceUpdate(
  Integer durationMinutes,
  LocalDate visitDate,
  LocalTime visitTime,
  TransportMode transportModeToNext,
  Integer travelMinutesToNext
) {}
