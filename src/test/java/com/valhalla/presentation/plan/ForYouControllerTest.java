package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
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

  private ForYouController controller;
  private PlanService planServiceMock;
  private Authentication authentication;

  @BeforeEach
  public void init() {
    this.planServiceMock = mock(PlanService.class);
    this.controller = new ForYouController(this.planServiceMock);
    this.authentication = new UsernamePasswordAuthenticationToken(OWNER_EMAIL, null);
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
    ModelAndView view = this.controller.forYou(this.authentication);

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
    ModelAndView view = this.controller.forYou(this.authentication);

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
    ModelAndView view = this.controller.forYou(this.authentication);

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
    ModelAndView view = this.controller.forYou(anon);

    // then: el visitante no tiene planes propios, el filtro no le saca nada
    assertThat(cardsOf(view), hasSize(1));
  }

  @Test
  public void U02d_forYou_sinPrincipalElFiltroDePropiosNoTiraNada() {
    // given: cadena de seguridad sin anonymous habilitado deja el principal en null
    Plan plan = plan(PLAN_ID, "Plan visible");
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    ModelAndView view = this.controller.forYou(null);

    // then
    assertThat(cardsOf(view), hasSize(1));
  }

  @Test
  public void U03_forYou_conLaBaseVaciaDevuelveLaListaVaciaSinError() {
    // given
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of());

    // when
    ModelAndView view = this.controller.forYou(this.authentication);

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
    ModelAndView view = this.controller.forYou(this.authentication);
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
  public void U04b_forYou_unAdministradorSinNombreNoFiltraSuEmail() {
    // given: el seed de E2E reinserta usuarios sin nombre, y el listado no puede filtrar el email
    Plan plan = plan(PLAN_ID, "Plan con administrador sin nombre");
    User administrator = new User();
    administrator.setEmail("anonimo@privado.com");
    plan.setAdministrator(administrator);
    when(this.planServiceMock.getPublicPlans()).thenReturn(List.of(plan));

    // when
    Map<?, ?> card = cardAt(this.controller.forYou(this.authentication), 0);

    // then (AC-09): el nombre de administrador queda vacio, nunca el email
    assertThat(card.get("administratorName"), is(equalTo("")));
  }

  private Plan plan(UUID id, String name) {
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
}
