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
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
  private static final String DETAIL_VIEW = "pages/plans/detail";
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000008");
  private static final UUID MISSING_PLAN_ID = UUID.fromString(
    "00000000-0000-0000-0000-000000000999"
  );
  private static final UUID UNKNOWN_PLAN_ID = UUID.fromString(
    "00000000-0000-0000-0000-000000000404"
  );

  private static final Validator VALIDATOR = localValidator();

  private PlanController controller;
  private PlanService planServiceMock;
  private PlanPlaceService planPlaceServiceMock;
  private PlanRatingService planRatingServiceMock;
  private Authentication authentication;

  @BeforeEach
  public void init() {
    this.planServiceMock = mock(PlanService.class);
    this.planPlaceServiceMock = mock(PlanPlaceService.class);
    this.planRatingServiceMock = mock(PlanRatingService.class);
    this.controller =
      new PlanController(
        this.planServiceMock,
        this.planPlaceServiceMock,
        this.planRatingServiceMock
      );
    this.authentication = new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null);
    // Sin calificaciones es el default de cualquier plan en los tests de este archivo.
    when(this.planRatingServiceMock.getSummary(any(UUID.class)))
      .thenReturn(new RatingSummary(PLAN_ID, null, 0L));
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
  public void showPlan_includesOwnerOnceWithoutChangingParticipants() {
    Plan plan = plan(PLAN_ID, "Owner");
    User owner = new User();
    owner.setEmail(OWNER_EMAIL);
    plan.setAdministrator(owner);
    when(this.planServiceMock.getParticipatingPlan(PLAN_ID, OWNER_EMAIL)).thenReturn(plan);
    ModelAndView view = this.controller.showPlan(PLAN_ID, this.authentication);
    assertThat(view.getModel().get("participantEmails"), is(List.of(OWNER_EMAIL)));
    assertThat(plan.getParticipants().isEmpty(), is(true));
    plan.getParticipants().add(owner);
    view = this.controller.showPlan(PLAN_ID, this.authentication);
    assertThat(view.getModel().get("participantEmails"), is(List.of(OWNER_EMAIL)));
  }

  @Test
  public void T_PLN_063_showPlan_propagaPlanNotFoundException() {
    // given
    when(this.planServiceMock.getParticipatingPlan(MISSING_PLAN_ID, OWNER_EMAIL))
      .thenThrow(new PlanNotFoundException());

    // when and then
    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPlan(MISSING_PLAN_ID, this.authentication)
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
    when(this.planServiceMock.getPublicPlan(MISSING_PLAN_ID))
      .thenThrow(new PlanNotFoundException());

    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPublicPlan(MISSING_PLAN_ID, this.authentication)
    );
  }

  @Test
  public void T_PVP_008_showPublicPlan_conIdInexistentePropagaPlanNotFoundException() {
    // U-03: el id que nadie conoce sigue el mismo camino que un plan privado (U-02). La
    // distinción real vive en el servicio; acá se prueba que el controller no la filtra.
    when(this.planServiceMock.getPublicPlan(UNKNOWN_PLAN_ID))
      .thenThrow(new PlanNotFoundException());

    assertThrows(
      PlanNotFoundException.class,
      () -> this.controller.showPublicPlan(UNKNOWN_PLAN_ID, this.authentication)
    );
    verify(this.planServiceMock, times(1)).getPublicPlan(UNKNOWN_PLAN_ID);
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

  // --- GET /for-you [PPV] (el feed vive en ForYouControllerTest) ---

  @Test
  public void T_PVP_016_showPublicPlan_exponeElContadorDeParticipantes() {
    // given: dos personas acompanan el plan, mas su administrador (que no es participant)
    Plan plan = plan(PLAN_ID, "Viaje publico");
    User admin = new User();
    admin.setEmail("admin@test.com");
    plan.setAdministrator(admin);
    plan.getParticipants().add(new User());
    plan.getParticipants().add(new User());
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then: la vista publica muestra cuanta gente lo acompana, nunca la lista ni los emails
    assertThat(view.getModel().get("participantCount"), is(equalTo(2)));
    assertThat(view.getModel().containsKey("participantEmails"), is(false));
  }

  @Test
  public void T_PVP_017_showPublicPlan_exponeLasCoordenadasDeCadaParadaParaElMapa() {
    // given: el unico stop trae lat/lng, que es lo que el mapa del template necesita
    Plan plan = plan(PLAN_ID, "Viaje publico");
    Place place = new Place();
    place.setId(1L);
    place.setName("Aeropuerto");
    place.setLatitude(-34.8222);
    place.setLongitude(-58.5358);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    plan.addPlanPlace(entry);
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then
    Map<?, ?> stop = (Map<?, ?>) ((List<?>) view.getModel().get("itineraryPlaces")).get(0);
    assertThat(stop.get("latitude"), is(equalTo(Double.valueOf(-34.8222))));
    assertThat(stop.get("longitude"), is(equalTo(Double.valueOf(-58.5358))));
  }

  // --- GET /for-you: el feed se mudo a ForYouController (/for-you, con y sin sesion) ---

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
    Plan copy = plan(OTHER_PLAN_ID, "Viaje a Bariloche");
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
    Plan copy = plan(OTHER_PLAN_ID, "Copia");
    when(this.planServiceMock.clonePlan(PLAN_ID, "cloner@test.com")).thenReturn(copy);

    // when
    ModelAndView view = this.controller.clonePlan(PLAN_ID, clonerSession);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + copy.getId())));
    verify(this.planServiceMock, times(1)).clonePlan(PLAN_ID, "cloner@test.com");
  }

  // --- POST /plans/{id}/ratings [CAL] ---

  @Test
  public void T_CAL_010_showPublicPlan_exponeElPromedioLaCantidadYLaCalificacionPropia() {
    // given
    Plan plan = plan(PLAN_ID, "Viaje publico");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);
    // El principal de una sesion real es un UserDetails, no un String (ver currentEmail)
    Authentication visitor = new UsernamePasswordAuthenticationToken(
      new org.springframework.security.core.userdetails.User(
        "visitante@test.com",
        "n/a",
        AuthorityUtils.createAuthorityList("ROLE_USER")
      ),
      null
    );
    when(this.planRatingServiceMock.getSummary(PLAN_ID))
      .thenReturn(new RatingSummary(PLAN_ID, 4.25, 3L));
    when(this.planRatingServiceMock.getMyRating(PLAN_ID, "visitante@test.com")).thenReturn(4);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, visitor);

    // then: promedio redondeado a un decimal, count y autoría interna (AC-02/03/07)
    assertThat(view.getModel().get("ratingAverage"), is(equalTo(4.3)));
    assertThat(view.getModel().get("ratingCount"), is(equalTo(3L)));
    assertThat(view.getModel().get("myRating"), is(equalTo(4)));
  }

  @Test
  public void T_CAL_010b_showPublicPlan_sinCalificacionesDejaPromedioNullYCountCero() {
    // given
    Plan plan = plan(PLAN_ID, "Viaje publico");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, this.authentication);

    // then (AC-07): la vista muestra "Todavía sin calificaciones"
    assertThat(view.getModel().get("ratingAverage"), is(nullValue()));
    assertThat(view.getModel().get("ratingCount"), is(equalTo(0L)));
  }

  @Test
  public void T_CAL_010c_showPublicPlan_anonimoNoTieneCalificacionPropia() {
    // given: el anonimo ve el indicador pero nunca un widget de voto (AC-05)
    Plan plan = plan(PLAN_ID, "Viaje publico");
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan);
    Authentication anonymous = new AnonymousAuthenticationToken(
      "key",
      "anonymousUser",
      AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    // when
    ModelAndView view = this.controller.showPublicPlan(PLAN_ID, anonymous);

    // then
    assertThat(view.getModel().get("myRating"), is(nullValue()));
  }

  @Test
  public void T_CAL_011_ratePlan_calificaConElEmailDeLaSesionYVuelveALaVistaPublica() {
    // given
    RatingRequest form = new RatingRequest(4);
    when(this.planServiceMock.getPublicPlan(PLAN_ID)).thenReturn(plan(PLAN_ID, "Viaje publico"));

    // when
    ModelAndView view =
      this.controller.ratePlan(PLAN_ID, form, noErrors(form), this.authentication);

    // then (AC-01)
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + PLAN_ID + "/public")));
    verify(this.planRatingServiceMock, times(1)).rate(PLAN_ID, OWNER_EMAIL, 4);
  }

  @Test
  public void T_CAL_011b_ratePlan_conScoreInvalidoNoTocaElServicioYVuelveALaVistaPublica() {
    // given: tampering con el form (radio no permite score fuera de 1..5)
    RatingRequest form = new RatingRequest(7);
    BeanPropertyBindingResult errors = errorsOf(form);

    // when
    ModelAndView view = this.controller.ratePlan(PLAN_ID, form, errors, this.authentication);

    // then
    assertThat(view.getViewName(), is(equalTo("redirect:/plans/" + PLAN_ID + "/public")));
    assertThat(fieldNames(errors), hasItem("score"));
    verify(this.planRatingServiceMock, never()).rate(any(UUID.class), anyString(), anyInt());
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

  private Plan plan(UUID id, String name) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    return plan;
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
