package com.valhalla.domain.planplace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanPlaceRepository {
  PlanPlace save(PlanPlace entry);
  List<PlanPlace> findByPlanId(UUID planId);
  Optional<PlanPlace> findById(Long id);
  boolean existsByPlanIdAndPlaceId(UUID planId, Long placeId);
  boolean existsByPlaceId(Long placeId);
  void deleteById(Long id);
}
