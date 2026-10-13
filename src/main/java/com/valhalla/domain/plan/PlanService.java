package com.valhalla.domain.plan;

import java.util.List;

public interface PlanService {
  Plan createPlan(Plan plan, String ownerEmail);
  List<Plan> getPlansByUserEmail(String ownerEmail);
  Plan getOwnedPlan(Long id, String ownerEmail);
  Plan getParticipatingPlan(Long id, String userEmail);
  Plan getPublicPlan(Long id);
  List<Plan> getParticipantPlans(String userEmail);
  void leavePlan(Long id, String userEmail);
  Plan joinPlan(String shortCode, String userEmail);
  Plan updatePlan(Long id, Plan changes, String ownerEmail);
  void deleteOwnedPlan(Long id, String ownerEmail);
}
