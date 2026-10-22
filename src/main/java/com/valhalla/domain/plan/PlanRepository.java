package com.valhalla.domain.plan;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository {
  List<Plan> findAll();
  Optional<Plan> findById(UUID id);
  List<Plan> findByIsPublicTrue();
  /** [FOLLOW] Planes publicos de un conjunto de autores: el scope Siguiendo en una sola query. */
  List<Plan> findByIsPublicTrueAndAdministratorIdIn(Collection<UUID> administratorIds);
  List<Plan> findByAdministratorId(UUID administratorId);
  List<Plan> findByParticipantsEmail(String email);
  List<Plan> findByInvitationsRecipientEmail(String email);
  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);
  Plan save(Plan plan);
  void deleteById(UUID id);
  List<PlanParticipantCount> countParticipantsByPlanIds(Collection<UUID> planIds);
}
