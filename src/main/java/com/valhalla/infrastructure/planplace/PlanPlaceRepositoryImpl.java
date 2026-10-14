package com.valhalla.infrastructure.planplace;

import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PlanPlaceRepositoryImpl implements PlanPlaceRepository {

  private final JpaPlanPlaceRepository jpa;

  public PlanPlaceRepositoryImpl(JpaPlanPlaceRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public PlanPlace save(PlanPlace entry) {
    return jpa.save(entry);
  }

  @Override
  public List<PlanPlace> findByPlanId(UUID id) {
    return jpa.findByPlanIdOrderBySortOrder(id);
  }

  @Override
  public Optional<PlanPlace> findById(Long id) {
    return jpa.findById(id);
  }

  @Override
  public boolean existsByPlanIdAndPlaceId(UUID planId, Long placeId) {
    return jpa.existsByPlanIdAndPlaceId(planId, placeId);
  }

  @Override
  public void deleteById(Long id) {
    jpa.deleteById(id);
  }
}
