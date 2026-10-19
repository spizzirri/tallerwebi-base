package com.valhalla.domain.rating;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** [CAL] Acceso a las calificaciones de los planes. */
public interface PlanRatingRepository {
  Optional<PlanRating> findByPlanIdAndUserEmail(UUID planId, String email);

  List<RatingSummary> findSummaries();

  PlanRating save(PlanRating rating);

  void deleteAllByPlanId(UUID planId);
}
