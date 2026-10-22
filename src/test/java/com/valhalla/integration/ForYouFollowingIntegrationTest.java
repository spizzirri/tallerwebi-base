package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [FOLLOW] El scope Siguiendo de {@code GET /for-you?scope=following}: escenarios I-07..I-10 de
 * docs/specs/19-FOLLOW.md.
 *
 * <p>El scope elige el conjunto (autores seguidos) y reusa los filtros y el orden de [SEARCH]; por
 * eso cada caso compara contra un autor que NO se sigue, y los planes se crean aca mismo: el
 * resultado no depende del seeder ni del orden de otras suites.
 */
@WebIntegrationTest
@Transactional
public class ForYouFollowingIntegrationTest {

  private static final String VIEWER_EMAIL = "feed.viewer@test.com";
  private static final String AUTHOR_EMAIL = "feed.author@test.com";
  private static final String STRANGER_EMAIL = "feed.stranger@test.com";

  private static final String PUBLIC_PLAN_NAME = "Plan publico del seguido";
  private static final String PRIVATE_PLAN_NAME = "Plan privado del seguido";
  private static final String STRANGER_PLAN_NAME = "Plan publico de un ajeno";

  private static final String FOLLOWING_URL = "/for-you?scope=following";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlaceRepository placeRepository;

  @Autowired
  private FollowService followService;

  private MockMvc mockMvc;

  private UUID viewerId;
  private UUID authorId;
  private UUID strangerId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    this.viewerId = givenUser(VIEWER_EMAIL, "Ana", "Seguidora");
    this.authorId = givenUser(AUTHOR_EMAIL, "Bruno", "Autor");
    this.strangerId = givenUser(STRANGER_EMAIL, "Carla", "Ajena");
  }

  // --- I-07 ---

  @Test
  public void I07_elScopeListaSoloPlanesPublicosDeLosAutoresSeguidos() throws Exception {
    // given: el viewer sigue solo al autor, y hay un plan publico de cada uno
    givenFollow();
    UUID followedPlan = givenPublicPlan(this.authorId, PUBLIC_PLAN_NAME, null, null);
    givenPublicPlan(this.strangerId, STRANGER_PLAN_NAME, null, null);

    // when
    String html =
      this.mockMvc.perform(get(FOLLOWING_URL).with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/plans/public-list"))
        .andExpect(model().attribute("followingScope", true))
        .andExpect(model().attribute("followsNobody", false))
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-07, AC-08): el conjunto es el de los seguidos, con el mismo payload del feed
    assertThat(html, containsString(PUBLIC_PLAN_NAME));
    assertThat(html, containsString("href=\"/plans/" + followedPlan + "/public\""));
    assertThat(html, not(containsString(STRANGER_PLAN_NAME)));
    assertThat(html, not(containsString("data-testid=\"following-empty\"")));
  }

  // --- I-08 ---

  @Test
  public void I08_filtrosYOrdenAplicanSobreElConjuntoDeSeguidos() throws Exception {
    // given: el autor seguido publica tres planes, uno por cada eje de filtro
    givenFollow();
    LocalDate today = LocalDate.now();
    givenPublicPlan(this.authorId, "Salida al museo", today.minusDays(2), PlaceCategory.MUSEUM);
    givenPublicPlan(this.authorId, "Cena en el barrio", today.plusDays(2), PlaceCategory.BAR);
    givenPublicPlan(this.authorId, "Plan sin fecha", null, null);

    // when: texto
    String porTexto = followingHtml("&q=museo");

    // then (AC-08)
    assertThat(porTexto, containsString("Salida al museo"));
    assertThat(porTexto, not(containsString("Cena en el barrio")));

    // when: categoria del lugar del itinerario
    String porCategoria = followingHtml("&category=BAR");

    // then (AC-08)
    assertThat(porCategoria, containsString("Cena en el barrio"));
    assertThat(porCategoria, not(containsString("Salida al museo")));

    // when: ventana de fecha
    String porFecha = followingHtml("&when=past");

    // then (AC-08)
    assertThat(porFecha, containsString("Salida al museo"));
    assertThat(porFecha, not(containsString("Cena en el barrio")));
    assertThat(porFecha, not(containsString("Plan sin fecha")));

    // when: orden por fecha ascendente, sin fecha al final
    String porOrden = followingHtml("&sort=date");

    // then (AC-08)
    assertThat(
      porOrden.indexOf("Salida al museo") < porOrden.indexOf("Cena en el barrio"),
      is(true)
    );
    assertThat(
      porOrden.indexOf("Cena en el barrio") < porOrden.indexOf("Plan sin fecha"),
      is(true)
    );
  }

  // --- I-09 ---

  @Test
  public void I09_unUsuarioSinSeguidosVeElEstadoVacioPropio() throws Exception {
    // given: el viewer no sigue a nadie (y hay planes publicos en el feed general)
    givenPublicPlan(this.strangerId, STRANGER_PLAN_NAME, null, null);

    // when
    String html =
      this.mockMvc.perform(get(FOLLOWING_URL).with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("followingScope", true))
        .andExpect(model().attribute("followsNobody", true))
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then (AC-09): estado propio con salida al feed completo, distinto de los otros dos
    assertThat(html, containsString("data-testid=\"following-empty\""));
    assertThat(html, containsString("Todavía no seguís a nadie."));
    assertThat(html, containsString("href=\"/for-you\""));
    assertThat(html, not(containsString("data-testid=\"following-no-plans\"")));
    assertThat(html, not(containsString("Todavía no hay planes públicos.")));
    assertThat(html, not(containsString(STRANGER_PLAN_NAME)));
  }

  // --- I-10 ---

  @Test
  public void I10_elScopeNoIncluyePlanesPrivadosDeLosSeguidosNiPlanesPropios() throws Exception {
    // given: el autor seguido tiene un plan publico y uno privado, y el viewer tiene uno propio
    givenFollow();
    givenPublicPlan(this.authorId, PUBLIC_PLAN_NAME, null, null);
    givenPlan(this.authorId, PRIVATE_PLAN_NAME, false);
    givenPublicPlan(this.viewerId, "Plan propio del viewer", null, null);

    // when
    String html = followingHtml("");

    // then (AC-08, AC-11): seguir no relaja la visibilidad ni la exclusion de los propios
    assertThat(html, containsString(PUBLIC_PLAN_NAME));
    assertThat(html, not(containsString(PRIVATE_PLAN_NAME)));
    assertThat(html, not(containsString("Plan propio del viewer")));
  }

  /** [FOLLOW] El viewer sigue al autor: la relacion se arma por el servicio, no por el HTML. */
  private void givenFollow() {
    this.followService.follow(this.viewerId, this.authorId);
  }

  /** [FOLLOW] HTML del scope Siguiendo pedido por el usuario de la sesion. */
  private String followingHtml(String extraQuery) throws Exception {
    return this.mockMvc.perform(get(FOLLOWING_URL + extraQuery).with(user(VIEWER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/plans/public-list"))
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private UUID givenUser(String email, String firstName, String lastName) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.activate();
    this.userRepository.save(user);
    return this.userRepository.findByEmail(email).orElseThrow().getId();
  }

  private UUID givenPublicPlan(
    UUID administratorId,
    String name,
    LocalDate eventDate,
    PlaceCategory category
  ) {
    return givenPlan(administratorId, name, eventDate, category, true);
  }

  private UUID givenPlan(UUID administratorId, String name, boolean isPublic) {
    return givenPlan(administratorId, name, null, null, isPublic);
  }

  /**
   * [SEARCH] El plan no tiene categoria propia: matchea por la categoria del lugar de su
   * itinerario, igual que el fixture del feed general.
   */
  private UUID givenPlan(
    UUID administratorId,
    String name,
    LocalDate eventDate,
    PlaceCategory category,
    boolean isPublic
  ) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setEventDate(eventDate);
    plan.setIsPublic(isPublic);
    plan.setAdministrator(this.userRepository.findById(administratorId).orElseThrow());
    if (category != null) {
      Place place = new Place();
      place.setName("Lugar " + category + " de " + name);
      place.setCategory(category);
      this.placeRepository.save(place);
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setSortOrder(1);
      plan.addPlanPlace(entry);
    }
    return this.planRepository.save(plan).getId();
  }
}
