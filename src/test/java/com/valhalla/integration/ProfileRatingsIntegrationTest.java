package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.rating.PlanRatingService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * [MYRAT] Integration coverage of "Mis calificaciones" (docs/specs/26-MYRAT.md, T-MYRAT-002..004):
 * la seccion del perfil propio contra la base real, el perfil ajeno que nunca la expone, el plan que
 * dejo de ser accesible y el estado vacio.
 *
 * <p>Los usuarios viven en la DB de test: {@code @WithMockUser} solo trae el email. Cada test corre
 * en su transaccion, asi que las calificaciones de un test no se filtran al siguiente.
 */
@WebIntegrationTest
@Transactional
public class ProfileRatingsIntegrationTest {

  private static final String SELF_EMAIL = "myrat.self@test.com";
  private static final String OTHER_EMAIL = "myrat.owner@test.com";
  private static final String PASSWORD = "la-password-de-siempre";
  private static final String PLAN_NAME = "Plan calificado de Grace";
  private static final String EMPTY_STATE = "Todavía no calificaste ningún plan";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlanService planService;

  @Autowired
  private PlanRatingService planRatingService;

  private MockMvc mockMvc;

  private UUID selfId;
  private UUID otherId;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();

    User self = user("Ada", "Lovelace", SELF_EMAIL);
    this.userRepository.save(self);
    this.selfId = self.getId();

    User other = user("Grace", "Hopper", OTHER_EMAIL);
    this.userRepository.save(other);
    this.otherId = other.getId();
  }

  // --- T-MYRAT-002: el perfil propio muestra la seccion, el ajeno no la expone ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void T_MYRAT_002_ownProfileShowsTheRatedPlanAndSomeoneElsesProfileNeverDoes()
    throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME);
    this.planRatingService.rate(planId, SELF_EMAIL, 4);

    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/edit"))
      .andExpect(content().string(containsString("id=\"my-ratings\"")))
      .andExpect(content().string(containsString("Mis calificaciones")))
      .andExpect(content().string(containsString(PLAN_NAME)))
      .andExpect(content().string(containsString("data-my-ratings-count>1<")))
      .andExpect(content().string(containsString("data-my-rating-row")))
      .andExpect(content().string(containsString("href=\"/plans/" + planId + "/public\"")))
      .andExpect(content().string(not(containsString(EMPTY_STATE))));

    // AC-09: el perfil de otro usuario no renderiza la seccion ni expone calificaciones ajenas.
    // El texto "Mis calificaciones" SI aparece: es el item del menu del navbar (global, linkea al
    // perfil propio). Lo que no puede aparecer es la seccion ni sus filas.
    this.mockMvc.perform(get("/profile/" + this.otherId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/view"))
      .andExpect(content().string(not(containsString("id=\"my-ratings\""))))
      .andExpect(content().string(not(containsString("data-my-rating-row"))))
      .andExpect(content().string(not(containsString("data-my-ratings-count"))))
      .andExpect(content().string(not(containsString(EMPTY_STATE))));
  }

  // --- T-MYRAT-003: un plan que paso a privado ajeno queda sin link y con badge ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void T_MYRAT_003_aPlanThatBecamePrivateIsMarkedUnavailableWithoutALink() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME);
    this.planRatingService.rate(planId, SELF_EMAIL, 2);

    Plan live = this.planRepository.findById(planId).orElseThrow();
    live.setIsPublic(false);
    this.planRepository.save(live);

    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(PLAN_NAME)))
      .andExpect(content().string(containsString("data-my-rating-badge")))
      .andExpect(content().string(containsString("No disponible")))
      .andExpect(content().string(not(containsString("href=\"/plans/" + planId + "\""))))
      .andExpect(content().string(not(containsString("href=\"/plans/" + planId + "/public\""))));
  }

  // --- T-MYRAT-004: estado vacio cuando no califico ningun plan ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void T_MYRAT_004_anEmptyHistoryShowsTheEmptyStateWithTheForYouLink() throws Exception {
    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(EMPTY_STATE)))
      .andExpect(content().string(containsString("data-my-ratings-empty")))
      .andExpect(content().string(containsString("href=\"/for-you\"")))
      .andExpect(content().string(containsString("data-my-ratings-count>0<")))
      .andExpect(content().string(not(containsString("data-my-rating-row"))));
  }

  // --- T-MYRAT-008: el indice del perfil linkea al ancla de la seccion ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void T_MYRAT_008_theNavbarMenuRendersTheSectionAnchor() throws Exception {
    String page =
      this.mockMvc.perform(get("/for-you"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(page, containsString("id=\"menu-my-ratings\""));
    assertThat(page, containsString("href=\"/profile#my-ratings\""));
  }

  // --- helpers ---

  private User user(String firstName, String lastName, String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword(
      new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(PASSWORD)
    );
    user.setRole("USER");
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setActive(true);
    return user;
  }

  private UUID givenPublicPlan(String name) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setDescription("Descripcion de " + name);
    plan.setEventDate(LocalDate.now().plusDays(10));
    plan.setIsPublic(true);
    plan.setAdministrator(this.userRepository.findById(this.otherId).orElseThrow());
    return this.planService.createPlan(plan, OTHER_EMAIL).getId();
  }
}
