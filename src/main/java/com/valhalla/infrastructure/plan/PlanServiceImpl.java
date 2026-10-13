package com.valhalla.infrastructure.plan;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
@Transactional
public class PlanServiceImpl implements PlanService {

  private static final int SHORT_CODE_LENGTH = 8;
  private static final int SHORT_CODE_MAX_ATTEMPTS = 10;

  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final PlaceRepository placeRepository;

  @Autowired
  public PlanServiceImpl(
    PlanRepository planRepository,
    UserRepository userRepository,
    PlaceRepository placeRepository
  ) {
    this.planRepository = planRepository;
    this.userRepository = userRepository;
    this.placeRepository = placeRepository;
  }

  @Override
  public Plan createPlan(Plan plan, String ownerEmail) {
    User owner = userRepository.findByEmail(ownerEmail).orElseThrow(UserNotFoundException::new);
    for (PlanPlace entry : plan.getPlanPlaces()) {
      entry.setPlace(
        placeRepository
          .findById(entry.getPlace().getId())
          .orElseThrow(() -> new IllegalArgumentException("Unknown place"))
      );
    }
    plan.setAdministrator(owner);
    // Always generated: the share code is the backend's, the form never asks for it.
    plan.setShortCode(generateUniqueShortCode());
    return planRepository.save(plan);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Plan> getPlansByUserEmail(String ownerEmail) {
    return userRepository
      .findByEmail(ownerEmail)
      .map(user -> planRepository.findByAdministratorId(user.getId()))
      .orElse(List.of());
  }

  /**
   * [PPV] Listado publico: solo {@code isPublic = true}, sin orden garantizado y sin escribir nada.
   * El filtro vive en la query del repositorio, no aca — la vista nunca ve un plan privado.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Plan> getPublicPlans() {
    return planRepository.findByIsPublicTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getOwnedPlan(Long id, String ownerEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.getAdministrator() == null || !ownerEmail.equals(plan.getAdministrator().getEmail())) {
      // A plan owned by somebody else answers as missing: "forbidden" would confirm the id exists.
      throw new PlanNotFoundException();
    }
    return plan;
  }

  @Override
  public Plan updatePlan(Long id, Plan changes, String ownerEmail) {
    Plan existing = getOwnedPlan(id, ownerEmail);
    existing.updateFrom(changes);
    return planRepository.save(existing);
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getParticipatingPlan(Long id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      throw new PlanNotFoundException();
    }
    // The detail view renders participants and places after this transaction has ended.
    plan.getParticipants().forEach(user -> user.getEmail());
    plan.getPlanPlaces().forEach(entry -> entry.getPlace().getName());
    return plan;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Plan> getParticipantPlans(String userEmail) {
    return planRepository.findByParticipantsEmail(userEmail);
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getPublicPlan(Long id) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!plan.getIsPublic()) {
      // Privado e inexistente responden igual: la vista publica no confirma que el plan exista.
      throw new PlanNotFoundException();
    }
    // La vista y los CTAs leen administrador, lugares y participantes despues de cerrar la
    // transaccion (isParticipant se consulta en el controller): hay que calentarlos aca.
    if (plan.getAdministrator() != null) {
      plan.getAdministrator().getEmail();
    }
    plan.getPlanPlaces().forEach(entry -> entry.getPlace().getName());
    plan.getParticipants().forEach(user -> user.getEmail());
    return plan;
  }

  @Override
  public void leavePlan(Long id, String userEmail) {
    Plan plan = getParticipatingPlan(id, userEmail);
    if (plan.isAdministrator(userEmail)) {
      throw new PlanNotFoundException();
    }
    plan.getParticipants().removeIf(user -> user.getEmail().equals(userEmail));
    planRepository.save(plan);
  }

  @Override
  public Plan joinPlan(String shortCode, String userEmail) {
    if (shortCode == null || shortCode.isBlank()) {
      throw new PlanNotFoundException();
    }
    Plan plan = planRepository
      .findByShortCode(shortCode.trim().toUpperCase(Locale.ROOT))
      .orElseThrow(PlanNotFoundException::new);
    User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    if (!plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      plan.getParticipants().add(user);
      planRepository.save(plan);
    }
    return plan;
  }

  @Override
  public void deleteOwnedPlan(Long id, String ownerEmail) {
    getOwnedPlan(id, ownerEmail);
    planRepository.deleteById(id);
  }

  @Override
  @Transactional
  public Plan clonePlan(Long id, String clonerEmail) {
    User cloner = userRepository.findByEmail(clonerEmail).orElseThrow(UserNotFoundException::new);
    Plan source = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    boolean accessible =
      source.getIsPublic() ||
      source.isAdministrator(clonerEmail) ||
      source.isParticipant(clonerEmail);
    if (!accessible) {
      // Same path as a missing id: the answer must not confirm the private plan exists.
      throw new PlanNotFoundException();
    }

    Plan copy = new Plan();
    copy.setName(source.getName());
    copy.setDescription(source.getDescription());
    copy.setEventDate(source.getEventDate());
    copy.setEventTime(source.getEventTime());
    // The copy is born private: publishing is the new owner's decision (AC-04).
    copy.setIsPublic(false);
    copy.setAdministrator(cloner);
    // Participants stay empty (AC-07); the shortCode is generated below (AC-03).

    int order = 1;
    for (PlanPlace entry : source.getPlanPlaces()) {
      PlanPlace entryCopy = new PlanPlace();
      entryCopy.setPlace(entry.getPlace()); // same persisted Place entity, not a copy (AC-06)
      entryCopy.setDescription(entry.getDescription());
      entryCopy.setVisitDate(entry.getVisitDate());
      entryCopy.setVisitTime(entry.getVisitTime());
      entryCopy.setSortOrder(order);
      order++;
      copy.addPlanPlace(entryCopy);
    }

    copy.setShortCode(generateUniqueShortCode());
    return planRepository.save(copy);
  }

  /**
   * The unique constraint on {@code shortCode} is the real guard; this loop only narrows the race
   * window down to that constraint. Ten collisions on 8 characters are already far past the odds,
   * so running out of attempts means something is broken, not unlucky.
   */
  private String generateUniqueShortCode() {
    for (int attempt = 0; attempt < SHORT_CODE_MAX_ATTEMPTS; attempt++) {
      String candidate = UUID
        .randomUUID()
        .toString()
        .substring(0, SHORT_CODE_LENGTH)
        .toUpperCase(Locale.ROOT);
      if (!planRepository.existsByShortCode(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not generate a unique plan short code");
  }
}
