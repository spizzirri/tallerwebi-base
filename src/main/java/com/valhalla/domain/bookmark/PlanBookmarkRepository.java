package com.valhalla.domain.bookmark;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** [MARK] Acceso a los planes guardados por cada usuario. */
public interface PlanBookmarkRepository {
  Optional<PlanBookmark> findByUserIdAndPlanId(UUID userId, UUID planId);

  /** Mas recientes primero; el id desempata dos guardados en el mismo milisegundo. */
  List<PlanBookmark> findByUserId(UUID userId);

  Set<UUID> findPlanIdsByUserId(UUID userId);

  PlanBookmark save(PlanBookmark bookmark);

  void deleteByUserIdAndPlanId(UUID userId, UUID planId);

  void deleteAllByUserId(UUID userId);
}
