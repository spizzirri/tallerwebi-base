package com.valhalla.domain.bookmark;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** [MARK] Acceso a los planes guardados por cada usuario. */
public interface PlanBookmarkRepository {
  Optional<PlanBookmark> findByUserIdAndPlanId(Long userId, UUID planId);

  /** Mas recientes primero; el id desempata dos guardados en el mismo milisegundo. */
  List<PlanBookmark> findByUserId(Long userId);

  Set<UUID> findPlanIdsByUserId(Long userId);

  PlanBookmark save(PlanBookmark bookmark);

  void deleteByUserIdAndPlanId(Long userId, UUID planId);

  void deleteAllByUserId(Long userId);
}
