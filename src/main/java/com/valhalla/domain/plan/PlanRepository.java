package com.valhalla.domain.plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository {
  List<Plan> findAll();
  Optional<Plan> findById(UUID id);
  List<Plan> findByIsPublicTrue();
  List<Plan> findByAdministratorId(Long administratorId);
  List<Plan> findByParticipantsEmail(String email);
  List<Plan> findByInvitationsRecipientEmail(String email);
  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);
  Plan save(Plan plan);
  void deleteById(UUID id);
}
