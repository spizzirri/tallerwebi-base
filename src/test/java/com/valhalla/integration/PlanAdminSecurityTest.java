package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.presentation.shared.LoginRedirects;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * [PABM] AC-15: la cadena /admin/** de SecurityConfig es la duena del acceso a /admin/plans*. Un
 * USER comun es redirigido a su landing (igual que /admin/places, no recibe 403) y ninguna de sus
 * requests llega al controller: se verifica ademas que no persiste nada.
 */
@WebIntegrationTest
@Transactional
public class PlanAdminSecurityTest {

  private static final String USER_EMAIL = "user.pabm@unlam.edu.ar";
  private static final String ADMIN_EMAIL = "admin.pabm@unlam.edu.ar";
  private static final String PARTICIPANT_EMAIL = "participante.pabm@unlam.edu.ar";
  private static final String OUTSIDER_EMAIL = "externo.pabm@unlam.edu.ar";
  private static final String INACTIVE_NAME = "Plan inactivo PABM";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter(new HiddenHttpMethodFilter())
        .build();
  }

  // --- S-01 ---

  @Test
  public void anonymousTrafficIsSentToLoginFromThePlansAbm() throws Exception {
    this.mockMvc.perform(get("/admin/plans"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/admin/login"));
  }

  // --- S-02 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void aCommonUserIsRedirectedAwayFromThePlansAbm() throws Exception {
    this.mockMvc.perform(get("/admin/plans"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));
  }

  // --- S-03 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void aCommonUserPostingACreateDoesNotPersistAnything() throws Exception {
    int before = planRepository.findAll().size();

    this.mockMvc.perform(
        post("/admin/plans")
          .with(csrf())
          .param("name", "Intento de usuario")
          .param("eventDate", "2026-12-01")
      )
      .andExpect(status().is3xxRedirection());

    assertThat(planRepository.findAll().size(), is(before));
  }

  // --- S-04 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void aCommonUserCannotDeleteAPlan() throws Exception {
    Plan plan = givenPlan("Plan protegido PABM", true);

    this.mockMvc.perform(post("/admin/plans/{id}/delete", plan.getId()).with(csrf()))
      .andExpect(status().is3xxRedirection());

    assertThat(planRepository.findById(plan.getId()).isPresent(), is(true));
  }

  // --- S-05 ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void aCommonUserCannotToggleThePlanActiveFlag() throws Exception {
    Plan plan = givenPlan("Plan activo PABM", true);

    this.mockMvc.perform(
        post("/admin/plans/{id}/toggle-active", plan.getId()).with(csrf()).param("active", "false")
      )
      .andExpect(status().is3xxRedirection());

    assertThat(planRepository.findById(plan.getId()).orElseThrow().getActive(), is(true));
  }

  // --- S-06 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void anAdminReachesThePlansAbm() throws Exception {
    this.mockMvc.perform(get("/admin/plans")).andExpect(status().isOk());
  }

  // --- S-07 ---

  @Test
  public void anInactivePlanIsNotReadableByAnOutsider() throws Exception {
    Plan plan = givenPlan(INACTIVE_NAME, false);
    givenUser(OUTSIDER_EMAIL);

    ResultActions result =
      this.mockMvc.perform(
          get("/plans/{id}", plan.getId()).with(user(OUTSIDER_EMAIL).roles("USER"))
        );

    // AC-14: el plan inactivo no es accesible para quien no participa; la respuesta no muestra el
    // contenido del plan (404 o redirect, segun la ruta).
    result.andExpect(status().is(not(200)));
    assertThat(
      result.andReturn().getResponse().getContentAsString().contains(INACTIVE_NAME),
      is(false)
    );
  }

  // --- S-08 ---

  @Test
  public void anInactivePlanStaysReadableByItsParticipants() throws Exception {
    Plan plan = givenPlan(INACTIVE_NAME, false);
    User participant = givenUser(PARTICIPANT_EMAIL);
    plan.getParticipants().add(participant);
    planRepository.save(plan);

    this.mockMvc.perform(
        get("/plans/{id}", plan.getId()).with(user(PARTICIPANT_EMAIL).roles("USER"))
      )
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(INACTIVE_NAME)));
  }

  // --- helpers ---

  private Plan givenPlan(String name, boolean active) {
    User owner = givenUser("dueno." + UUID.randomUUID() + "@unlam.edu.ar");
    Plan plan = new Plan();
    plan.setName(name);
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    plan.setIsPublic(true);
    plan.setAdministrator(owner);
    plan.setShortCode(
      UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT)
    );
    plan.setActive(active);
    return planRepository.save(plan);
  }

  private User givenUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("x");
    user.setRole("USER");
    user.setFirstName("Test");
    user.setLastName("User");
    user.activate();
    userRepository.save(user);
    return userRepository.findByEmail(email).orElseThrow();
  }
}
