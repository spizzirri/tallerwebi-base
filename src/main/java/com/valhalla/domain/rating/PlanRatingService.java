package com.valhalla.domain.rating;

import java.util.Map;
import java.util.UUID;

/** [CAL] Casos de uso de las calificaciones de 1 a 5 sobre planes publicos (16-CAL.md). */
public interface PlanRatingService {
  /**
   * Crea o reemplaza la calificacion del usuario sobre el plan (AC-01/AC-04). Solo planes
   * publicos: privado e inexistente responden igual que un id inexistente (AC-06).
   */
  PlanRating rate(UUID planId, String email, int score);

  /** Promedio y cantidad de calificaciones de un plan; count 0 con promedio null si no hay (AC-07). */
  RatingSummary getSummary(UUID planId);

  /** Puntaje propio del usuario sobre el plan, o null si no califico (AC-03). */
  Integer getMyRating(UUID planId, String email);

  /**
   * Un agregado por plan para las tarjetas del feed [PPV]: una sola query, sin N+1. Los planes sin
   * calificaciones no aparecen en el mapa.
   */
  Map<UUID, RatingSummary> getSummariesByPlanId();
}
