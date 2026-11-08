package com.valhalla.presentation.plan;

import static com.valhalla.presentation.shared.SuccessRedirects.confirmed;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.plan.PlanSettings;
import com.valhalla.domain.plan.TransportMode;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.planplace.PlanPlaceUpdate;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ABM de planes bajo /admin/plans, espejo de {@code PlaceAdminController}: listado, alta a
 * nombre del usuario sistema, edicion de cualquier plan, activar/desactivar y baja. El detalle es
 * de solo lectura; participantes, lugares y settings se gestionan en el edit (AC-08 a AC-11,
 * AC-19, AC-20). La desactivacion oculta el plan de los listados publicos pero conserva el acceso
 * de sus participantes (AC-12 a AC-14).
 */
@Controller
@RequestMapping("/admin/plans")
public class PlanAdminController {

  private static final String VIEW_PLANS = "pages/admin/plans";
  private static final String VIEW_PLAN_FORM = "pages/admin/plan-form";
  private static final String VIEW_PLAN_DETAIL = "pages/admin/plan-detail";
  private static final String REDIRECT_PLANS = "redirect:/admin/plans";
  private static final String REDIRECT_PLAN_DETAIL = "redirect:/admin/plans/";
  // [PABM] Las operaciones de edicion vuelven al edit para seguir editando sin navegar.
  private static final String REDIRECT_PLAN_EDIT = "redirect:/admin/plans/";
  private static final String EDIT_SUFFIX = "/edit";
  private static final String ATTR_PLANS = "plans";
  private static final String ATTR_PLAN_FORM = "planForm";
  private static final String ATTR_PLAN_ID = "planId";
  private static final String ATTR_IS_EDIT = "isEdit";
  private static final String ATTR_PLAN = "plan";
  private static final String ATTR_PLAN_PLACE_IDS = "planPlaceIds";
  private static final String ATTR_TRANSPORT_LABELS = "transportLabels";
  private static final String ATTR_DEFAULT_SETTINGS = "defaultPlanSettings";
  private static final String ATTR_TODAY = "today";
  private static final String ATTR_ERROR = "error";
  private static final String ATTR_INVITATION_ERROR = "invitationError";
  private static final String ERROR_INVALID_DATA = "Invalid plan data";

  /** Etiquetas en castellano de los modos de traslado (los mismos que el alta del usuario). */
  private static final Map<TransportMode, String> TRANSPORT_LABELS = new EnumMap<>(
    Map.of(
      TransportMode.WALK,
      "A pie",
      TransportMode.BIKE,
      "Bicicleta",
      TransportMode.BUS,
      "Colectivo",
      TransportMode.CAR,
      "Auto"
    )
  );

  private final PlanService planService;
  private final PlanPlaceService planPlaceService;

  @Autowired
  public PlanAdminController(PlanService planService, PlanPlaceService planPlaceService) {
    this.planService = planService;
    this.planPlaceService = planPlaceService;
  }

  @GetMapping
  public ModelAndView listPlans() {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLANS, planService.getAllPlans());
    return new ModelAndView(VIEW_PLANS, model);
  }

  /** AC-02: el alta usa los mismos tres pasos que el usuario, a nombre del usuario sistema. */
  @GetMapping("/new")
  public ModelAndView showNewPlanForm() {
    return new ModelAndView(VIEW_PLAN_FORM, newFormModel(new EditPlanRequest(), null));
  }

  /**
   * AC-03 y AC-19: el alta pasa por el mismo camino que el usuario ({@link PlanController#toPlan})
   * para que los lugares y el horario se deriven igual. El dueño lo decide el servicio.
   */
  @PostMapping
  public ModelAndView createPlan(
    @Valid @ModelAttribute(ATTR_PLAN_FORM) EditPlanRequest planForm,
    BindingResult bindingResult,
    @RequestParam(defaultValue = "false") boolean autoSchedule
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(planForm, false, firstError(bindingResult), null);
    }
    Plan draft = toPlan(planForm);
    PlanSettings settings = planForm.getSettings();
    draft.getSettings().setHandleRequests(settings.getHandleRequests());
    draft.getSettings().setInviteInternal(settings.getInviteInternal());
    draft.getSettings().setInviteByEmail(settings.getInviteByEmail());
    draft.getSettings().setAutoSchedule(autoSchedule);
    Plan created = planService.createAdminPlan(draft);
    return confirmed(REDIRECT_PLAN_DETAIL + created.getId(), "Plan creado.");
  }

  @GetMapping("/{id}/edit")
  public ModelAndView showEditPlanForm(@PathVariable UUID id) {
    Plan plan = planService.getAnyPlan(id);
    EditPlanRequest form = new EditPlanRequest();
    form.setName(plan.getName());
    form.setDescription(plan.getDescription());
    form.setEventDate(plan.getEventDate());
    form.setEventTime(plan.getEventTime());
    form.setTransportMode(plan.getTransportMode());
    form.setIsPublic(plan.getIsPublic());
    form.setActive(plan.getActive());
    return new ModelAndView(VIEW_PLAN_FORM, editFormModel(form, plan));
  }

  @PostMapping("/{id}")
  public ModelAndView updatePlan(
    @PathVariable UUID id,
    @Valid @ModelAttribute(ATTR_PLAN_FORM) EditPlanRequest planForm,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      return renderFormWithError(planForm, true, firstError(bindingResult), id);
    }
    planService.updateAnyPlan(id, toPlan(planForm));
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Plan actualizado.");
  }

  /** AC-06: alterna el estado y vuelve al listado con el resultado como toast. */
  @PostMapping("/{id}/toggle-active")
  public ModelAndView toggleActive(@PathVariable UUID id, @RequestParam boolean active) {
    planService.togglePlanActive(id, active);
    return confirmed(REDIRECT_PLANS, active ? "Plan activado." : "Plan desactivado.");
  }

  @RequestMapping(value = "/{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
  public ModelAndView deletePlan(@PathVariable UUID id) {
    planService.deleteAnyPlan(id);
    return confirmed(REDIRECT_PLANS, "Plan eliminado.");
  }

  /** Solo lectura: el detalle no tiene formularios, la edicion vive en el edit. */
  @GetMapping("/{id}")
  public ModelAndView showPlanDetail(@PathVariable UUID id) {
    Map<String, Object> model = new ModelMap();
    // getAnyPlan calienta participantes, editores y lugares dentro de su transaccion.
    model.put(ATTR_PLAN, planService.getAnyPlan(id));
    return new ModelAndView(VIEW_PLAN_DETAIL, model);
  }

  @PostMapping("/{id}/participants/role")
  public ModelAndView changeParticipantRole(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    @RequestParam String role
  ) {
    planService.changeParticipantRoleAny(id, participantEmail, role);
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Rol actualizado.");
  }

  @PostMapping("/{id}/participants/remove")
  public ModelAndView removeParticipant(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    Authentication authentication
  ) {
    planService.removeParticipantAny(id, participantEmail, authentication.getName());
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Participante eliminado del plan.");
  }

  /**
   * AC-20: invitacion desde el edit. Actua con la autoridad del dueño del plan; los rechazos de
   * negocio (ya es participante, baneado, correo inexistente) vuelven como aviso en el edit.
   */
  @PostMapping("/{id}/participants/invite")
  public ModelAndView inviteParticipant(
    @PathVariable UUID id,
    @RequestParam String participantEmail,
    @RequestParam(defaultValue = "guest") String role,
    RedirectAttributes redirectAttributes
  ) {
    try {
      planService.inviteParticipantAny(id, participantEmail, role);
      return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Invitación enviada.");
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute(ATTR_INVITATION_ERROR, exception.getMessage());
      return new ModelAndView(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX);
    }
  }

  /** AC-11: los cuatro settings del plan, con autoSchedule incluido. */
  @PostMapping("/{id}/settings")
  public ModelAndView updateSettings(
    @PathVariable UUID id,
    @RequestParam PlanPermission handleRequests,
    @RequestParam PlanPermission inviteInternal,
    @RequestParam PlanPermission inviteByEmail,
    @RequestParam(defaultValue = "false") boolean autoSchedule
  ) {
    planService.updateAnySettings(id, handleRequests, inviteInternal, inviteByEmail, autoSchedule);
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Configuración guardada.");
  }

  @RequestMapping(
    value = "/{id}/places/{placeId}/delete",
    method = { RequestMethod.POST, RequestMethod.DELETE }
  )
  public ModelAndView removePlace(@PathVariable UUID id, @PathVariable UUID placeId) {
    Plan plan = planService.getAnyPlan(id);
    planPlaceService.removePlaceFromPlan(id, placeId, ownerEmail(plan));
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Lugar quitado del plan.");
  }

  /** AC-19: alta de lugar desde el mapa del edit (el picker postea el id del catalogo). */
  @PostMapping("/{id}/places/add")
  public ModelAndView addPlace(@PathVariable UUID id, @RequestParam UUID placeId) {
    Plan plan = planService.getAnyPlan(id);
    planPlaceService.addPlaceToPlan(id, placeId, ownerEmail(plan));
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Lugar agregado al plan.");
  }

  @PostMapping("/{id}/places/{placeId}/move-up")
  public ModelAndView movePlaceUp(@PathVariable UUID id, @PathVariable UUID placeId) {
    return movePlace(id, placeId, -1);
  }

  @PostMapping("/{id}/places/{placeId}/move-down")
  public ModelAndView movePlaceDown(@PathVariable UUID id, @PathVariable UUID placeId) {
    return movePlace(id, placeId, 1);
  }

  /**
   * Guardado por fila del itinerario: duracion, horario fijado, modo y minutos del tramo hacia la
   * siguiente. Los campos son opcionales (el input vacio llega como cadena vacia y se convierte en
   * null): null significa "dejalo como esta / que lo derive el planificador".
   */
  @PostMapping("/{id}/places/{placeId}/update")
  public ModelAndView updatePlaceEntry(
    @PathVariable UUID id,
    @PathVariable UUID placeId,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate,
    @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime visitTime,
    @RequestParam(required = false) Integer durationMinutes,
    @RequestParam(required = false) TransportMode transportModeToNext,
    @RequestParam(required = false) Integer travelMinutesToNext
  ) {
    Plan plan = planService.getAnyPlan(id);
    PlanPlaceUpdate changes = new PlanPlaceUpdate(
      durationMinutes,
      visitDate,
      visitTime,
      transportModeToNext,
      travelMinutesToNext
    );
    planPlaceService.updatePlanPlace(id, placeId, changes, ownerEmail(plan));
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Parada actualizada.");
  }

  /**
   * Intercambia la parada con su vecina y reordena. El servicio exige la lista completa de ids en
   * el nuevo orden, asi que se arma desde las filas del plan (que ya vienen ordenadas por
   * sortOrder) y se swapea la pareja.
   */
  private ModelAndView movePlace(UUID id, UUID placeId, int offset) {
    Plan plan = planService.getAnyPlan(id);
    // toList() es inmutable y Collections.swap la modifica: hay que copiarla a una lista mutable.
    List<UUID> entryIds = new ArrayList<>(
      plan.getPlanPlaces().stream().map(PlanPlace::getId).toList()
    );
    int index = entryIds.indexOf(placeId);
    int target = index + offset;
    if (index >= 0 && target >= 0 && target < entryIds.size()) {
      Collections.swap(entryIds, index, target);
      planPlaceService.reorderPlaces(id, entryIds, ownerEmail(plan));
      return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "Orden actualizado.");
    }
    return confirmed(REDIRECT_PLAN_EDIT + id + EDIT_SUFFIX, "La parada ya esta en el extremo.");
  }

  /** Ids de los lugares ya en el plan: el picker los excluye del catalogo que ofrece. */
  private static List<UUID> planPlaceIds(Plan plan) {
    return plan.getPlanPlaces().stream().map(entry -> entry.getPlace().getId()).toList();
  }

  /**
   * El admin actua con la autoridad del dueño: el servicio de lugares solo acepta al dueno del
   * plan, y todo plan de la plataforma tiene dueno (el alta admin le asigna el usuario sistema).
   */
  private static String ownerEmail(Plan plan) {
    if (plan.getAdministrator() == null) {
      throw new PlanNotFoundException();
    }
    return plan.getAdministrator().getEmail();
  }

  /**
   * El form no manda id, shortCode ni administrator: son datos del backend. La ventana del dia
   * no es del admin: siempre el default del planificador, y en el update el servicio conserva la
   * guardada. {@code active} se normaliza porque el checkbox sin tildar no manda el param.
   */
  private static Plan toPlan(EditPlanRequest planForm) {
    Plan plan = PlanController.toPlan(planForm);
    plan.setDayEndTime(PlanController.DEFAULT_DAY_END);
    plan.setActive(Boolean.TRUE.equals(planForm.getActive()));
    return plan;
  }

  private static String firstError(BindingResult bindingResult) {
    return bindingResult
      .getFieldErrors()
      .stream()
      .findFirst()
      .map(FieldError::getDefaultMessage)
      .orElse(ERROR_INVALID_DATA);
  }

  private static Map<String, Object> newFormModel(EditPlanRequest form, String error) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLAN_FORM, form);
    model.put(ATTR_IS_EDIT, false);
    model.put(ATTR_TRANSPORT_LABELS, TRANSPORT_LABELS);
    model.put(ATTR_DEFAULT_SETTINGS, new PlanSettings());
    model.put(ATTR_TODAY, LocalDate.now());
    model.put(ATTR_ERROR, error);
    return model;
  }

  private static Map<String, Object> editFormModel(EditPlanRequest form, Plan plan) {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_PLAN_FORM, form);
    model.put(ATTR_IS_EDIT, true);
    model.put(ATTR_TRANSPORT_LABELS, TRANSPORT_LABELS);
    model.put(ATTR_PLAN_ID, plan.getId());
    // Las secciones de participantes, lugares y settings leen el plan directo.
    model.put(ATTR_PLAN, plan);
    model.put(ATTR_PLAN_PLACE_IDS, planPlaceIds(plan));
    return model;
  }

  private ModelAndView renderFormWithError(
    EditPlanRequest form,
    boolean isEdit,
    String error,
    UUID id
  ) {
    if (id == null) {
      return new ModelAndView(VIEW_PLAN_FORM, newFormModel(form, error));
    }
    // En edicion el form re-renderiza las secciones con el plan guardado, no con lo tipeado.
    Map<String, Object> model = editFormModel(form, planService.getAnyPlan(id));
    model.put(ATTR_IS_EDIT, isEdit);
    model.put(ATTR_ERROR, error);
    return new ModelAndView(VIEW_PLAN_FORM, model);
  }
}
