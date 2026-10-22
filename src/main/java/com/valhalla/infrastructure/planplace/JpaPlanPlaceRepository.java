package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.planplace.PlanPlace;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPlanPlaceRepository extends JpaRepository<PlanPlace, UUID> {
  @EntityGraph(attributePaths = { "place" })
  List<PlanPlace> findByPlanIdOrderBySortOrder(UUID planId);

  boolean existsByPlanIdAndPlaceId(UUID planId, UUID placeId);

  boolean existsByPlaceId(UUID placeId);
}
