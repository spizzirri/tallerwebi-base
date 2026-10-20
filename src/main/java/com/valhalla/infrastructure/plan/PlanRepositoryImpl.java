package com.valhalla.infrastructure.plan;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanParticipantCount;
import com.valhalla.domain.plan.PlanRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PlanRepositoryImpl implements PlanRepository {

  private final JpaPlanRepository jpaPlanRepository;

  @Autowired
  public PlanRepositoryImpl(JpaPlanRepository jpaPlanRepository) {
    this.jpaPlanRepository = jpaPlanRepository;
  }

  @Override
  public List<Plan> findAll() {
    return jpaPlanRepository.findAll();
  }

  @Override
  public Optional<Plan> findById(UUID id) {
    return jpaPlanRepository.findById(id);
  }

  @Override
  public List<Plan> findByAdministratorId(Long administratorId) {
    return jpaPlanRepository.findByAdministratorId(administratorId);
  }

  @Override
  public Optional<Plan> findByShortCode(String shortCode) {
    return jpaPlanRepository.findByShortCode(shortCode);
  }

  @Override
  public List<Plan> findByIsPublicTrue() {
    return jpaPlanRepository.findByIsPublicTrue();
  }

  @Override
  public List<Plan> findByParticipantsEmail(String email) {
    return jpaPlanRepository.findByParticipantsEmail(email);
  }

  @Override
  public boolean existsByShortCode(String shortCode) {
    return jpaPlanRepository.existsByShortCode(shortCode);
  }

  @Override
  public List<Plan> findByInvitationsRecipientEmail(String email) {
    return jpaPlanRepository.findByInvitationsRecipientEmail(email);
  }

  @Override
  public Plan save(Plan plan) {
    return jpaPlanRepository.save(plan);
  }

  @Override
  public void deleteById(UUID id) {
    jpaPlanRepository.deleteById(id);
  }

  @Override
  public List<PlanParticipantCount> countParticipantsByPlanIds(Collection<UUID> planIds) {
    return jpaPlanRepository.countParticipantsByPlanIds(planIds);
  }
}
