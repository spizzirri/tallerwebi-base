package com.valhalla.infrastructure.rating;

import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.rating.RatingSummary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanRatingRepositoryImpl implements PlanRatingRepository {

  private final JpaPlanRatingRepository jpaPlanRatingRepository;

  @Autowired
  public PlanRatingRepositoryImpl(JpaPlanRatingRepository jpaPlanRatingRepository) {
    this.jpaPlanRatingRepository = jpaPlanRatingRepository;
  }

  @Override
  public Optional<PlanRating> findByPlanIdAndUserEmail(UUID planId, String email) {
    return jpaPlanRatingRepository.findByPlanIdAndUserEmail(planId, email);
  }

  @Override
  public List<RatingSummary> findSummaries() {
    return jpaPlanRatingRepository.findSummaries();
  }

  @Override
  public PlanRating save(PlanRating rating) {
    return jpaPlanRatingRepository.save(rating);
  }

  @Override
  public void deleteAllByPlanId(UUID planId) {
    jpaPlanRatingRepository.deleteAllByPlanId(planId);
  }
}
