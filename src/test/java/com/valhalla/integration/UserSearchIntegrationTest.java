package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.follow.FollowService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.presentation.user.UserSearchResult;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [USEARCH] Buscador de personas contra la cadena real de seguridad y HSQLDB: sesion obligatoria,
 * coincidencias por nombre/apellido, estado del grafo en el boton y los estados vacios.
 */
@WebIntegrationTest
@Transactional
public class UserSearchIntegrationTest {

  private static final String VIEWER_EMAIL = "search.viewer@test.com";
  private static final String JOSE_EMAIL = "search.jose@test.com";
  private static final String JOSEFINA_EMAIL = "search.josefina@test.com";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private FollowService followService;

  private MockMvc mockMvc;

  private UUID viewerId;
  private UUID joseId;
  private UUID josefinaId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    this.viewerId = givenUser(VIEWER_EMAIL, "Rosa", "Seguidora");
    this.joseId = givenUser(JOSE_EMAIL, "José", "Álvarez");
    this.josefinaId = givenUser(JOSEFINA_EMAIL, "Josefina", "Pérez");

    givenPlan("Plan publico de Jose", this.joseId, true);
    givenPlan("Borrador privado de Jose", this.joseId, false);
    givenPlan("Plan publico de Josefina", this.josefinaId, true);
  }

  // --- T-USEARCH-002 ---

  @Test
  public void I01_searchRequiresASessionAndRedirectsToLogin() throws Exception {
    this.mockMvc.perform(get("/users").param("q", "jose"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));
  }

  @Test
  public void I02_searchReturnsTheMatchesAndExcludesTheSessionUser() throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/users").param("q", "JOSE").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/users/search"))
        .andExpect(model().attribute("q", "JOSE"))
        .andReturn();

    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString("José Álvarez"));
    assertThat(html, containsString("Josefina Pérez"));

    @SuppressWarnings("unchecked")
    List<UserSearchResult> results = (List<UserSearchResult>) result
      .getModelAndView()
      .getModel()
      .get("results");
    assertThat(results, hasSize(2));
    UserSearchResult jose = resultFor(results, this.joseId);
    assertThat(jose.publicPlanCount(), is(1L));
    assertThat(jose.following(), is(false));

    // La propia persona no aparece aunque su nombre matchee.
    this.mockMvc.perform(get("/users").param("q", "rosa").with(user(VIEWER_EMAIL)))
      .andExpect(status().isOk())
      .andExpect(content().string(not(containsString("Rosa Seguidora"))));
  }

  // --- T-USEARCH-003 ---

  @Test
  public void I03_theButtonReflectsTheExistingFollowGraph() throws Exception {
    // given: el visitante ya sigue a Jose, pero no a Josefina
    this.followService.follow(this.viewerId, this.joseId);

    // when
    String html =
      this.mockMvc.perform(get("/users").param("q", "jose").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // then: cada fila rinde la accion inversa y el label correcto
    assertThat(html, containsString("/users/" + this.joseId + "/unfollow"));
    assertThat(html, containsString("data-following=\"true\""));
    assertThat(html, containsString("Siguiendo"));

    assertThat(html, containsString("/users/" + this.josefinaId + "/follow"));
    assertThat(html, containsString("data-following=\"false\""));
    assertThat(html, containsString("Seguir"));
  }

  // --- T-USEARCH-004 ---

  @Test
  public void I04_followingFromAResultPersistsAndReturnsToTheSameSearch() throws Exception {
    // when
    this.mockMvc.perform(
        post("/users/" + this.joseId + "/follow")
          .param("q", "Jose")
          .with(user(VIEWER_EMAIL))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/users?q=Jose"));

    // then
    assertThat(this.followService.isFollowing(this.viewerId, this.joseId), is(true));

    // when: el mismo boton ahora deja de seguir
    this.mockMvc.perform(
        post("/users/" + this.joseId + "/unfollow")
          .param("q", "Jose")
          .with(user(VIEWER_EMAIL))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/users?q=Jose"));

    // then
    assertThat(this.followService.isFollowing(this.viewerId, this.joseId), is(false));
  }

  // --- T-USEARCH-005 ---

  @Test
  public void I05_emptyQueryShowsTheInitialStateAndNeverListsEveryone() throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/users").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/users/search"))
        .andReturn();

    String html = result.getResponse().getContentAsString();
    assertThat(html, containsString("data-search-idle"));
    assertThat(html, not(containsString("José Álvarez")));
    @SuppressWarnings("unchecked")
    List<UserSearchResult> results = (List<UserSearchResult>) result
      .getModelAndView()
      .getModel()
      .get("results");
    assertThat(results, hasSize(0));
  }

  @Test
  public void I06_noResultsShowsTheQueryAndThePlanSuggestion() throws Exception {
    String html =
      this.mockMvc.perform(get("/users").param("q", "Nadie").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("q", "Nadie"))
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("data-search-empty"));
    assertThat(html, containsString("No encontramos usuarios para"));
    assertThat(html, containsString("Nadie"));
    assertThat(html, containsString("llegar a una persona desde un plan"));
    assertThat(html, not(containsString("José Álvarez")));
  }

  @Test
  public void I07_aWildcardOnlyQueryCannotListEveryone() throws Exception {
    String html =
      this.mockMvc.perform(get("/users").param("q", "%").with(user(VIEWER_EMAIL)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, containsString("data-search-empty"));
    assertThat(html, not(containsString("José Álvarez")));
  }

  private static UserSearchResult resultFor(List<UserSearchResult> results, UUID id) {
    return results.stream().filter(result -> id.equals(result.id())).findFirst().orElseThrow();
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

  private void givenPlan(String name, UUID administratorId, boolean isPublic) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setDescription("Plan de prueba para el buscador");
    plan.setAdministrator(this.userRepository.findById(administratorId).orElseThrow());
    plan.setIsPublic(isPublic);
    plan.setShortCode(UUID.randomUUID().toString().substring(0, 8));
    plan.setEventDate(LocalDate.now().plusDays(30));
    this.planRepository.save(plan);
  }
}
