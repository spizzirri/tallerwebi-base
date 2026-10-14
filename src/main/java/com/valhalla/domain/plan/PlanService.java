package com.valhalla.domain.plan;

import java.util.List;
import java.util.UUID;

public interface PlanService {
  Plan createPlan(Plan plan, String ownerEmail);
  List<Plan> getPlansByUserEmail(String ownerEmail);
  List<Plan> getPublicPlans();
  Plan getOwnedPlan(UUID id, String ownerEmail);
  Plan getEditablePlan(UUID id, String userEmail);
  void changeParticipantRole(UUID id, String participantEmail, String role, String userEmail);
  void removeParticipant(UUID id, String participantEmail, String userEmail);
  void inviteParticipant(UUID id, String participantEmail, String role, String userEmail);
  List<PendingInvitation> getPendingInvitations(String userEmail);
  void respondToInvitation(UUID planId, Long invitationId, boolean accept, String userEmail);

  record PendingInvitation(
    Long id,
    UUID planId,
    String planName,
    boolean isPublic,
    String inviterName,
    String inviterEmail,
    String role
  ) {}

  Plan getParticipatingPlan(UUID id, String userEmail);
  Plan getPublicPlan(UUID id);
  List<Plan> getParticipantPlans(String userEmail);
  void leavePlan(UUID id, String userEmail);
  Plan joinPlan(String shortCode, String userEmail);
  Plan updatePlan(UUID id, Plan changes, String ownerEmail);
  void deleteOwnedPlan(UUID id, String ownerEmail);
  /** [CLO] Copia propia y editable de un plan accesible: nuevo dueño, nuevo shortCode. */
  Plan clonePlan(UUID id, String clonerEmail);
}
