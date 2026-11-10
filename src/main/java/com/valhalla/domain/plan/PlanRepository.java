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
  /**
   * [PSHARE] Los planes PUBLICOS que incluyen un lugar (join por plan_places), para la ficha
   * /places/{id}. Explicita como findByIsPublicTrue: el atributo isPublic resuelve ambiguo en
   * derived queries. Orden por nombre para que la ficha muestre la lista estable.
   */
  List<Plan> findByIsPublicTrueAndPlaceId(UUID placeId);
  List<Plan> findByAdministratorId(UUID administratorId);
  List<Plan> findByParticipantsEmail(String email);
  List<Plan> findByBannedUsersEmail(String email);
  List<Plan> findByInvitationsRecipientEmail(String email);
  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);
  Plan save(Plan plan);
  void deleteById(UUID id);
  List<PlanParticipantCount> countParticipantsByPlanIds(Collection<UUID> planIds);
}
