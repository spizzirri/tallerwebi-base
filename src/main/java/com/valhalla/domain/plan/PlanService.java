package com.valhalla.domain.plan;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlanService {
  Plan getSharedPlan(UUID id);
  void requestToJoin(UUID id, String userEmail);
  void respondToJoinRequest(UUID id, UUID requestId, String action, String userEmail);
  List<PendingJoinRequest> getPendingJoinRequests(UUID id, String userEmail);
  void updateSettings(
    UUID id,
    PlanPermission shareLink,
    PlanPermission handleRequests,
    PlanPermission inviteInternal,
    PlanPermission inviteByEmail,
    String userEmail
  );

  record PendingJoinRequest(UUID id, UUID requesterId, String requesterName) {}

  Plan createPlan(Plan plan, String ownerEmail);
  List<Plan> getPlansByUserEmail(String ownerEmail);
  List<Plan> getPublicPlans();
  /**
   * [FOLLOW] Planes publicos de los autores indicados: la fuente del scope Siguiendo. Conjunto
   * vacio cuando no se sigue a nadie; el orden y los filtros siguen siendo de {@code PlanDiscovery}.
   */
  List<Plan> getPublicPlansByAdministrators(Collection<UUID> administratorIds);
  Plan getOwnedPlan(UUID id, String ownerEmail);
  Plan getEditablePlan(UUID id, String userEmail);
  void changeParticipantRole(UUID id, String participantEmail, String role, String userEmail);
  void removeParticipant(UUID id, String participantEmail, String userEmail);
  void inviteParticipant(UUID id, String participantEmail, String role, String userEmail);
  void inviteParticipant(
    UUID id,
    String participantEmail,
    String role,
    String channel,
    String userEmail
  );
  void acceptInvitationToken(UUID id, String token, String userEmail);
  List<PendingInvitation> getPendingInvitations(String userEmail);
  void respondToInvitation(UUID planId, UUID invitationId, boolean accept, String userEmail);

  record PendingInvitation(
    UUID id,
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
  /** [TOP] Participantes por plan, para el ranking del feed. Vacio si no hay ids. */
  Map<UUID, Long> getParticipantCounts(Collection<UUID> planIds);
}
