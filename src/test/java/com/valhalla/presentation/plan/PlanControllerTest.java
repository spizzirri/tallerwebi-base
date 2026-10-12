package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests of {@link PlanController}. The service is mocked; validation runs through a real
 * {@link LocalValidatorFactoryBean} so the {@code @Valid} constraints on {@link PlanRequest} are the
 * ones under test, not a hand-made {@code BindingResult}.
 */
public class PlanControllerTest {

  private static final String OWNER_EMAIL = "dueno@test.com";
  private static final String LIST_VIEW = "pages/plans/list";
  private static final String DETAIL_VIEW = "pages/plans/detail";
  private static final Long PLAN_ID = 7L;

  private static final Validator VALIDATOR = localValidator();

  private PlanController controller;
  private PlanService planServiceMock;
  private PlanPlaceService planPlaceServiceMock;
  private Authentication authentication;

  @BeforeEach
  public void init() {
    this.planServiceMock = mock(PlanService.class);
    this.planPlaceServiceMock = mock(PlanPlaceService.class);
    this.controller = new PlanController(this.planServiceMock, this.planPlaceServiceMock);
    this.authentication = new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null);
  }

  // --- GET /plans, GET /plans/{id} ---

  @Test
  public void T_PLN_060_listPlans_pideLosPlanesDelEmailAutenticado() {
    // given
    List<Plan> planes = List.of(plan(PLAN_ID, "Viaje a Bariloche"));
    when(this.planServiceMock.getPlansByUserEmail(OWNER_EMAIL)).thenReturn(planes);

    // when
    ModelAndView view = this.controller.listPlans(this.authentication);

    // then
    assertThat(view.getViewName(), is(LIST_VIEW));
    assertThat(view.getModel().get("plans"), is(sameInstance(planes)));
    verify(this.planServiceMock, times(1)).getPlansByUserEmail(OWNER_EMAIL);
  }

  @Test
  public void T_PLN_062_showPlan_cargaElPlanDelDueno() {
    // given
    Plan plan = plan(PLAN_ID, "Viaje a Bariloche");
    when(this.planServiceMock.getParticipatingPlan(PLAN_ID, OWNER_EMAIL)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPlan(PLAN_ID, this.authentication);

    // then
    assertThat(view.getViewName(), is(DETAIL_VIEW));
    assertThat(view.getModel().get("plan"), is(sameInstance(plan)));
    verify(this.planServiceMock, times(1)).getParticipatingPlan(PLAN_ID, OWNER_EMAIL);
  }

  @Test
  public void showPlan_ordenaLosParticipantesPorEmailSinModificarElPlan() {
    Plan plan = plan(PLAN_ID, "Participantes");
    User zoe = new User();
    zoe.setEmail("zoe@test.com");
    User ana = new User();
    ana.setEmail("Ana@test.com");
    plan.getParticipants().addAll(List.of(zoe, ana));
    when(this.planServiceMock.getParticipatingPlan(PLAN_ID, OWNER_EMAIL)).thenReturn(plan);

    ModelAndView view = this.controller.showPlan(PLAN_ID, this.authentication);

    assertThat(
      view.getModel().get("participantEmails"),
      is(List.of("Ana@test.com", "zoe@test.com"))
    );
    assertThat(plan.getParticipants(), is(List.of(zoe, ana)));
  }

  @Test
  public void T_PLN_063_showPlan_propagaPlanNotFoundException() {
    // given
    when(this.planServiceMock.getParticipatingPlan(999L, OWNER_EMAIL))
      .thenThrow(new PlanNotFoundException());

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPlan(999L, this.authentication)
    );
  }

  @Test
  public void T_PVP_001_showPublicPlan_retornaLaVistaYElPlanEnElModelo() {
    Plan plan = plan(PLAN_ID, "Viaje publico");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    assertThat(view.getViewName(), is(equalTo("pages/plans/public")));
    assertThat(view.getModel().get("plan"), is(sameInstance(plan)));
    verify(this.planServiceMock, times(1)).getPublicPlan(PLAN_ID);
  }

  @Test
  public void T_PVP_002_showPublicPlan_propagaPlanNotFoundException() {
    when(this.planServiceMock.getPublicPlan(999L)).thenThrow(new PlanNotFoundException());

    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPublicPlan(999L, this.authentication)
    );
  }

  // --- POST /plans ---

  @Test
  public void T_PLN_064_createPlan_conErroresRedirigeAExploreSinTocarElServicio() {
    // given
    PlanRequest form = new PlanRequest("   ", null, null, null);
    BeanPropertyBindingResult errors = errorsOf(form);

    // when
    ModelAndView view = this.controller.createPlan(form, errors, this.authentication);

    // then: sin formulario standalone, un POST inválido vuelve a /explore
    assertThat(view.getViewName(), is(equalTo("redirect:/explore")));
    assertThat(fieldNames(errors), hasItem("name"));
    verify(this.planServiceMock, never()).createPlan(any(Plan.class), anyString());
  }

  @Test
  public void T_PLN_065_createPlan_validoRedirigeAlDetalleDelPlanCreado() {
    // given
    PlanRequest form = new PlanRequest(
      "Viaje a Bariloche",
      "Un viaje largo",
      LocalDate.of(2026, 12, 31),
      true
    );
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    when(this.planServiceMock.createPlan(captor.capture(), eq(OWNER_EMAIL)))
      .thenReturn(plan(PLAN_ID, "Viaje a Bariloche"));

    // when
    ModelAndView view = this.controller.createPlan(form, noErrors(form), this.authentication);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + PLAN_ID)));
    assertThat(captor.getValue().getName(), is(equalTo("Viaje a Bariloche")));
    assertThat(captor.getValue().getDescription(), is(equalTo("Un viaje largo")));
    assertThat(captor.getValue().getEventDate(), is(equalTo(LocalDate.of(2026, 12, 31))));
    assertThat(captor.getValue().getIsPublic(), is(true));
    // The form is not the owner of id, shortCode or administrator, so the DTO must carry none.
    assertThat(captor.getValue().getId(), is(nullValue()));
    assertThat(captor.getValue().getShortCode(), is(nullValue()));
    assertThat(captor.getValue().getAdministrator(), is(nullValue()));
    verify(this.planServiceMock, times(1)).createPlan(any(Plan.class), eq(OWNER_EMAIL));
  }

  // --- PUT /plans/{id} ---

  @Test
  public void T_PLN_067_createPlan_mapeaLosLugaresEnOrdenYSaltaLasFilasVacias() {
    // given
    PlanRequest form = new PlanRequest("Viaje", null, null, false);
    form.setPlaces(
      List.of(
        new PlanPlaceRequest(4L, "primero", LocalDate.of(2026, 12, 1), LocalTime.of(9, 30)),
        new PlanPlaceRequest(null, null, null, null),
        new PlanPlaceRequest(4L, "otra vez", null, null)
      )
    );
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    when(this.planServiceMock.createPlan(captor.capture(), eq(OWNER_EMAIL)))
      .thenReturn(plan(PLAN_ID, "Viaje"));

    // when
    this.controller.createPlan(form, noErrors(form), this.authentication);

    // then
    List<PlanPlace> entries = captor.getValue().getPlanPlaces();
    assertThat(entries.size(), is(2));
    assertThat(entries.get(0).getPlace().getId(), is(4L));
    assertThat(entries.get(0).getSortOrder(), is(1));
    assertThat(entries.get(0).getDescription(), is(equalTo("primero")));
    assertThat(entries.get(0).getVisitDate(), is(equalTo(LocalDate.of(2026, 12, 1))));
    assertThat(entries.get(0).getVisitTime(), is(equalTo(LocalTime.of(9, 30))));
    assertThat(entries.get(1).getPlace().getId(), is(4L));
    assertThat(entries.get(1).getSortOrder(), is(2));
    assertThat(entries.get(1).getPlan(), is(sameInstance(captor.getValue())));
  }

  // --- PUT /plans/{id} ---

  @Test
  public void T_PLN_066_updatePlan_conErroresRedirigeAlDetalleConElIdDelPlan() {
    // given
    PlanRequest form = new PlanRequest("", null, null, null);
    BeanPropertyBindingResult errors = errorsOf(form);

    // when
    ModelAndView view = this.controller.updatePlan(PLAN_ID, form, errors, this.authentication);

    // then: sin formulario standalone, un PUT inválido vuelve al detalle del plan
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + PLAN_ID)));
    assertThat(fieldNames(errors), hasItem("name"));
    verify(this.planServiceMock, never()).updatePlan(any(), any(Plan.class), anyString());
  }

  @Test
  public void T_PLN_067_updatePlan_validoRedirigeAlDetalle() {
    // given
    PlanRequest form = new PlanRequest("Nombre nuevo", null, null, null);
    ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
    when(this.planServiceMock.updatePlan(eq(PLAN_ID), captor.capture(), eq(OWNER_EMAIL)))
      .thenReturn(plan(PLAN_ID, "Nombre nuevo"));

    // when
    ModelAndView view =
      this.controller.updatePlan(PLAN_ID, form, noErrors(form), this.authentication);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + PLAN_ID)));
    assertThat(captor.getValue().getName(), is(equalTo("Nombre nuevo")));
    assertThat(captor.getValue().getId(), is(nullValue()));
    verify(this.planServiceMock, times(1))
      .updatePlan(eq(PLAN_ID), any(Plan.class), eq(OWNER_EMAIL));
  }

  // --- DELETE /plans/{id} ---

  @Test
  public void T_PLN_068_deletePlan_borraElIdDelPathYRedirigeAlListado() {
    // when
    ModelAndView view = this.controller.deletePlan(PLAN_ID, this.authentication);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans")));
    verify(this.planServiceMock, times(1)).deleteOwnedPlan(PLAN_ID, OWNER_EMAIL);
  }

  // --- helpers ---

  private static LocalValidatorFactoryBean localValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.afterPropertiesSet();
    return factoryBean;
  }

  private static BeanPropertyBindingResult noErrors(Object target) {
    return new BeanPropertyBindingResult(target, "target");
  }

  private static BeanPropertyBindingResult errorsOf(Object target) {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
    VALIDATOR.validate(target, bindingResult);
    return bindingResult;
  }

  private static Set<String> fieldNames(BeanPropertyBindingResult bindingResult) {
    return bindingResult
      .getFieldErrors()
      .stream()
      .map(FieldError::getField)
      .collect(Collectors.toSet());
  }

  private Plan plan(Long id, String name) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    return plan;
  }
}
