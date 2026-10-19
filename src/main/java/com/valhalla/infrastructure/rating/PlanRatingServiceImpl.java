package com.valhalla.infrastructure.rating;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.rating.PlanRating;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
public class PlanRatingServiceImpl implements PlanRatingService {

  private static final int MIN_SCORE = 1;
  private static final int MAX_SCORE = 5;

  private final PlanRatingRepository planRatingRepository;
  private final PlanRepository planRepository;
  private final UserRepository userRepository;

  @Autowired
  public PlanRatingServiceImpl(
    PlanRatingRepository planRatingRepository,
    PlanRepository planRepository,
    UserRepository userRepository
  ) {
    this.planRatingRepository = planRatingRepository;
    this.planRepository = planRepository;
    this.userRepository = userRepository;
  }

  /**
   * Upsert: la unique constraint (plan_id, user_id) es el guard real; el find-or-create solo
   * evita chocarla en el camino feliz. El administrador puede calificar su propio plan: la card
   * no pone restricciones (16-CAL.md).
   */
  @Override
  @Transactional
  public PlanRating rate(UUID planId, String email, int score) {
    if (score < MIN_SCORE || score > MAX_SCORE) {
      throw new IllegalArgumentException("La calificacion tiene que ser de 1 a 5 estrellas.");
    }
    Plan plan = planRepository.findById(planId).orElseThrow(PlanNotFoundException::new);
    if (!plan.getIsPublic()) {
      // Privado e inexistente responden igual (AC-06): el endpoint no es oraculo de ids.
      throw new PlanNotFoundException();
    }
    User user = userRepository.findByEmail(email).orElseThrow(PlanNotFoundException::new);
    PlanRating rating = planRatingRepository
      .findByPlanIdAndUserEmail(planId, email)
      .orElseGet(() -> new PlanRating(plan, user, score));
    rating.setScore(score);
    return planRatingRepository.save(rating);
  }

  @Override
  @Transactional(readOnly = true)
  public RatingSummary getSummary(UUID planId) {
    return planRatingRepository
      .findSummaries()
      .stream()
      .filter(summary -> summary.planId().equals(planId))
      .findFirst()
      .orElse(new RatingSummary(planId, null, 0L));
  }

  @Override
  @Transactional(readOnly = true)
  public Integer getMyRating(UUID planId, String email) {
    if (email == null) {
      return null;
    }
    return planRatingRepository
      .findByPlanIdAndUserEmail(planId, email)
      .map(PlanRating::getScore)
      .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, RatingSummary> getSummariesByPlanId() {
    return planRatingRepository
      .findSummaries()
      .stream()
      .collect(Collectors.toMap(RatingSummary::planId, Function.identity()));
  }
}
