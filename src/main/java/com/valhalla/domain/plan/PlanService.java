package com.valhalla.domain.plan;

import java.util.List;

public interface PlanService {
  Plan createPlan(Plan plan, String ownerEmail);
  List<Plan> getPlansByUserEmail(String ownerEmail);
  List<Plan> getPublicPlans();
  Plan getOwnedPlan(Long id, String ownerEmail);
  Plan getEditablePlan(Long id, String userEmail);
  void changeParticipantRole(Long id, String participantEmail, String role, String userEmail);
  void removeParticipant(Long id, String participantEmail, String userEmail);
  void inviteParticipant(Long id, String participantEmail, String role, String userEmail);
  List<PendingInvitation> getPendingInvitations(String userEmail);
  void respondToInvitation(Long planId, Long invitationId, boolean accept, String userEmail);

  record PendingInvitation(
    Long id,
    Long planId,
    String planName,
    String inviterName,
    String inviterEmail,
    String role
  ) {}

  Plan getParticipatingPlan(Long id, String userEmail);
  Plan getPublicPlan(Long id);
  List<Plan> getParticipantPlans(String userEmail);
  void leavePlan(Long id, String userEmail);
  Plan joinPlan(String shortCode, String userEmail);
  Plan updatePlan(Long id, Plan changes, String ownerEmail);
  void deleteOwnedPlan(Long id, String ownerEmail);
  /** [CLO] Copia propia y editable de un plan accesible: nuevo dueño, nuevo shortCode. */
  Plan clonePlan(Long id, String clonerEmail);
}
