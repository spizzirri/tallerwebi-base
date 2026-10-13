package com.valhalla.domain.plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
  List<Plan> findAll();
  Optional<Plan> findById(Long id);
  List<Plan> findByIsPublicTrue();
  List<Plan> findByAdministratorId(Long administratorId);
  List<Plan> findByParticipantsEmail(String email);
  Optional<Plan> findByShortCode(String shortCode);
  boolean existsByShortCode(String shortCode);
  Plan save(Plan plan);
  void deleteById(Long id);
}
