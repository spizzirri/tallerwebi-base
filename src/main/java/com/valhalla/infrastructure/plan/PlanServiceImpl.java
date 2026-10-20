package com.valhalla.infrastructure.plan;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanInvitation;
import com.valhalla.domain.plan.PlanParticipantCount;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
@Transactional
public class PlanServiceImpl implements PlanService { // NOPMD — intentional: merging invitations (INV) and cloning (CLO) pushes total class complexity to 83 vs the default 80 threshold; every method stays under the methodReportLevel=10 limit.

  private static final int SHORT_CODE_LENGTH = 8;
  private static final int SHORT_CODE_MAX_ATTEMPTS = 10;
  private static final String PARTICIPANT_ROLE = "guest";
  private static final String EDITOR_ROLE = "editor";

  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final PlaceRepository placeRepository;
  private final PlanRatingRepository planRatingRepository;

  @Autowired
  public PlanServiceImpl(
    PlanRepository planRepository,
    UserRepository userRepository,
    PlaceRepository placeRepository,
    PlanRatingRepository planRatingRepository
  ) {
    this.planRepository = planRepository;
    this.userRepository = userRepository;
    this.placeRepository = placeRepository;
    this.planRatingRepository = planRatingRepository;
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
  public Plan getOwnedPlan(UUID id, String ownerEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.getAdministrator() == null || !ownerEmail.equals(plan.getAdministrator().getEmail())) {
      // A plan owned by somebody else answers as missing: "forbidden" would confirm the id exists.
      throw new PlanNotFoundException();
    }
    return plan;
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getEditablePlan(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!plan.canEdit(userEmail)) {
      throw new PlanNotFoundException();
    }
    return plan;
  }

  @Override
  public void changeParticipantRole(
    UUID id,
    String participantEmail,
    String role,
    String userEmail
  ) {
    Plan plan = getEditablePlan(id, userEmail);
    if (plan.isAdministrator(participantEmail)) {
      throw new PlanNotFoundException();
    }
    if (!PARTICIPANT_ROLE.equals(role) && !EDITOR_ROLE.equals(role)) {
      throw new IllegalArgumentException("Unknown participant role");
    }
    User participant = plan
      .getParticipants()
      .stream()
      .filter(user -> user.getEmail().equals(participantEmail))
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
    plan.getEditors().removeIf(user -> user.getEmail().equals(participantEmail));
    if (EDITOR_ROLE.equals(role)) {
      plan.getEditors().add(participant);
    }
    planRepository.save(plan);
  }

  @Override
  public void removeParticipant(UUID id, String participantEmail, String userEmail) {
    Plan plan = getEditablePlan(id, userEmail);
    if (
      plan.isAdministrator(participantEmail) ||
      !plan.isParticipant(participantEmail) ||
      (plan.isEditor(participantEmail) && !plan.isAdministrator(userEmail))
    ) {
      throw new PlanNotFoundException();
    }
    plan.getEditors().removeIf(user -> user.getEmail().equals(participantEmail));
    plan.getParticipants().removeIf(user -> user.getEmail().equals(participantEmail));
    plan
      .getInvitations()
      .removeIf(invitation -> invitation.getRecipient().getEmail().equals(participantEmail));
    planRepository.save(plan);
  }

  @Override
  public void inviteParticipant(UUID id, String participantEmail, String role, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    validateInvitationPermissions(plan, userEmail);
    validateInvitationRole(role);
    User participant = findInvitationRecipient(participantEmail);
    validateInvitationRecipient(plan, participant.getEmail());
    User inviter = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    plan.getInvitations().add(new PlanInvitation(plan, participant, inviter, role));
    planRepository.save(plan);
  }

  private void validateInvitationPermissions(Plan plan, String userEmail) {
    if (userEmail == null || userEmail.isBlank() || !plan.canEdit(userEmail)) {
      // Un plan sin permiso responde como inexistente: no se confirma que el id exista.
      throw new PlanNotFoundException();
    }
  }

  private void validateInvitationRole(String role) {
    if (!PARTICIPANT_ROLE.equals(role) && !EDITOR_ROLE.equals(role)) {
      throw new IllegalArgumentException("Rol de participante inválido.");
    }
  }

  private User findInvitationRecipient(String participantEmail) {
    if (participantEmail == null || participantEmail.isBlank()) {
      throw new IllegalArgumentException("Ingresá un correo electrónico.");
    }
    return userRepository
      .findByEmail(participantEmail.trim())
      .orElseThrow(() ->
        new IllegalArgumentException("No hay una persona registrada con ese correo.")
      );
  }

  private void validateInvitationRecipient(Plan plan, String email) {
    if (plan.isAdministrator(email) || plan.isParticipant(email)) {
      throw new IllegalArgumentException("Esta persona ya forma parte del plan.");
    }
    if (
      plan
        .getInvitations()
        .stream()
        .anyMatch(invitation -> email.equals(invitation.getRecipient().getEmail()))
    ) {
      throw new IllegalArgumentException("Esta persona ya tiene una invitación pendiente.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<PendingInvitation> getPendingInvitations(String userEmail) {
    return planRepository
      .findByInvitationsRecipientEmail(userEmail)
      .stream()
      .flatMap(plan -> plan.getInvitations().stream())
      .filter(invitation -> invitation.getRecipient().getEmail().equals(userEmail))
      .map(invitation -> {
        User inviter = invitation.getInviter();
        String name =
          ((inviter.getFirstName() == null ? "" : inviter.getFirstName()) +
            " " +
            (inviter.getLastName() == null ? "" : inviter.getLastName())).trim();
        return new PendingInvitation(
          invitation.getId(),
          invitation.getPlan().getId(),
          invitation.getPlan().getName(),
          Boolean.TRUE.equals(invitation.getPlan().getIsPublic()),
          name,
          inviter.getEmail(),
          invitation.getRole()
        );
      })
      .sorted(java.util.Comparator.comparing(PendingInvitation::id).reversed())
      .toList();
  }

  @Override
  public void respondToInvitation(
    UUID planId,
    Long invitationId,
    boolean accept,
    String userEmail
  ) {
    Plan plan = planRepository.findById(planId).orElseThrow(PlanNotFoundException::new);
    PlanInvitation invitation = plan
      .getInvitations()
      .stream()
      .filter(item ->
        item.getId().equals(invitationId) && item.getRecipient().getEmail().equals(userEmail)
      )
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
    if (accept && !plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      plan.getParticipants().add(invitation.getRecipient());
      if (EDITOR_ROLE.equals(invitation.getRole())) {
        plan.getEditors().add(invitation.getRecipient());
      }
    }
    plan.getInvitations().remove(invitation);
    planRepository.save(plan);
  }

  @Override
  public Plan updatePlan(UUID id, Plan changes, String ownerEmail) {
    Plan existing = getEditablePlan(id, ownerEmail);
    existing.updateFrom(changes);
    return planRepository.save(existing);
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getParticipatingPlan(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (!plan.getIsPublic() && !plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      throw new PlanNotFoundException();
    }
    // The detail view renders participants and places after this transaction has ended.
    plan.getParticipants().forEach(user -> user.getEmail());
    plan.getEditors().forEach(user -> user.getEmail());
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
  public Plan getPublicPlan(UUID id) {
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
  public void leavePlan(UUID id, String userEmail) {
    Plan plan = getParticipatingPlan(id, userEmail);
    if (plan.isAdministrator(userEmail) || !plan.isParticipant(userEmail)) {
      throw new PlanNotFoundException();
    }
    plan.getEditors().removeIf(user -> user.getEmail().equals(userEmail));
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
  public void deleteOwnedPlan(UUID id, String ownerEmail) {
    getOwnedPlan(id, ownerEmail);
    // [CAL] Las calificaciones no cuelgan de Plan como coleccion: se borran explicito para no
    // violar la FK owner (AC-12). Los planPlaces si viajan con la cascada de la coleccion.
    planRatingRepository.deleteAllByPlanId(id);
    planRepository.deleteById(id);
  }

  @Override
  @Transactional
  public Plan clonePlan(UUID id, String clonerEmail) {
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

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, Long> getParticipantCounts(Collection<UUID> planIds) {
    // `in :planIds` with an empty collection is not valid JPQL: the early return is correctness.
    if (planIds == null || planIds.isEmpty()) {
      return Map.of();
    }
    return planRepository
      .countParticipantsByPlanIds(planIds)
      .stream()
      .collect(Collectors.toMap(PlanParticipantCount::planId, PlanParticipantCount::count));
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
