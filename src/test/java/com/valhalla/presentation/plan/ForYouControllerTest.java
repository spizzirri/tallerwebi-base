package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.rating.RatingSummary;
import com.valhalla.domain.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests of {@link ForYouController}. The service is mocked; the feed moved out of
 * {@code PlanController} when the route became the top-level {@code GET /for-you}, visible with and
 * without session.
 */
public class ForYouControllerTest {

  private static final String OWNER_EMAIL = "dueno@test.com";
  private static final String PUBLIC_LIST_VIEW = "pages/plans/public-list";
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");
  private static final UUID OTHER_PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000008");
  private static final UUID MUSEUM_PLAN_ID = UUID.fromString(
    "0192f0a1-0000-7000-8000-000000000009"
  );

  private ForYouController controller;
  private PlanService planServiceMock;
  private PlanRatingService planRatingServiceMock;
  private Authentication authentication;

  @BeforeEach
  public void init() {
    this.planServiceMock = mock(PlanService.class);
    this.planRatingServiceMock = mock(PlanRatingService.class);
    this.controller = new ForYouController(this.planServiceMock, this.planRatingServiceMock);
    this.authentication = new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null);
    when(this.planRatingServiceMock.getSummariesByPlanId()).thenReturn(java.util.Map.of());
  }

  // --- GET /for-you ---

  @Test
  public void U01_forYou_retornaLaVistaPublicListConUnaTarjetaPorPlanPublico() {
    // given
    List<Plan> planes = List.of(
      plan(PLAN_ID, "Buenos Aires para todos"),
      plan(OTHER_PLAN_ID, "Noche")
    );
    when(this.planServiceMock.getPublicPlans()).thenReturn(planes);

    // when
    ModelAndView view = this.controller.forYou(this.authentication, null, null, null, null);

    // then
    assertThat(view.getViewName(), is(PUBLIC_LIST_VIEW));
    assertThat(cardsOf(view), hasSize(2));
    verify(this.planServiceMock, times(1)).getPublicPlans();
  }

  @Test
  public void U02_forYou_armaCadaTarjetaConLosCamposPublicosDelPlan() {
    // given
    Plan plan = plan(PLAN_ID, "Feria de San Telmo");
    plan.setDescription("Recorrido entre puestos");
    plan.setEventDate(LocalDate.of(2026, 12, 31));
    User administrator = new User();
    administrator.setFirstName("Ana");
    administrator.setLastName("Perez");
    administrator.setEmail("otra@persona.com");
    plan.setAdministrator(administrator);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(new Place());
    plan.addPlanPlace(entry);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.forYou(this.authentication, null, null, null, null);

    // then: nombre, descripcion, fecha, cantidad de lugares y administrador (AC-03)
    Map<?, ?> card = cardAt(view, 0);
    assertThat(card.get("id"), is(equalTo(PLAN_ID)));
    assertThat(card.get("name"), is(equalTo("Feria de San Telmo")));
    assertThat(card.get("description"), is(equalTo("Recorrido entre puestos")));
    assertThat(card.get("eventDate"), is(equalTo("2026-12-31")));
    assertThat(card.get("placeCount"), is(equalTo(1)));
    assertThat(card.get("administratorName"), is(equalTo("Ana Perez")));
    // cover generico deterministico: hash del id fijo 4 -> 4 % 6 + 1 -> cover-5.svg
    assertThat(card.get("cover"), is(equalTo("/images/plans/cover-5.svg")));
    // [CAL] sin calificaciones el agregado es count 0 y promedio null (AC-07)
    assertThat(card.get("ratingAverage"), is(nullValue()));
    assertThat(card.get("ratingCount"), is(equalTo(0L)));
  }

  @Test
  public void U02e_forYou_laTarjetaExponeElAgregadoDeCalificaciones() {
    // given: dos calificaciones, promedio 4.25 → la tarjeta muestra 4.3 (un decimal)
    Plan plan = plan(PLAN_ID, "Plan calificado");
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));
    when(this.planRatingServiceMock.getSummariesByPlanId())
      .thenReturn(Map.of(PLAN_ID, new RatingSummary(PLAN_ID, 4.25, 12L)));

    // when
    Map<?, ?> card = cardAt(this.controller.forYou(this.authentication, null, null, null, null), 0);

    // then (AC-10): el agregado sale al feed, jamas la autoria de los votantes
    assertThat(card.get("ratingAverage"), is(equalTo(4.3)));
    assertThat(card.get("ratingCount"), is(equalTo(12L)));
  }

  @Test
  public void U02b_forYou_unPlanPropioNoApareceEnElListado() {
    // given: el plan es del usuario logueado (dueno@test.com)
    Plan plan = plan(PLAN_ID, "Mi plan propio");
    User administrator = new User();
    administrator.setEmail(OWNER_EMAIL);
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.forYou(this.authentication, null, null, null, null);

    // then: el feed es para descubrir planes de otros: el propio queda fuera
    assertThat(cardsOf(view), is(empty()));
  }

  @Test
  public void U02c_forYou_unVisitanteVeTodosLosPlanesPublicosSinFiltroDePropios() {
    // given: sin sesion el principal es un AnonymousAuthenticationToken ("anonymousUser")
    Plan plan = plan(PLAN_ID, "Plan de otro");
    User administrator = new User();
    administrator.setEmail("otro@test.com");
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));
    Authentication anon = new AnonymousAuthenticationToken(
      "key",
      "anonymousUser",
      AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    // when
    ModelAndView view = this.controller.forYou(anon, null, null, null, null);

    // then: el visitante no tiene planes propios, el filtro no le saca nada
    assertThat(cardsOf(view), hasSize(1));
  }

  @Test
  public void U02d_forYou_sinPrincipalElFiltroDePropiosNoTiraNada() {
    // given: cadena de seguridad sin anonymous habilitado deja el principal en null
    Plan plan = plan(PLAN_ID, "Plan visible");
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.forYou(null, null, null, null, null);

    // then
    assertThat(cardsOf(view), hasSize(1));
  }

  @Test
  public void U03_forYou_conLaBaseVaciaDevuelveLaListaVaciaSinError() {
    // given
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of());

    // when
    ModelAndView view = this.controller.forYou(this.authentication, null, null, null, null);

    // then: el estado vacio lo decide la vista, el controller no lanza nada
    assertThat(view.getViewName(), is(PUBLIC_LIST_VIEW));
    assertThat(cardsOf(view), is(empty()));
    verify(this.planServiceMock, times(1)).getPublicPlans();
  }

  @Test
  public void U04_forYou_noExponeShortCodeNiEmailsNiParticipantes() {
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
    ModelAndView view = this.controller.forYou(this.authentication, null, null, null, null);
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
            "administratorId",
            "cover",
            "ratingAverage",
            "ratingCount"
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
  public void U04b_forYou_unAdministradorSinNombreNoFiltraSuEmail() {
    // given: el seed de E2E reinserta usuarios sin nombre, y el listado no puede filtrar el email
    Plan plan = plan(PLAN_ID, "Plan con administrador sin nombre");
    User administrator = new User();
    administrator.setEmail("anonimo@privado.com");
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    Map<?, ?> card = cardAt(this.controller.forYou(this.authentication, null, null, null, null), 0);

    // then (AC-09): el nombre de administrador queda vacio, nunca el email
    assertThat(card.get("administratorName"), is(equalTo("")));
  }

  // --- GET /for-you con busqueda, filtros y orden [SEARCH] ---

  @Test
  public void U19_forYou_pasaLosParametrosAlDescubrimientoYDevuelveElListadoFiltrado() {
    // given: solo el primero matchea texto, categoria y fecha a la vez
    LocalDate today = LocalDate.now();
    Plan match = plan(MUSEUM_PLAN_ID, "Noche de museo", "Museo y bar", today.plusDays(3));
    addPlace(match, PlaceCategory.BAR);
    Plan wrongCategory = plan(OTHER_PLAN_ID, "Museo sin bar", "Museo", today.plusDays(3));
    addPlace(wrongCategory, PlaceCategory.MUSEUM);
    Plan wrongDate = plan(PLAN_ID, "Museo viejo", "Museo y bar", today.minusDays(1));
    addPlace(wrongDate, PlaceCategory.BAR);
    when(this.planServiceMock.getPublicPlans())
      .thenReturn(List.of(match, wrongCategory, wrongDate));

    // when
    ModelAndView view =
      this.controller.forYou(this.authentication, "museo", "BAR", "upcoming", "date");

    // then (AC-01, AC-03, AC-04, AC-05): un solo plan y el conteo de participantes solo de los
    // planes publicos que llegan al descubrimiento
    assertThat(cardsOf(view), hasSize(1));
    assertThat(cardAt(view, 0).get("id"), is(equalTo(MUSEUM_PLAN_ID)));
    verify(this.planServiceMock, times(1))
      .getParticipantCounts(List.of(MUSEUM_PLAN_ID, OTHER_PLAN_ID, PLAN_ID));
  }

  @Test
  public void U20_forYou_elModeloExponeLosValoresAplicadosYFiltersApplied() {
    // when: texto con espacios, categoria en minusculas y order/fecha explicitos
    ModelAndView view =
      this.controller.forYou(this.authentication, "  museo  ", "bar", "past", "name");

    // then (AC-08, AC-09): el formulario refleja lo aplicado, normalizado
    assertThat(view.getModel().get("q"), is(equalTo("museo")));
    assertThat(view.getModel().get("selectedCategory"), is(equalTo("BAR")));
    assertThat(view.getModel().get("selectedWhen"), is(equalTo("past")));
    assertThat(view.getModel().get("selectedSort"), is(equalTo("name")));
    assertThat(view.getModel().get("filtersApplied"), is(equalTo(true)));
    assertThat(
      (PlaceCategory[]) view.getModel().get("categories"),
      is(arrayContaining(PlaceCategory.values()))
    );
  }

  @Test
  public void U21_forYou_parametrosNulosOInvalidosNoLanzanYNoFiltran() {
    // given
    when(this.planServiceMock.getPublicPlans())
      .thenReturn(List.of(plan(PLAN_ID, "Plan visible"), plan(OTHER_PLAN_ID, "Otro plan")));

    // when: todos nulos, y despues basura en categoria, fecha y orden
    ModelAndView nulls = this.controller.forYou(this.authentication, null, null, null, null);
    ModelAndView invalid =
      this.controller.forYou(this.authentication, "  ", "NOPE", "ever", "random");

    // then (AC-12): la pagina no rompe y el listado queda sin filtros
    assertThat(cardsOf(nulls), hasSize(2));
    assertThat(cardsOf(invalid), hasSize(2));
    assertThat(invalid.getModel().get("selectedCategory"), is(equalTo("")));
    assertThat(invalid.getModel().get("selectedWhen"), is(equalTo("all")));
    assertThat(invalid.getModel().get("selectedSort"), is(equalTo("popular")));
    assertThat(invalid.getModel().get("filtersApplied"), is(equalTo(false)));
  }

  @Test
  public void U22_forYou_conFiltrosYSinResultadosMarcaFiltersAppliedYListaVacia() {
    // given
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan(PLAN_ID, "Museo de Arte")));

    // when
    ModelAndView view = this.controller.forYou(this.authentication, "zzz", null, null, null);

    // then (AC-09): la vista elige el estado vacio propio de los filtros
    assertThat(cardsOf(view), is(empty()));
    assertThat(view.getModel().get("filtersApplied"), is(equalTo(true)));
  }

  private Plan plan(UUID id, String name) {
    Plan plan = new Plan();
    plan.setId(id);
    plan.setName(name);
    return plan;
  }

  private Plan plan(UUID id, String name, String description, LocalDate eventDate) {
    Plan plan = plan(id, name);
    plan.setDescription(description);
    plan.setEventDate(eventDate);
    return plan;
  }

  private static void addPlace(Plan plan, PlaceCategory category) {
    Place place = new Place();
    place.setCategory(category);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    plan.addPlanPlace(entry);
  }

  private static List<?> cardsOf(ModelAndView view) {
    return (List<?>) view.getModel().get("publicPlans");
  }

  private static Map<?, ?> cardAt(ModelAndView view, int index) {
    return (Map<?, ?>) cardsOf(view).get(index);
  }
}
