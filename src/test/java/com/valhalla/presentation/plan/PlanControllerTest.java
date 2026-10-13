package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
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
  private static final String PUBLIC_LIST_VIEW = "pages/plans/public-list";
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

  @Test
  public void T_PVP_008_showPublicPlan_conIdInexistentePropagaPlanNotFoundException() {
    // U-03: el id que nadie conoce sigue el mismo camino que un plan privado (U-02). La
    // distinción real vive en el servicio; acá se prueba que el controller no la filtra.
    when(this.planServiceMock.getPublicPlan(404L)).thenThrow(new PlanNotFoundException());

    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPublicPlan(404L, this.authentication)
    );
    verify(this.planServiceMock, times(1)).getPublicPlan(404L);
  }

  @Test
  public void T_PVP_009_showPublicPlan_armaElItinerarioEnOrdenSortOrder() {
    // given: la colección viene ordenada por @OrderBy("sortOrder ASC") desde JPA
    Plan plan = plan(PLAN_ID, "Viaje publico");
    plan.addPlanPlace(stop(1L, "Aeropuerto", 1));
    plan.addPlanPlace(stop(2L, "Centro historico", 2));
    plan.addPlanPlace(stop(3L, "Mirador", 3));
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then
    List<?> stops = (List<?>) view.getModel().get("itineraryPlaces");
    List<String> names = stops
      .stream()
      .map(item -> (String) ((Map<?, ?>) item).get("name"))
      .toList();
    assertThat(names, is(List.of("Aeropuerto", "Centro historico", "Mirador")));
  }

  @Test
  public void T_PVP_010_showPublicPlan_anonimoDejaLosFlagsEnFalsoYSinAtributosDeEdicion() {
    // given: con anonimo el Authentication existe, pero el principal no es un UserDetails
    Plan plan = plan(PLAN_ID, "Viaje publico");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);
    Authentication anonymous = new AnonymousAuthenticationToken(
      "key",
      "anonymousUser",
      AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, anonymous);

    // then: los CTAs se deciden por estos flags y la vista es read-only
    assertThat(view.getViewName(), is(equalTo("pages/plans/public")));
    assertThat(view.getModel().get("isLogged"), is(false));
    assertThat(view.getModel().get("isParticipant"), is(false));
    assertThat(view.getModel().get("isPlanAdministrator"), is(false));
    assertThat(view.getModel().containsKey("participantEmails"), is(false));
  }

  @Test
  public void T_PVP_011_showPublicPlan_conSesionDeNoParticipanteExponeElShortCode() {
    // given
    Plan plan = plan(PLAN_ID, "Viaje publico");
    plan.setShortCode("ABCD1234");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);
    // El principal de una sesión real es un UserDetails, no un String (ver currentEmail)
    Authentication visitor = new UsernamePasswordAuthenticationToken(
      new org.springframework.security.core.userdetails.User(
        "visitante@test.com",
        "n/a",
        AuthorityUtils.createAuthorityList("ROLE_USER")
      ),
      null
    );

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, visitor);

    // then: el form de join necesita el código, pero el visitante no es ni admin ni participante
    assertThat(view.getModel().get("isLogged"), is(true));
    assertThat(view.getModel().get("isParticipant"), is(false));
    assertThat(view.getModel().get("isPlanAdministrator"), is(false));
    assertThat(view.getModel().get("shortCode"), is(equalTo("ABCD1234")));
  }

  @Test
  public void T_PVP_012_showPublicPlan_muestraElNombreCompletoDelAdministrador() {
    // given
    Plan plan = plan(PLAN_ID, "Viaje publico");
    User admin = new User();
    admin.setEmail("admin@test.com");
    admin.setFirstName("Ana");
    admin.setLastName("Gomez");
    plan.setAdministrator(admin);
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then
    assertThat(view.getModel().get("administratorName"), is(equalTo("Ana Gomez")));
  }

  @Test
  public void T_PVP_013_showPublicPlan_administradorSinNombreUsaElEmail() {
    // given: sin nombre de visualización el "Creado por" nunca queda en blanco (14-PPV)
    Plan plan = plan(PLAN_ID, "Viaje publico");
    User admin = new User();
    admin.setEmail("admin@test.com");
    admin.setFirstName("   ");
    admin.setLastName(null);
    plan.setAdministrator(admin);
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then
    assertThat(view.getModel().get("administratorName"), is(equalTo("admin@test.com")));
  }

  // --- GET /plans/public [PPV] ---

  @Test
  public void U01_publicPlans_retornaLaVistaPublicListConUnaTarjetaPorPlanPublico() {
    // given
    List<Plan> planes = List.of(
      plan(PLAN_ID, "Buenos Aires para todos"),
      plan(PLAN_ID + 1, "Noche")
    );
    when(this.planServiceMock.getPublicPlans()).thenReturn(planes);

    // when
    ModelAndView view = this.controller.publicPlans(this.authentication);

    // then
    assertThat(view.getViewName(), is(PUBLIC_LIST_VIEW));
    assertThat(cardsOf(view), hasSize(2));
    verify(this.planServiceMock, times(1)).getPublicPlans();
  }

  @Test
  public void U02_publicPlans_armaCadaTarjetaConLosCamposPublicosDelPlan() {
    // given
    Plan plan = plan(PLAN_ID, "Feria de San Telmo");
    plan.setDescription("Recorrido entre puestos");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    User administrator = new User();
    administrator.setFirstName("Ana");
    administrator.setLastName("Perez");
    administrator.setEmail(OWNER_EMAIL);
    plan.setAdministrator(administrator);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(new Place());
    plan.addPlanPlace(entry);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.publicPlans(this.authentication);

    // then: nombre, descripcion, fecha, cantidad de lugares y administrador (AC-03)
    Map<?, ?> card = cardAt(view, 0);
    assertThat(card.get("id"), is(equalTo(PLAN_ID)));
    assertThat(card.get("name"), is(equalTo("Feria de San Telmo")));
    assertThat(card.get("description"), is(equalTo("Recorrido entre puestos")));
    assertThat(card.get("eventDate"), is(equalTo("2026-12-31")));
    assertThat(card.get("placeCount"), is(equalTo(1)));
    assertThat(card.get("administratorName"), is(equalTo("Ana Perez")));
    // el plan es del usuario logueado (dueno@test.com): la tarjeta marca own para mostrar Editar
    assertThat(card.get("own"), is(equalTo(true)));
    // cover generico deterministico: id 7 -> 7 % 6 + 1 -> cover-2.svg
    assertThat(card.get("cover"), is(equalTo("/images/plans/cover-2.svg")));
  }

  @Test
  public void U02b_publicPlans_unPlanAjenoNoSeMarcaComoPropio() {
    // given: el plan es de otra persona
    Plan plan = plan(PLAN_ID, "Plan ajeno");
    User administrator = new User();
    administrator.setEmail("otra@persona.com");
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    Map<?, ?> card = cardAt(this.controller.publicPlans(this.authentication), 0);

    // then: own en false — la tarjeta no ofrece Editar sobre un plan que no es mio
    assertThat(card.get("own"), is(equalTo(false)));
  }

  @Test
  public void U03_publicPlans_conLaBaseVaciaDevuelveLaListaVaciaSinError() {
    // given
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of());

    // when
    ModelAndView view = this.controller.publicPlans(this.authentication);

    // then: el estado vacio lo decide la vista, el controller no lanza nada
    assertThat(view.getViewName(), is(PUBLIC_LIST_VIEW));
    assertThat(cardsOf(view), is(empty()));
    verify(this.planServiceMock, times(1)).getPublicPlans();
  }

  @Test
  public void U04_publicPlans_noExponeShortCodeNiEmailsNiParticipantes() {
    // given: un plan publico con todo lo que AC-09 prohibe mostrar
    Plan plan = plan(PLAN_ID, "Plan con secretos");
    plan.setShortCode("SECRET01");
    User administrator = new User();
    administrator.setFirstName("Ana");
    administrator.setLastName("Perez");
    administrator.setEmail("ana@privado.com");
    plan.setAdministrator(administrator);
    User participant = new User();
    participant.setEmail("invitado@privado.com");
    plan.getParticipants().add(participant);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.publicPlans(this.authentication);
    Map<?, ?> card = cardAt(view, 0);

    // then: ni la clave del codigo, ni rastro de los emails en los valores
    List<String> keys = card.keySet().stream().map(String::valueOf).toList();
    assertThat(
      keys,
      is(
        equalTo(
          List.of(
            "id",
            "name",
            "description",
            "eventDate",
            "placeCount",
            "administratorName",
            "own",
            "cover"
          )
        )
      )
    );
    String values = card.values().stream().map(String::valueOf).collect(Collectors.joining(" "));
    assertThat(values, not(containsString("SECRET01")));
    assertThat(values, not(containsString("ana@privado.com")));
    assertThat(values, not(containsString("invitado@privado.com")));
  }

  @Test
  public void U04b_publicPlans_unAdministradorSinNombreNoFiltraSuEmail() {
    // given: el seed de E2E reinserta usuarios sin nombre, y el listado no puede filtrar el email
    Plan plan = plan(PLAN_ID, "Plan con administrador sin nombre");
    User administrator = new User();
    administrator.setEmail("anonimo@privado.com");
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    Map<?, ?> card = cardAt(this.controller.publicPlans(this.authentication), 0);

    // then (AC-09): el nombre de administrador queda vacio, nunca el email
    assertThat(card.get("administratorName"), is(equalTo("")));
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

  // --- POST /plans/{id}/clone ---

  @Test
  public void T_PLN_069_clonePlan_redirigeAlDetalleDeLaCopia() {
    // given (U-12)
    Plan copy = plan(PLAN_ID + 1, "Viaje a Bariloche");
    when(this.planServiceMock.clonePlan(PLAN_ID, OWNER_EMAIL)).thenReturn(copy);

    // when
    ModelAndView view = this.controller.clonePlan(PLAN_ID, this.authentication);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + copy.getId())));
    verify(this.planServiceMock, times(1)).clonePlan(PLAN_ID, OWNER_EMAIL);
  }

  @Test
  public void T_PLN_069b_clonePlan_tomaElEmailDeLaSesionYNoUnDuenoDelForm() {
    // given (U-13): the cloner travels in the session; the endpoint takes no form body at all
    Authentication clonerSession = new UsernamePasswordAuthenticationToken("cloner@test.com", null);
    Plan copy = plan(PLAN_ID + 1, "Copia");
    when(this.planServiceMock.clonePlan(PLAN_ID, "cloner@test.com")).thenReturn(copy);

    // when
    ModelAndView view = this.controller.clonePlan(PLAN_ID, clonerSession);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + copy.getId())));
    verify(this.planServiceMock, times(1)).clonePlan(PLAN_ID, "cloner@test.com");
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

  private static List<?> cardsOf(ModelAndView view) {
    return (List<?>) view.getModel().get("publicPlans");
  }

  private static Map<?, ?> cardAt(ModelAndView view, int index) {
    return (Map<?, ?>) cardsOf(view).get(index);
  }

  private static PlanPlace stop(Long placeId, String placeName, int sortOrder) {
    Place place = new Place();
    place.setId(placeId);
    place.setName(placeName);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setSortOrder(sortOrder);
    return entry;
  }
}
