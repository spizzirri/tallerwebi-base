package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.plan.TransportMode;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.planplace.PlanPlaceUpdate;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

/**
 * [PABM] Unit tests of {@link PlanAdminController} (U-01 a U-10 de 25-PABM.md) con el servicio
 * mockeado. Se prueba la vista, el redirect, el toast y lo que el controller le entrega al servicio.
 */
public class PlanAdminControllerTest {

  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000004a");
  private static final UUID PLACE_A = UUID.fromString("0192f0a1-0000-7000-8000-00000000004b");
  private static final UUID PLACE_B = UUID.fromString("0192f0a1-0000-7000-8000-00000000004c");
  private static final UUID PLACE_C = UUID.fromString("0192f0a1-0000-7000-8000-00000000004d");
  private static final String APP_EMAIL = "app@planit.com";

  private PlanService planServiceMock;
  private PlanPlaceService planPlaceServiceMock;
  private PlanAdminController controller;

  @BeforeEach
  public void setUp() {
    this.planServiceMock = mock(PlanService.class);
    this.planPlaceServiceMock = mock(PlanPlaceService.class);
    this.controller = new PlanAdminController(planServiceMock, planPlaceServiceMock);
    // Sin request activo confirmed() no puede dejar toast: se monta uno con flash de salida.
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  // --- U-01 ---

  @Test
  public void listPlansRendersTheListWithEveryPlan() {
    List<Plan> plans = List.of(plan(PLAN_ID, "Uno"));
    when(planServiceMock.getAllPlans()).thenReturn(plans);

    ModelAndView view = controller.listPlans();

    assertThat(view.getViewName(), is(equalTo("pages/admin/plans")));
    assertThat(view.getModel().get("plans"), is(equalTo(plans)));
  }

  // --- U-02 ---

  @Test
  public void showNewPlanFormRendersAnEmptyCreateForm() {
    ModelAndView view = controller.showNewPlanForm();

    assertThat(view.getViewName(), is(equalTo("pages/admin/plan-form")));
    assertThat(view.getModel().get("isEdit"), is(false));
    assertThat(view.getModel().get("planForm"), is(notNullValue()));
    assertThat(view.getModel(), hasKey("defaultPlanSettings"));
  }

  // --- U-03 ---

  @Test
  public void createPlanRedirectsToTheDetailAndSendsTheDefaultDayEnd() {
    when(planServiceMock.createAdminPlan(any())).thenReturn(plan(PLAN_ID, "Nuevo"));
    EditPlanRequest form = validForm("Nuevo");

    ModelAndView view = controller.createPlan(form, bindingOf(form), false);

    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID)));
    assertThat(toast(), is(equalTo("Plan creado.")));
    // AC-21: el alta admin usa el default del planificador, no un valor del form.
    Plan sent = capturedCreate();
    assertThat(sent.getDayEndTime(), is(equalTo(LocalTime.of(23, 0))));
  }

  // --- U-04 ---

  @Test
  public void createPlanWithABlankNameRendersTheFormWithTheError() {
    EditPlanRequest form = validForm("");
    BindingResult errors = bindingOf(form);
    errors.addError(new FieldError("planForm", "name", "Plan name is required"));

    ModelAndView view = controller.createPlan(form, errors, false);

    assertThat(view.getViewName(), is(equalTo("pages/admin/plan-form")));
    assertThat(view.getModel().get("error"), is(equalTo("Plan name is required")));
    verify(planServiceMock, never()).createAdminPlan(any());
  }

  // --- U-05 ---

  @Test
  public void showEditPlanFormPreloadsTheStoredPlan() {
    Plan plan = plan(PLAN_ID, "Guardado");
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setTransportMode(TransportMode.BUS);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    ModelAndView view = controller.showEditPlanForm(PLAN_ID);

    EditPlanRequest form = (EditPlanRequest) view.getModel().get("planForm");
    assertThat(view.getViewName(), is(equalTo("pages/admin/plan-form")));
    assertThat(view.getModel().get("isEdit"), is(true));
    assertThat(form.getName(), is(equalTo("Guardado")));
    assertThat(form.getTransportMode(), is(equalTo(TransportMode.BUS)));
    assertThat(view.getModel().get("plan"), is(equalTo(plan)));
  }

  // --- U-06 ---

  @Test
  public void updatePlanRedirectsBackToTheEditAndKeepsTheDayEndDefault() {
    EditPlanRequest form = validForm("Editado");
    form.setActive(true);

    ModelAndView view = controller.updatePlan(PLAN_ID, form, bindingOf(form));

    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
    assertThat(toast(), is(equalTo("Plan actualizado.")));
    Plan sent = capturedUpdate();
    assertThat(sent.getName(), is(equalTo("Editado")));
    assertThat(sent.getDayEndTime(), is(equalTo(LocalTime.of(23, 0))));
  }

  @Test
  public void updatePlanWithUncheckedActiveDeactivatesThePlan() {
    EditPlanRequest form = validForm("Editado");
    form.setActive(null);

    controller.updatePlan(PLAN_ID, form, bindingOf(form));

    assertThat(capturedUpdate().getActive(), is(false));
  }

  // --- U-07 ---

  @Test
  public void deletePlanDeletesAndRedirectsToTheList() {
    ModelAndView view = controller.deletePlan(PLAN_ID);

    verify(planServiceMock).deleteAnyPlan(PLAN_ID);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans")));
    assertThat(toast(), is(equalTo("Plan eliminado.")));
  }

  // --- U-08 / U-09 ---

  @Test
  public void toggleActiveReactivatesAndRedirectsToTheList() {
    ModelAndView view = controller.toggleActive(PLAN_ID, true);

    verify(planServiceMock).togglePlanActive(PLAN_ID, true);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans")));
    assertThat(toast(), is(equalTo("Plan activado.")));
  }

  @Test
  public void toggleActiveDeactivatesAndRedirectsToTheList() {
    ModelAndView view = controller.toggleActive(PLAN_ID, false);

    verify(planServiceMock).togglePlanActive(PLAN_ID, false);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans")));
    assertThat(toast(), is(equalTo("Plan desactivado.")));
  }

  // --- U-10 ---

  @Test
  public void showPlanDetailRendersTheReadOnlyView() {
    Plan plan = plan(PLAN_ID, "Detalle");
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    ModelAndView view = controller.showPlanDetail(PLAN_ID);

    assertThat(view.getViewName(), is(equalTo("pages/admin/plan-detail")));
    assertThat(view.getModel().get("plan"), is(equalTo(plan)));
  }

  // --- Places: reorden (regresion del 500 al mover) y alta ---

  @Test
  public void movePlaceDownSwapsWithTheNextEntryAndSendsTheFullOrder() {
    Plan plan = planWithPlaces(PLACE_A, PLACE_B, PLACE_C);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    ModelAndView view = controller.movePlaceDown(PLAN_ID, PLACE_B);

    verify(planPlaceServiceMock)
      .reorderPlaces(eq(PLAN_ID), eq(List.of(PLACE_A, PLACE_C, PLACE_B)), any());
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
  }

  @Test
  public void movePlaceUpSwapsWithThePreviousEntry() {
    Plan plan = planWithPlaces(PLACE_A, PLACE_B, PLACE_C);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    controller.movePlaceUp(PLAN_ID, PLACE_C);

    verify(planPlaceServiceMock)
      .reorderPlaces(eq(PLAN_ID), eq(List.of(PLACE_A, PLACE_C, PLACE_B)), any());
  }

  @Test
  public void movePlaceUpOnTheFirstEntryDoesNotReorder() {
    Plan plan = planWithPlaces(PLACE_A, PLACE_B);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    controller.movePlaceUp(PLAN_ID, PLACE_A);

    verify(planPlaceServiceMock, never()).reorderPlaces(any(), anyList(), any());
  }

  @Test
  public void addPlaceUsesTheOwnerAuthorityAndRedirectsToTheEdit() {
    Plan plan = plan(PLAN_ID, "Con dueño");
    plan.setAdministrator(owner(APP_EMAIL));
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    ModelAndView view = controller.addPlace(PLAN_ID, PLACE_A);

    verify(planPlaceServiceMock).addPlaceToPlan(PLAN_ID, PLACE_A, APP_EMAIL);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
  }

  // --- Invitaciones: rechazo de negocio vuelve como aviso, no como error 500 ---

  @Test
  public void inviteParticipantRejectedByBusinessRulesRedirectsWithTheReason() {
    doThrow(new IllegalArgumentException("Ya es participante"))
      .when(planServiceMock)
      .inviteParticipantAny(PLAN_ID, "x@test.com", "guest");
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView view = controller.inviteParticipant(PLAN_ID, "x@test.com", "guest", redirect);

    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
    assertThat(
      redirect.getFlashAttributes().get("invitationError"),
      is(equalTo("Ya es participante"))
    );
  }

  @Test
  public void inviteParticipantWithTheEditorRoleGoesThroughTheService() {
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    controller.inviteParticipant(PLAN_ID, "editor@test.com", "editor", redirect);

    verify(planServiceMock).inviteParticipantAny(PLAN_ID, "editor@test.com", "editor");
    assertThat(redirect.getFlashAttributes(), not(hasKey("invitationError")));
  }

  // --- Resto de las acciones del edit ---

  @Test
  public void updatePlanWithABlankNameRerendersTheEditWithTheStoredPlan() {
    Plan stored = plan(PLAN_ID, "Guardado");
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(stored);
    EditPlanRequest form = validForm("");
    BindingResult errors = bindingOf(form);
    errors.addError(new FieldError("planForm", "name", "Plan name is required"));

    ModelAndView view = controller.updatePlan(PLAN_ID, form, errors);

    assertThat(view.getViewName(), is(equalTo("pages/admin/plan-form")));
    assertThat(view.getModel().get("isEdit"), is(equalTo(true)));
    assertThat(view.getModel().get("plan"), is(equalTo(stored)));
    assertThat(view.getModel().get("error"), is(equalTo("Plan name is required")));
    verify(planServiceMock, never()).updateAnyPlan(any(), any());
  }

  @Test
  public void createPlanWithAGlobalErrorFallsBackToTheGenericMessage() {
    EditPlanRequest form = validForm("Cualquiera");
    BindingResult errors = bindingOf(form);
    errors.addError(new ObjectError("planForm", "bad"));

    ModelAndView view = controller.createPlan(form, errors, false);

    assertThat(view.getModel().get("error"), is(equalTo("Invalid plan data")));
    verify(planServiceMock, never()).createAdminPlan(any());
  }

  @Test
  public void changeParticipantRoleGoesThroughTheServiceAndRedirectsToTheEdit() {
    ModelAndView view = controller.changeParticipantRole(PLAN_ID, "editor@test.com", "editor");

    verify(planServiceMock).changeParticipantRoleAny(PLAN_ID, "editor@test.com", "editor");
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
    assertThat(toast(), is(equalTo("Rol actualizado.")));
  }

  @Test
  public void removeParticipantGoesThroughTheServiceAndRedirectsToTheEdit() {
    Authentication admin = mock(Authentication.class);
    when(admin.getName()).thenReturn(APP_EMAIL);

    ModelAndView view = controller.removeParticipant(PLAN_ID, "guest@test.com", admin);

    verify(planServiceMock).removeParticipantAny(PLAN_ID, "guest@test.com", APP_EMAIL);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
    assertThat(toast(), is(equalTo("Participante eliminado del plan.")));
  }

  @Test
  public void updateSettingsSendsAllFourValuesAndRedirectsToTheEdit() {
    ModelAndView view = controller.updateSettings(
      PLAN_ID,
      PlanPermission.EDITORS,
      PlanPermission.PARTICIPANTS,
      PlanPermission.OWNER,
      true
    );

    verify(planServiceMock)
      .updateAnySettings(
        PLAN_ID,
        PlanPermission.EDITORS,
        PlanPermission.PARTICIPANTS,
        PlanPermission.OWNER,
        true
      );
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
    assertThat(toast(), is(equalTo("Configuración guardada.")));
  }

  @Test
  public void removePlaceUsesTheOwnerAuthorityAndRedirectsToTheEdit() {
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan(PLAN_ID, "Con paradas"));

    ModelAndView view = controller.removePlace(PLAN_ID, PLACE_A);

    verify(planPlaceServiceMock).removePlaceFromPlan(PLAN_ID, PLACE_A, APP_EMAIL);
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
  }

  @Test
  public void updatePlaceEntrySendsTheTypedChangesWithTheOwnerAuthority() {
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan(PLAN_ID, "Con paradas"));
    LocalDate day = LocalDate.of(2026, 12, 2);
    LocalTime at = LocalTime.of(18, 30);

    ModelAndView view = controller.updatePlaceEntry(
      PLAN_ID,
      PLACE_A,
      day,
      at,
      90,
      TransportMode.BUS,
      25
    );

    verify(planPlaceServiceMock)
      .updatePlanPlace(
        eq(PLAN_ID),
        eq(PLACE_A),
        eq(new PlanPlaceUpdate(90, day, at, TransportMode.BUS, 25)),
        eq(APP_EMAIL)
      );
    assertThat(view.getViewName(), is(equalTo("redirect:/admin/plans/" + PLAN_ID + "/edit")));
  }

  @Test
  public void aPlaceOperationOnAPlanWithoutOwnerFailsAsNotFound() {
    Plan orphan = plan(PLAN_ID, "Sin dueño");
    orphan.setAdministrator(null);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(orphan);

    assertThrows(PlanNotFoundException.class, () -> controller.removePlace(PLAN_ID, PLACE_A));
    verify(planPlaceServiceMock, never()).removePlaceFromPlan(any(), any(), any());
  }

  @Test
  public void movePlaceDownOnTheLastEntryDoesNotReorder() {
    Plan plan = planWithPlaces(PLACE_A, PLACE_B);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    controller.movePlaceDown(PLAN_ID, PLACE_B);

    verify(planPlaceServiceMock, never()).reorderPlaces(any(), anyList(), any());
  }

  @Test
  public void movePlaceDownOnAnUnknownEntryDoesNotReorder() {
    Plan plan = planWithPlaces(PLACE_A, PLACE_B);
    when(planServiceMock.getAnyPlan(PLAN_ID)).thenReturn(plan);

    controller.movePlaceDown(PLAN_ID, PLACE_C);

    verify(planPlaceServiceMock, never()).reorderPlaces(any(), anyList(), any());
  }

  // --- helpers ---

  private static Plan plan(UUID id, String name) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    plan.setAdministrator(owner(APP_EMAIL));
    return plan;
  }

  private static User owner(String email) {
    User user = new User();
    user.setId(UUID.fromString("0192f0a1-0000-7000-8000-000000000050"));
    user.setEmail(email);
    return user;
  }

  private static Plan planWithPlaces(UUID... placeIds) {
    Plan plan = plan(PLAN_ID, "Con paradas");
    for (UUID placeId : placeIds) {
      Place place = new Place();
      place.setId(placeId);
      place.setName("Lugar " + placeId);
      PlanPlace entry = new PlanPlace();
      entry.setId(placeId);
      entry.setPlace(place);
      plan.addPlanPlace(entry);
    }
    return plan;
  }

  private static EditPlanRequest validForm(String name) {
    EditPlanRequest form = new EditPlanRequest();
    form.setName(name);
    form.setEventDate(LocalDate.of(2026, 12, 1));
    form.setTransportMode(TransportMode.WALK);
    form.setIsPublic(true);
    form.getSettings().setHandleRequests(PlanPermission.OWNER);
    form.getSettings().setInviteInternal(PlanPermission.OWNER);
    form.getSettings().setInviteByEmail(PlanPermission.OWNER);
    return form;
  }

  private static BindingResult bindingOf(Object form) {
    return new BeanPropertyBindingResult(form, "planForm");
  }

  private static String toast() {
    FlashMap flash = (FlashMap) RequestContextHolder
      .currentRequestAttributes()
      .getAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, 0);
    return flash == null ? null : (String) flash.get("successToast");
  }

  /** Lo que el controller le entrego a createAdminPlan. */
  private Plan capturedCreate() {
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    verify(planServiceMock).createAdminPlan(captor.capture());
    return captor.getValue();
  }

  /** Lo que el controller le entrego a updateAnyPlan para el plan de la prueba. */
  private Plan capturedUpdate() {
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    verify(planServiceMock).updateAnyPlan(eq(PLAN_ID), captor.capture());
    return captor.getValue();
  }
}
