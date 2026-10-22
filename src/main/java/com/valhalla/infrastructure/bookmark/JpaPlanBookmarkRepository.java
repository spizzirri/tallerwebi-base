package com.valhalla.infrastructure.bookmark;

import com.valhalla.domain.bookmark.PlanBookmark;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaPlanBookmarkRepository extends JpaRepository<PlanBookmark, UUID> {
  Optional<PlanBookmark> findByUserIdAndPlanId(UUID userId, UUID planId);

  List<PlanBookmark> findByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

  /** [MARK] Flag por request para las cards: una sola query, sin N+1 por plan. */
  @Query("select bookmark.planId from PlanBookmark bookmark where bookmark.user.id = :userId")
  Set<UUID> findPlanIdsByUserId(UUID userId);

  void deleteByUserIdAndPlanId(UUID userId, UUID planId);

  /** [MARK] Borrar un usuario del ABM admin borra sus guardados: la FK user_id es owner. */
  void deleteAllByUserId(UUID userId);
}
