package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.planplace.PlanPlace;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPlanPlaceRepository extends JpaRepository<PlanPlace, Long> {
  @EntityGraph(attributePaths = { "place" })
  List<PlanPlace> findByPlanIdOrderBySortOrder(UUID planId);

  boolean existsByPlanIdAndPlaceId(UUID planId, Long placeId);

  boolean existsByPlaceId(Long placeId);
}
