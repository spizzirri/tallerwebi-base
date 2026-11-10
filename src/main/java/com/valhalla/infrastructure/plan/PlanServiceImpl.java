package com.valhalla.infrastructure.plan;

import com.valhalla.domain.exception.PlanBannedException;
import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.notification.NotificationService;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanDiscovery;
import com.valhalla.domain.plan.PlanInvitation;
import com.valhalla.domain.plan.PlanJoinRequest;
import com.valhalla.domain.plan.PlanParticipantCount;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanScheduler;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRatingRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalTime;
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
  private static final String EMAIL_CHANNEL = "email";
  // [PABM] Canal de la invitacion que hace el admin: dentro de la app, como el default del usuario.
  private static final String INTERNAL_CHANNEL = "app";
  private static final String EDITOR_ROLE = "editor";
  // [PABM] Usuario sistema dueño de los planes que se crean desde el admin (AC-03). El email vive
  // en el dominio porque el ranking tambien lo necesita para priorizar esos planes.
  private static final String APP_USER_EMAIL = PlanDiscovery.PLANIT_EMAIL;

  @Autowired
  private com.valhalla.domain.plan.EmailNotifier emailNotifier;

  private final PlanRepository planRepository;
  private final UserRepository userRepository;
  private final PlaceRepository placeRepository;
  private final PlanRatingRepository planRatingRepository;
  private final NotificationService notificationService;

  @Override
  @Transactional(readOnly = true)
  public Plan getSharedPlan(UUID id) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.getIsPublic()) {
      return getPublicPlan(id);
    }
    plan.getParticipants().forEach(user -> user.getEmail());
    plan.getEditors().forEach(user -> user.getEmail());
    plan.getJoinRequests().forEach(request -> request.getRequester().getEmail());
    plan.getBannedUsers().forEach(user -> user.getEmail());
    return plan;
  }

  @Override
  public void requestToJoin(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.isBanned(userEmail)) {
      throw new PlanBannedException();
    }
    User requester = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    if (plan.isAdministrator(userEmail) || plan.isParticipant(userEmail)) {
      return;
    }
    // [SEC] Un plan privado siempre acepta solicitudes: la visibilidad gobierna el acceso, no
    // una configuración por plan. Un plan público se une por short code.
    if (plan.getIsPublic()) {
      throw new PlanNotFoundException();
    }
    addJoinRequest(plan, requester);
  }

  private void addJoinRequest(Plan plan, User requester) {
    if (!plan.hasPendingJoinRequest(requester.getEmail())) {
      plan.getJoinRequests().add(new PlanJoinRequest(plan, requester));
      planRepository.save(plan);
      // [NOTIF] AC-15: la solicitud es un pendiente del dueno, vive tambien en la campanita. La
      // pongo aca y no en requestToJoin porque joinPlan con codigo a un plan privado pasa por el
      // mismo camino. La solicitud repetida corta arriba y no notifica de nuevo.
      notificationService.notifyJoinRequest(
        requester,
        plan.getAdministrator(),
        plan.getId(),
        plan.getName()
      );
    }
  }

  @Override
  public void respondToJoinRequest(UUID id, UUID requestId, String action, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    requireRequestManager(plan, userEmail);
    if (!"accept".equals(action) && !"reject".equals(action)) {
      throw new IllegalArgumentException("Respuesta de solicitud inválida.");
    }
    boolean accepted = "accept".equals(action);
    PlanJoinRequest request = plan
      .getJoinRequests()
      .stream()
      .filter(item -> item.getId().equals(requestId))
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
    User requester = request.getRequester();
    validateJoinResponse(plan, requester, action);
    if (
      accepted &&
      !plan.isAdministrator(requester.getEmail()) &&
      !plan.isParticipant(requester.getEmail())
    ) {
      plan.getParticipants().add(requester);
      plan.getInvitations().removeIf(item -> item.getRecipient().getId().equals(requester.getId()));
      // [NOTIF] AC-02: aceptar una solicitud convierte al solicitante en participante del plan.
      notificationService.notifyNewParticipant(
        requester,
        plan.getAdministrator(),
        plan.getId(),
        plan.getName()
      );
    }
    plan.getJoinRequests().remove(request);
    planRepository.save(plan);
    // [NOTIF] AC-16: la respuesta cierra el pendiente del solicitante en los dos sentidos.
    notificationService.notifyJoinRequestResponse(
      plan.getAdministrator(),
      requester,
      plan.getId(),
      plan.getName(),
      accepted
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<PendingJoinRequest> getPendingJoinRequests(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    requireRequestManager(plan, userEmail);
    return plan
      .getJoinRequests()
      .stream()
      .map(request -> {
        User user = request.getRequester();
        String name =
          ((user.getFirstName() == null ? "" : user.getFirstName()) +
            " " +
            (user.getLastName() == null ? "" : user.getLastName())).trim();
        return new PendingJoinRequest(
          request.getId(),
          user.getId(),
          name.isEmpty() ? "Ver perfil" : name
        );
      })
      .toList();
  }

  @Override
  public void updateSettings(
    UUID id,
    PlanPermission handleRequests,
    PlanPermission inviteInternal,
    PlanPermission inviteByEmail,
    String userEmail
  ) {
    Plan plan = getOwnedPlan(id, userEmail);
    plan.getSettings().setHandleRequests(handleRequests);
    plan.getSettings().setInviteInternal(inviteInternal);
    plan.getSettings().setInviteByEmail(inviteByEmail);
    planRepository.save(plan);
  }

  private void validateJoinResponse(Plan plan, User requester, String action) {
    if ("accept".equals(action) && plan.isBanned(requester.getEmail())) {
      throw new PlanBannedException();
    }
  }

  private void requireRequestManager(Plan plan, String userEmail) {
    if (!plan.canHandleRequests(userEmail)) {
      throw new PlanNotFoundException();
    }
  }

  @Autowired
  public PlanServiceImpl(
    PlanRepository planRepository,
    UserRepository userRepository,
    PlaceRepository placeRepository,
    PlanRatingRepository planRatingRepository,
    NotificationService notificationService
  ) {
    this.planRepository = planRepository;
    this.userRepository = userRepository;
    this.placeRepository = placeRepository;
    this.planRatingRepository = planRatingRepository;
    this.notificationService = notificationService;
  }

  @Override
  public Plan createPlan(Plan plan, String ownerEmail) {
    User owner = userRepository.findByEmail(ownerEmail).orElseThrow(UserNotFoundException::new);
    return persistNewPlan(plan, owner);
  }

  /**
   * [PABM] Alta compartida por el usuario y el admin: los lugares se resuelven y el horario se
   * deriva igual en los dos caminos, sea cual sea el dueño.
   */
  private Plan persistNewPlan(Plan plan, User owner) {
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
    // [SCH] El horario se deriva con los lugares ya resueltos: la calculadora necesita categoria y
    // coordenadas reales para estimar duraciones y traslados.
    PlanScheduler.apply(plan);
    Plan saved = planRepository.save(plan);
    // [NOTIF] AC-03: publicar al crear avisa a los seguidores del autor; un plan privado nace
    // sin notificar a nadie.
    if (Boolean.TRUE.equals(saved.getIsPublic())) {
      notificationService.notifyNewPlan(owner, saved.getId(), saved.getName());
    }
    return saved;
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
   *
   * <p>[PABM] AC-12: los planes desactivados tampoco entran al feed ni a /explore.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Plan> getPublicPlans() {
    List<Plan> plans = planRepository
      .findByIsPublicTrue()
      .stream()
      .filter(Plan::getActive)
      .toList();
    plans.forEach(plan -> plan.getBannedUsers().forEach(user -> user.getEmail()));
    return plans;
  }

  /**
   * [FOLLOW] Planes publicos de los autores seguidos: el conjunto lo elige el follow, el orden y
   * los filtros siguen siendo de {@code PlanDiscovery}, asi que la query no ordena (AC-08, AC-15).
   *
   * <p>[PABM] AC-12: el scope Siguiendo tampoco muestra planes desactivados.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Plan> getPublicPlansByAdministrators(Collection<UUID> administratorIds) {
    // `in :administratorIds` con una coleccion vacia no es JPQL valido: el early return es correcto.
    if (administratorIds == null || administratorIds.isEmpty()) {
      return List.of();
    }
    List<Plan> plans = planRepository
      .findByIsPublicTrueAndAdministratorIdIn(administratorIds)
      .stream()
      .filter(Plan::getActive)
      .toList();
    plans.forEach(plan -> plan.getBannedUsers().forEach(user -> user.getEmail()));
    return plans;
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
  public void updateBudget(UUID id, com.valhalla.domain.plan.PlanBudget budget, String userEmail) {
    Plan plan = getEditablePlan(id, userEmail);
    plan.setBudget(
      new com.valhalla.domain.plan.PlanBudget(
        budget.getMinimum(),
        budget.getMaximum(),
        budget.getNotes()
      )
    );
    planRepository.save(plan);
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getEditablePlan(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.isBanned(userEmail)) {
      throw new PlanBannedException();
    }
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
    // Solo el dueño cambia roles existentes: los editores conservan la invitación, no la
    // reasignación. getOwnedPlan responde como inexistente para el resto (misma política que
    // deleteOwnedPlan), así que un editor no puede distinguir un plan ajeno de uno inexistente.
    applyParticipantRole(getOwnedPlan(id, userEmail), participantEmail, role);
  }

  /** [PABM] AC-09: el admin reasigna roles de cualquier plan sin ser el dueño. */
  @Override
  public void changeParticipantRoleAny(UUID id, String participantEmail, String role) {
    applyParticipantRole(getAnyPlan(id), participantEmail, role);
  }

  private void applyParticipantRole(Plan plan, String participantEmail, String role) {
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
    boolean wasEditor = plan.isEditor(participantEmail);
    boolean becomesEditor = EDITOR_ROLE.equals(role);
    plan.getEditors().removeIf(user -> user.getEmail().equals(participantEmail));
    if (becomesEditor) {
      plan.getEditors().add(participant);
    }
    planRepository.save(plan);
    if (wasEditor != becomesEditor) {
      notificationService.notifyEditorRoleChanged(
        plan.getAdministrator(),
        participant,
        plan.getId(),
        plan.getName(),
        becomesEditor
      );
    }
  }

  @Override
  public void removeParticipant(UUID id, String participantEmail, String userEmail) {
    moderateParticipant(id, participantEmail, userEmail, false);
  }

  /**
   * [PABM] AC-10: el admin saca participantes de cualquier plan, guest o editor.
   *
   * <p>[NOTIF] La notificación se firma con el admin real, no con el dueño: el aviso le dice al
   * expulsado quién lo sacó. Por eso el email llega como parámetro y no se infiere del plan.
   */
  @Override
  public void removeParticipantAny(UUID id, String participantEmail, String adminEmail) {
    Plan plan = getAnyPlan(id);
    requireRemovableParticipant(plan, participantEmail);
    User admin = userRepository.findByEmail(adminEmail).orElseThrow(UserNotFoundException::new);
    ejectParticipant(plan, admin, participantEmail, false);
  }

  /** [PABM] AC-20: el admin invita con la autoridad del dueño; el resto de las reglas son las mismas. */
  @Override
  public void inviteParticipantAny(UUID id, String participantEmail, String role) {
    Plan plan = getAnyPlan(id);
    if (plan.getAdministrator() == null) {
      throw new PlanNotFoundException();
    }
    inviteParticipant(
      id,
      participantEmail,
      role,
      INTERNAL_CHANNEL,
      plan.getAdministrator().getEmail()
    );
  }

  @Override
  public void banParticipant(UUID id, String participantEmail, String userEmail) {
    moderateParticipant(id, participantEmail, userEmail, true);
  }

  @Override
  public void unbanParticipant(UUID id, String participantEmail, String userEmail) {
    Plan plan = getEditablePlan(id, userEmail);
    if (!plan.getBannedUsers().removeIf(user -> user.getEmail().equals(participantEmail))) {
      throw new PlanNotFoundException();
    }
    planRepository.save(plan);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BannedParticipant> getBannedParticipants(UUID id, String userEmail) {
    return getEditablePlan(id, userEmail)
      .getBannedUsers()
      .stream()
      .sorted(java.util.Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER))
      .map(user -> new BannedParticipant(user.getEmail(), user.getFirstName(), user.getLastName()))
      .toList();
  }

  private void moderateParticipant(
    UUID id,
    String participantEmail,
    String userEmail,
    boolean ban
  ) {
    Plan plan = getEditablePlan(id, userEmail);
    // Solo el dueño expulsa a un editor: un editor no saca a otro editor.
    if (plan.isEditor(participantEmail) && !plan.isAdministrator(userEmail)) {
      throw new PlanNotFoundException();
    }
    requireRemovableParticipant(plan, participantEmail);
    ejectParticipant(plan, findPlanActor(plan, userEmail), participantEmail, ban);
  }

  /** [PABM] Ni al dueño ni a un ajeno al plan: los guardas de la expulsión, ahora compartidos. */
  private void requireRemovableParticipant(Plan plan, String participantEmail) {
    if (plan.isAdministrator(participantEmail) || !plan.isParticipant(participantEmail)) {
      throw new PlanNotFoundException();
    }
  }

  /**
   * [NOTIF] Saca al participante y avisa al afectado. El {@code actor} es quien ejecuta la
   * expulsión —el dueño/editor para {@code removeParticipant}, el admin para
   * {@code removeParticipantAny}—, no el dueño del plan: la notificación dice quién lo hizo.
   */
  private void ejectParticipant(Plan plan, User actor, String participantEmail, boolean ban) {
    User participant = plan
      .getParticipants()
      .stream()
      .filter(user -> user.getEmail().equals(participantEmail))
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
    if (ban) {
      plan.getBannedUsers().add(participant);
    }
    plan.getEditors().removeIf(user -> user.getEmail().equals(participantEmail));
    plan.getParticipants().removeIf(user -> user.getEmail().equals(participantEmail));
    plan
      .getInvitations()
      .removeIf(invitation -> invitation.getRecipient().getEmail().equals(participantEmail));
    planRepository.save(plan);
    if (ban) {
      notificationService.notifyParticipantBanned(actor, participant, plan.getName());
    } else {
      notificationService.notifyParticipantRemoved(actor, participant, plan.getName());
    }
  }

  private User findPlanActor(Plan plan, String userEmail) {
    if (plan.isAdministrator(userEmail)) {
      return plan.getAdministrator();
    }
    return plan
      .getEditors()
      .stream()
      .filter(user -> user.getEmail().equals(userEmail))
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
  }

  @Override
  public void inviteParticipant(UUID id, String participantEmail, String role, String userEmail) {
    inviteParticipant(id, participantEmail, role, INTERNAL_CHANNEL, userEmail);
  }

  @Override
  public void inviteParticipant(
    UUID id,
    String participantEmail,
    String role,
    String channel,
    String userEmail
  ) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    validateInvitationChannel(plan, channel, role, userEmail);
    validateInvitationRole(role);
    User participant = findInvitationRecipient(participantEmail);
    validateInvitationRecipient(plan, participant.getEmail());
    User inviter = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    PlanInvitation invitation = new PlanInvitation(plan, participant, inviter, role);
    plan.getInvitations().add(invitation);
    planRepository.save(plan);
    if (EMAIL_CHANNEL.equals(channel)) {
      emailNotifier.sendInvitation(participant.getEmail(), plan.getId(), invitation.getToken());
    }
    // [NOTIF] La campanita también avisa al invitado: el mail puede no llegar nunca (no hay SMTP
    // en dev ni en el TP) y la lista de pendientes solo se ve si ya sabés dónde mirar.
    notificationService.notifyInvitation(inviter, participant, plan.getId(), plan.getName());
  }

  private void validateInvitationChannel(Plan plan, String channel, String role, String userEmail) {
    if (!INTERNAL_CHANNEL.equals(channel) && !EMAIL_CHANNEL.equals(channel)) {
      throw new IllegalArgumentException("Canal de invitación inválido.");
    }
    boolean allowed = INTERNAL_CHANNEL.equals(channel)
      ? plan.canInviteInternal(userEmail)
      : plan.canInviteByEmail(userEmail);
    if (!allowed || (EDITOR_ROLE.equals(role) && !plan.canEdit(userEmail))) {
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
    if (plan.isBanned(email)) {
      throw new IllegalArgumentException("Esta persona está baneada del plan.");
    }
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
    UUID invitationId,
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
    if (accept && plan.isBanned(userEmail)) {
      throw new PlanBannedException();
    }
    if (accept && !plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      plan.getParticipants().add(invitation.getRecipient());
      plan.getJoinRequests().removeIf(item -> item.getRequester().getEmail().equals(userEmail));
      if (EDITOR_ROLE.equals(invitation.getRole())) {
        plan.getEditors().add(invitation.getRecipient());
      }
      // [NOTIF] AC-02: aceptar una invitacion tambien suma un participante al plan del dueno.
      notificationService.notifyNewParticipant(
        invitation.getRecipient(),
        plan.getAdministrator(),
        plan.getId(),
        plan.getName()
      );
    }
    plan.getInvitations().remove(invitation);
    planRepository.save(plan);
  }

  @Override
  public void acceptInvitationToken(UUID id, String token, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    PlanInvitation invitation = plan
      .getInvitations()
      .stream()
      .filter(item ->
        token.equals(item.getToken()) && item.getRecipient().getEmail().equals(userEmail)
      )
      .findFirst()
      .orElseThrow(PlanNotFoundException::new);
    respondToInvitation(id, invitation.getId(), true, userEmail);
  }

  @Override
  public Plan updatePlan(UUID id, Plan changes, String ownerEmail) {
    Plan existing = getEditablePlan(id, ownerEmail);
    boolean wasPublic = Boolean.TRUE.equals(existing.getIsPublic());
    existing.updateFrom(changes);
    // [SCH] Cambiar la fecha, la hora de inicio, el fin de dia o el modo recalcula el itinerario
    // entero: el horario es una funcion de esa configuracion, no un dato independiente.
    PlanScheduler.apply(existing);
    Plan saved = planRepository.save(existing);
    // [NOTIF] AC-03: publicar un plan existente es el "cambio relevante" que se notifica a los
    // seguidores; editar un plan ya publico no genera ruido.
    if (!wasPublic && Boolean.TRUE.equals(saved.getIsPublic())) {
      notificationService.notifyNewPlan(saved.getAdministrator(), saved.getId(), saved.getName());
    }
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Plan getParticipatingPlan(UUID id, String userEmail) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    if (plan.isBanned(userEmail)) {
      throw new PlanBannedException();
    }
    // [PABM] AC-13/AC-14: un plan desactivado sigue abierto solo para su dueño y sus
    // participantes, igual que los privados; para un extraño respondemos como si no existiera.
    boolean member = plan.isAdministrator(userEmail) || plan.isParticipant(userEmail);
    if (!member && (!plan.getActive() || !plan.getIsPublic())) {
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
  public List<BannedPlan> getBannedPlans(String userEmail) {
    return planRepository
      .findByBannedUsersEmail(userEmail)
      .stream()
      .map(plan -> new BannedPlan(plan.getName()))
      .toList();
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
    plan.getBannedUsers().forEach(user -> user.getEmail());
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
    if (plan.isBanned(userEmail)) {
      throw new PlanBannedException();
    }
    User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    if (!plan.isAdministrator(userEmail) && !plan.isParticipant(userEmail)) {
      if (plan.getIsPublic()) {
        plan.getParticipants().add(user);
        plan.getJoinRequests().removeIf(item -> item.getRequester().getEmail().equals(userEmail));
        planRepository.save(plan);
        // [NOTIF] AC-02: unirse por short code suma el participante y avisa al dueno.
        notificationService.notifyNewParticipant(
          user,
          plan.getAdministrator(),
          plan.getId(),
          plan.getName()
        );
      } else {
        addJoinRequest(plan, user);
      }
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
    if (source.isBanned(clonerEmail)) {
      throw new PlanBannedException();
    }
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
    copy.setBudget(
      new com.valhalla.domain.plan.PlanBudget(
        source.getBudget().getMinimum(),
        source.getBudget().getMaximum(),
        source.getBudget().getNotes()
      )
    );
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

  /** [PABM] Listado completo del ABM: todos los planes, sin filtros de acceso (AC-01). */
  @Override
  @Transactional(readOnly = true)
  public List<Plan> getAllPlans() {
    List<Plan> plans = planRepository.findAll();
    // El listado pinta la cantidad de participantes despues de cerrar la transaccion.
    plans.forEach(plan -> plan.getParticipants().forEach(user -> user.getEmail()));
    return plans;
  }

  /**
   * [PABM] Cualquier plan para el admin: sin chequeo de ownership (AC-04, AC-08). Calienta las
   * colecciones lazy que la vista de detalle lee despues de cerrar la transaccion, igual que
   * {@link #getSharedPlan(UUID)}.
   */
  @Override
  @Transactional(readOnly = true)
  public Plan getAnyPlan(UUID id) {
    Plan plan = planRepository.findById(id).orElseThrow(PlanNotFoundException::new);
    plan.getParticipants().forEach(user -> user.getEmail());
    plan.getEditors().forEach(user -> user.getEmail());
    // Las invitaciones pendientes se listan en el edit, fuera de la transaccion.
    plan.getInvitations().forEach(invitation -> invitation.getRecipient().getEmail());
    plan.getPlanPlaces().forEach(entry -> entry.getPlace().getName());
    if (plan.getAdministrator() != null) {
      plan.getAdministrator().getEmail();
    }
    return plan;
  }

  /** [PABM] El alta admin siempre nace a nombre del usuario sistema y activa (AC-03, AC-16). */
  @Override
  public Plan createAdminPlan(Plan plan) {
    plan.setActive(true);
    return persistNewPlan(plan, getOrCreateAppUser());
  }

  @Override
  public Plan updateAnyPlan(UUID id, Plan changes) {
    Plan plan = getAnyPlan(id);
    // [PABM] El admin no edita la ventana del dia: el valor guardado (23:00 por defecto) se conserva
    // aunque el objeto cambios no lo traiga.
    LocalTime dayEnd = plan.getDayEndTime();
    plan.updateFrom(changes);
    plan.setDayEndTime(dayEnd);
    // [SCH] Mismo motivo que updatePlan: cambiar fecha u horario recalcula el itinerario.
    PlanScheduler.apply(plan);
    return planRepository.save(plan);
  }

  /** [PABM] Baja admin: ratings primero (FK owner), el resto viaja con las cascadas (AC-07). */
  @Override
  public void deleteAnyPlan(UUID id) {
    Plan plan = getAnyPlan(id);
    planRatingRepository.deleteAllByPlanId(plan.getId());
    planRepository.deleteById(plan.getId());
  }

  @Override
  public void togglePlanActive(UUID id, boolean active) {
    Plan plan = getAnyPlan(id);
    plan.setActive(active);
    planRepository.save(plan);
  }

  /** [PABM] AC-11: settings completos desde el admin, autoSchedule incluido. */
  @Override
  public void updateAnySettings(
    UUID id,
    PlanPermission handleRequests,
    PlanPermission inviteInternal,
    PlanPermission inviteByEmail,
    boolean autoSchedule
  ) {
    Plan plan = getAnyPlan(id);
    plan.getSettings().setHandleRequests(handleRequests);
    plan.getSettings().setInviteInternal(inviteInternal);
    plan.getSettings().setInviteByEmail(inviteByEmail);
    plan.getSettings().setAutoSchedule(autoSchedule);
    planRepository.save(plan);
  }

  /**
   * [PABM] Usuario sistema dueño de los planes creados desde el admin (AC-03): si no existe se
   * crea aca. Rol ADMIN y contraseña no loginable porque no tiene cuenta de acceso (AC-18).
   */
  private User getOrCreateAppUser() {
    return userRepository
      .findByEmail(APP_USER_EMAIL)
      .orElseGet(() -> {
        User appUser = new User();
        appUser.setEmail(APP_USER_EMAIL);
        appUser.setPassword("N/A");
        appUser.setFirstName("PlanIt");
        appUser.setLastName("App");
        appUser.setRole("ADMIN");
        appUser.setActive(true);
        userRepository.save(appUser);
        return appUser;
      });
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
