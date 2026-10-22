package com.valhalla.presentation.plan;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.User;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/plans")
public class PlanController {

  private static final String VIEW_PLANS_LIST = "pages/plans/list";
  private static final String VIEW_PLAN_DETAIL = "pages/plans/detail";
  private static final String VIEW_PUBLIC_PLAN = "pages/plans/public";
  private static final String PUBLIC_SUFFIX = "/public";
  private static final String REDIRECT_PLANS = "redirect:/plans";
  private static final String REDIRECT_PLAN_DETAIL = "redirect:/plans/";
  private static final String REDIRECT_EXPLORE = "redirect:/explore";
  private static final String ATTR_PLANS = "plans";
  private static final String ATTR_PLAN = "plan";

  private final PlanService planService;
  private final PlanPlaceService planPlaceService;
  private final PlanRatingService planRatingService;
  private final PlanBookmarkService planBookmarkService;

  @Autowired
  public PlanController(
    PlanService planService,
    PlanPlaceService planPlaceService,
    PlanRatingService planRatingService,
    PlanBookmarkService planBookmarkService
  ) {
    this.planService = planService;
    this.planPlaceService = planPlaceService;
    this.planRatingService = planRatingService;
    this.planBookmarkService = planBookmarkService;
  }

  @GetMapping
  public ModelAndView listPlans(Authentication authentication) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLANS, planService.getPlansByUserEmail(authentication.getName()));
    model.put("participantPlans", planService.getParticipantPlans(authentication.getName()));
    model.put("pendingInvitations", planService.getPendingInvitations(authentication.getName()));
    return new ModelAndView(VIEW_PLANS_LIST, model);
  }

  /**
   * [PPV] El feed de planes publicos se mudó a {@code GET /for-you} (ver {@link ForYouController}),
   * fuera de /plans porque ahora es una ruta de primer nivel visible con y sin sesion.
   */

  @GetMapping("/{id}")
  public ModelAndView showPlan(@PathVariable UUID id, Authentication authentication) {
    Map<String, Object> model = new ModelMap();
    String userEmail = authentication == null ? null : authentication.getName();
    Plan plan = planService.getParticipatingPlan(id, userEmail);
    model.put(ATTR_PLAN, plan);
    model.put("canEditPlan", plan.canEdit(userEmail));
    model.put("isPlanAdministrator", plan.isAdministrator(userEmail));
    model.put("isPlanParticipant", plan.isParticipant(userEmail));
    model.put("canInviteInternal", plan.canInviteInternal(userEmail));
    model.put("canInviteByEmail", plan.canInviteByEmail(userEmail));
    model.put("canInvite", plan.canInviteInternal(userEmail) || plan.canInviteByEmail(userEmail));
    model.put(
      "canShareLink",
      plan.canShareLink(userEmail) && (plan.getIsPublic() || plan.getSettings().isSharingEnabled())
    );
    model.put("canHandleRequests", plan.canHandleRequests(userEmail));
    if (plan.canHandleRequests(userEmail)) {
      model.put("joinRequests", planService.getPendingJoinRequests(id, userEmail));
    }
    model.put(
      "participants",
      Stream
        .concat(Stream.ofNullable(plan.getAdministrator()), plan.getParticipants().stream())
        .collect(
          java.util.stream.Collectors.toMap(User::getEmail, user -> user, (first, second) -> first)
        )
        .values()
        .stream()
        .sorted(Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER))
        .toList()
    );
    model.put(
      "participantEmails",
      Stream
        .concat(
          Stream.ofNullable(plan.getAdministrator()).map(User::getEmail),
          plan.getParticipants().stream().map(User::getEmail)
        )
        .distinct()
        .sorted(String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder()))
        .toList()
    );
    model.put(
      "itineraryPlaces",
      plan.getPlanPlaces().stream().map(PlanController::placeView).toList()
    );
    addBookmarkFlags(model, plan, userEmail);
    return new ModelAndView(VIEW_PLAN_DETAIL, model);
  }

  @GetMapping("/{id}/public")
  public ModelAndView showPublicPlan(@PathVariable UUID id, Authentication authentication) {
    Plan plan = planService.getSharedPlan(id);
    if (!plan.getIsPublic()) {
      return showJoinLanding(plan, currentEmail(authentication));
    }
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLAN, plan);
    model.put(
      "itineraryPlaces",
      plan.getPlanPlaces().stream().map(PlanController::placeView).toList()
    );
    // AC-03: "Creado por" en la vista pública; sin nombre de visualización se usa el email.
    model.put("administratorName", displayName(plan.getAdministrator()));
    // [PROF] AC-22: el autor linkea a su perfil público; el id viaja al link del template.
    model.put(
      "administratorId",
      plan.getAdministrator() == null ? null : plan.getAdministrator().getId()
    );
    // Cuánta gente acompaña el plan. Es el único dato de participantes que sale al HTML: la
    // vista pública no expone emails ni la lista (PlanPublicSecurityTest S-04).
    model.put("participantCount", plan.getParticipants().size());

    String email = currentEmail(authentication);
    model.put("isLogged", email != null);
    model.put("isParticipant", plan.isParticipant(email));
    model.put("isPlanAdministrator", plan.isAdministrator(email));
    // El form de join necesita el codigo; en un plan publico compartirlo es el proposito.
    model.put("shortCode", plan.getShortCode());
    // [CAL] Calificaciones: promedio (1 decimal), cantidad y el puntaje propio del visitante
    // (preselección del widget). La autoria es interna: del HTML solo sale el agregado.
    RatingSummary summary = this.planRatingService.getSummary(id);
    model.put("ratingAverage", PlanFeedCards.roundOneDecimal(summary.average()));
    model.put("ratingCount", summary.count());
    // Sin sesion no hay puntaje propio que buscar (AC-05): el anonimo nunca tiene widget de voto.
    model.put("myRating", email == null ? null : this.planRatingService.getMyRating(id, email));
    addBookmarkFlags(model, plan, email);
    return new ModelAndView(VIEW_PUBLIC_PLAN, model);
  }

  private ModelAndView showJoinLanding(Plan plan, String email) {
    UUID id = plan.getId();
    if (email == null) {
      return new ModelAndView("redirect:/auth/login?redirect=/plans/" + id + PUBLIC_SUFFIX);
    }
    if (plan.isAdministrator(email) || plan.isParticipant(email)) {
      return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
    }
    Map<String, Object> model = new ModelMap();
    boolean sharingEnabled = plan.getSettings().isSharingEnabled();
    model.put("planId", id);
    model.put("sharingEnabled", sharingEnabled);
    model.put("requestPending", plan.hasPendingJoinRequest(email));
    if (sharingEnabled) {
      model.put(
        "summary",
        new JoinPlanSummary(
          plan.getName(),
          plan.getDescription(),
          plan.getEventDate(),
          plan.getPlanPlaces().size(),
          "/images/plans/cover-" + (Math.floorMod(id.hashCode(), 6) + 1) + ".svg"
        )
      );
    }
    return new ModelAndView("pages/plans/request", model);
  }

  public record JoinPlanSummary(
    String name,
    String description,
    java.time.LocalDate eventDate,
    int placeCount,
    String cover
  ) {}

  @PostMapping("/{id}/join-requests")
  public ModelAndView requestToJoin(@PathVariable UUID id, Authentication authentication) {
    planService.requestToJoin(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id + PUBLIC_SUFFIX);
  }

  @PostMapping("/{id}/join-requests/{requestId}/respond")
  public ModelAndView respondToJoinRequest(
    @PathVariable UUID id,
    @PathVariable UUID requestId,
    @RequestParam String action,
    Authentication authentication
  ) {
    planService.respondToJoinRequest(id, requestId, action, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  @GetMapping("/{id}/settings")
  public ModelAndView showSettings(@PathVariable UUID id, Authentication authentication) {
    return new ModelAndView(
      "pages/plans/settings",
      ATTR_PLAN,
      planService.getOwnedPlan(id, authentication.getName())
    );
  }

  @PostMapping("/{id}/settings")
  public ModelAndView updateSettings(
    @PathVariable UUID id,
    @RequestParam PlanPermission shareLink,
    @RequestParam PlanPermission handleRequests,
    @RequestParam PlanPermission inviteInternal,
    @RequestParam PlanPermission inviteByEmail,
    Authentication authentication
  ) {
    planService.updateSettings(
      id,
      shareLink,
      handleRequests,
      inviteInternal,
      inviteByEmail,
      authentication.getName()
    );
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id + "/settings?saved=true");
  }

  /**
   * [MARK] Estado del toggle para el usuario de la sesion. Solo los planes publicos se guardan, y
   * sin sesion no se renderiza el control: {@code canBookmark} es la unica condicion que mira el
   * template, {@code isBookmarked} solo importa cuando hay sesion.
   */
  private void addBookmarkFlags(Map<String, Object> model, Plan plan, String userEmail) {
    boolean canBookmark = userEmail != null && plan.getIsPublic();
    model.put("canBookmark", canBookmark);
    model.put(
      "isBookmarked",
      canBookmark && this.planBookmarkService.isBookmarked(plan.getId(), userEmail)
    );
  }

  /** [CAL] "Calificar": upsert del puntaje del usuario logueado sobre el plan publico. Sin matcher
   * nuevo en SecurityConfig: POST /plans/** ya exige sesion, un POST anonimo rebota a login.
   * El email sale de la sesion, nunca del form.
   */
  @PostMapping("/{id}/ratings")
  public ModelAndView ratePlan(
    @PathVariable UUID id,
    @Valid @ModelAttribute("ratingData") RatingRequest ratingData,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    if (bindingResult.hasErrors()) {
      // Tampering con el form (score inválido): de vuelta a la vista publica sin calificar.
      return new ModelAndView(REDIRECT_PLAN_DETAIL + id + PUBLIC_SUFFIX);
    }
    this.planRatingService.rate(id, authentication.getName(), ratingData.getScore());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id + PUBLIC_SUFFIX);
  }

  /** En anonimo el principal es el String "anonymousUser", no un UserDetails. */
  private static String currentEmail(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
      return null;
    }
    return authentication.getName();
  }

  /**
   * Nombre con el que se firma el plan en la vista pública. Prefiere el nombre real y cae al
   * email: el "Creado por" nunca queda en blanco (13-PVP AC-03, decisión de 14-PPV).
   */
  private static String displayName(User user) {
    if (user == null) {
      return "";
    }
    String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
    String last = user.getLastName() == null ? "" : user.getLastName().trim();
    String full = (first + " " + last).trim();
    return full.isEmpty() ? user.getEmail() : full;
  }

  @PostMapping
  public ModelAndView createPlan(
    @Valid @ModelAttribute(ATTR_PLAN) PlanRequest planForm,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    if (bindingResult.hasErrors()) {
      // No hay formulario standalone: la creación vive en el modal de /explore, así que
      // un POST inválido vuelve a esa vista en vez de re-renderizar pages/plans/new.
      return new ModelAndView(REDIRECT_EXPLORE);
    }
    Plan draft = toPlanOrderedByVisitTime(planForm);
    draft.getSettings().setShareLink(planForm.getSettings().getShareLink());
    draft.getSettings().setHandleRequests(planForm.getSettings().getHandleRequests());
    draft.getSettings().setInviteInternal(planForm.getSettings().getInviteInternal());
    draft.getSettings().setInviteByEmail(planForm.getSettings().getInviteByEmail());
    Plan created = planService.createPlan(draft, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + created.getId());
  }

  @RequestMapping(value = "/{id}", method = { RequestMethod.POST, RequestMethod.PUT })
  public ModelAndView updatePlan(
    @PathVariable UUID id,
    @Valid @ModelAttribute(ATTR_PLAN) PlanRequest planForm,
    BindingResult bindingResult,
    Authentication authentication
  ) {
    if (bindingResult.hasErrors()) {
      // El detalle edita en su propia página: un PUT inválido vuelve al detalle del plan.
      return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
    }
    planService.updatePlan(id, toPlan(planForm), authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  /**
   * Delete lives on its own sub-path and accepts POST, the same way UserController's
   * /admin/users/{id}/delete does, so it never competes with updatePlan above for
   * {@code POST /plans/{id}}. Both verbs answer on the same route, which keeps the delete button
   * behaving identically whether or not {@code _method} is honoured.
   */
  @RequestMapping(value = "/{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
  public ModelAndView deletePlan(@PathVariable UUID id, Authentication authentication) {
    planService.deleteOwnedPlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLANS);
  }

  @PostMapping("/{id}/participants/role")
  public ModelAndView changeParticipantRole(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    @RequestParam String role,
    Authentication authentication
  ) {
    planService.changeParticipantRole(id, participantEmail, role, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  @PostMapping("/{id}/participants/remove")
  public ModelAndView removeParticipant(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    Authentication authentication,
    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
  ) {
    planService.removeParticipant(id, participantEmail, authentication.getName());
    redirectAttributes.addFlashAttribute(
      "participantSuccess",
      "La persona fue eliminada del plan."
    );
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  @GetMapping("/{id}/invitations/{token}/accept")
  public ModelAndView acceptInvitationLink(
    @PathVariable UUID id,
    @PathVariable String token,
    Authentication authentication
  ) {
    String email = currentEmail(authentication);
    if (email == null) {
      return new ModelAndView(
        "redirect:/auth/login?redirect=/plans/" + id + "/invitations/" + token + "/accept"
      );
    }
    return new ModelAndView("pages/plans/accept-invitation", Map.of("planId", id, "token", token));
  }

  @PostMapping("/{id}/invitations/{token}/accept")
  public ModelAndView acceptInvitationLinkPost(
    @PathVariable UUID id,
    @PathVariable String token,
    Authentication authentication
  ) {
    planService.acceptInvitationToken(id, token, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  @PostMapping("/{id}/participants/invite")
  public ModelAndView inviteParticipant(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    @RequestParam(defaultValue = "guest") String role,
    @RequestParam(defaultValue = "app") String channel,
    Authentication authentication,
    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
  ) {
    try {
      planService.inviteParticipant(
        id,
        participantEmail,
        role,
        channel,
        authentication == null ? null : authentication.getName()
      );
      redirectAttributes.addFlashAttribute(
        "invitationSuccess",
        "Invitación enviada. La persona podrá aceptarla o rechazarla en Mis planes."
      );
    } catch (org.springframework.mail.MailException exception) {
      redirectAttributes.addFlashAttribute(
        "invitationError",
        "No se pudo enviar el email. Revisá la configuración SMTP o usá la invitación dentro de la app."
      );
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("invitationError", exception.getMessage());
    }
    return new ModelAndView(REDIRECT_PLAN_DETAIL + id);
  }

  /**
   * [CLO] "Clonar Plan": copia el plan como tuyo y caes en tu copia editable. El email sale de la
   * sesion, nunca del form.
   */
  @PostMapping("/{id}/clone")
  public ModelAndView clonePlan(@PathVariable UUID id, Authentication authentication) {
    Plan copy = planService.clonePlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLAN_DETAIL + copy.getId());
  }

  @PostMapping("/{id}/leave")
  public ModelAndView leavePlan(@PathVariable UUID id, Authentication authentication) {
    planService.leavePlan(id, authentication.getName());
    return new ModelAndView(REDIRECT_PLANS);
  }

  @PostMapping("/{id}/invitations/{invitationId}/respond")
  public ModelAndView respondToInvitation(
    @PathVariable UUID id,
    @PathVariable UUID invitationId,
    @RequestParam boolean accept,
    Authentication authentication,
    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
  ) {
    planService.respondToInvitation(id, invitationId, accept, authentication.getName());
    redirectAttributes.addFlashAttribute(
      "invitationResponse",
      accept ? "Invitación aceptada." : "Invitación rechazada."
    );
    return new ModelAndView(REDIRECT_PLANS);
  }

  @PostMapping("/join")
  public ModelAndView joinPlan(@RequestParam String shortCode, Authentication authentication) {
    String email = authentication.getName();
    Plan plan = planService.joinPlan(shortCode, email);
    boolean awaitingApproval =
      !plan.getIsPublic() && !plan.isAdministrator(email) && !plan.isParticipant(email);
    return new ModelAndView(
      REDIRECT_PLAN_DETAIL + plan.getId() + (awaitingApproval ? PUBLIC_SUFFIX : "")
    );
  }

  /**
   * El orden del formulario es el del mouse, no el del día: quien arma el plan agrega primero el
   * lugar que vio primero y recién después le pone la hora. Así que al crear, el itinerario se
   * ordena por hora de visita; los que no tienen hora quedan al final, en el orden en que se
   * cargaron. Solo al crear: en la edición el orden ya es una decisión del usuario (el drag &amp;
   * drop del detalle).
   */
  private Plan toPlanOrderedByVisitTime(PlanRequest planForm) {
    Plan plan = toPlan(planForm);
    plan
      .getPlanPlaces()
      .sort(
        Comparator.comparing(
          PlanPlace::getVisitTime,
          Comparator.nullsLast(Comparator.naturalOrder())
        )
      );
    int order = 1;
    for (PlanPlace entry : plan.getPlanPlaces()) {
      entry.setSortOrder(order);
      order++;
    }
    return plan;
  }

  /** The form owns these fields and its itinerary rows only: id, shortCode and administrator never come from a post. */
  private Plan toPlan(PlanRequest planForm) {
    Plan plan = new Plan();
    plan.setName(planForm.getName());
    plan.setDescription(planForm.getDescription());
    plan.setEventDate(planForm.getEventDate());
    plan.setEventTime(planForm.getEventTime());
    plan.setIsPublic(planForm.getIsPublic());
    int order = 1;
    for (PlanPlaceRequest row : planForm.getPlaces()) {
      if (row.getPlaceId() == null) {
        continue;
      }
      // Id-only stub: PlanServiceImpl swaps it for the persisted Place.
      Place place = new Place();
      place.setId(row.getPlaceId());
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setDescription(row.getDescription());
      entry.setVisitDate(row.getVisitDate());
      entry.setVisitTime(row.getVisitTime());
      entry.setSortOrder(order);
      order++;
      plan.addPlanPlace(entry);
    }
    return plan;
  }

  private static Map<String, Object> placeView(PlanPlace entry) {
    Place place = entry.getPlace();
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("id", String.valueOf(place.getId()));
    view.put("entryId", String.valueOf(entry.getId()));
    view.put("description", entry.getDescription());
    view.put("visitDate", entry.getVisitDate() == null ? null : entry.getVisitDate().toString());
    view.put("visitTime", entry.getVisitTime() == null ? null : entry.getVisitTime().toString());
    view.put("name", place.getName());
    view.put("category", place.getCategory() == null ? null : place.getCategory().name());
    view.put("address", place.getAddress());
    view.put("latitude", place.getLatitude());
    view.put("longitude", place.getLongitude());
    return view;
  }
}
