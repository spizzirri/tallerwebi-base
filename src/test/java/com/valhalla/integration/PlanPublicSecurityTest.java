package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Security contract of {@code GET /plans/{id}/public} [PVP], scenarios S-01..S-04 of
 * 13-PVP.md. The endpoint is {@code permitAll} for GET only and must not become an oracle of
 * ids nor a leak of private data.
 *
 * <p>Every test name carries the {@code PVP_} prefix: another branch adds a feed-oriented class
 * under this same FQN, so the prefix keeps both sets distinguishable after the merge.
 */
@WebIntegrationTest
@Transactional
public class PlanPublicSecurityTest {

  private static final String OWNER_EMAIL = "dueno-pvp@test.com";
  private static final String PARTICIPANT_EMAIL = "participante-pvp@test.com";
  private static final String PUBLIC_PLAN_NAME = "Plan publico PVP";
  private static final String PRIVATE_PLAN_NAME = "Plan privado PVP";
  private static final String PUBLIC_SHORT_CODE = "PVPBLIC01";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  private MockMvc mvc;
  private Long publicPlanId;
  private Long privatePlanId;

  @BeforeEach
  public void setUp() {
    this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();

    User owner = new User();
    owner.setEmail(OWNER_EMAIL);
    owner.setPassword("password123");
    owner.setRole("USER");
    owner.setFirstName("Dueno");
    owner.setLastName("PVP");
    this.userRepository.save(owner);

    User participant = new User();
    participant.setEmail(PARTICIPANT_EMAIL);
    participant.setPassword("password123");
    participant.setRole("USER");
    participant.setFirstName("Participante");
    participant.setLastName("PVP");
    this.userRepository.save(participant);

    this.publicPlanId = givenPlan(PUBLIC_PLAN_NAME, true);
    Plan publicPlan = this.planRepository.findById(this.publicPlanId).orElseThrow();
    publicPlan.setShortCode(PUBLIC_SHORT_CODE);
    publicPlan.getParticipants().add(participant);
    this.planRepository.save(publicPlan);

    this.privatePlanId = givenPlan(PRIVATE_PLAN_NAME, false);
  }

  /** S-01 (AC-09): the GET is permitAll — 200 without a session and without a login redirect. */
  @Test
  public void PVP_S01_getPlanPublicoAnonimoResponde200SinRedirigir() throws Exception {
    this.mvc.perform(get("/plans/" + this.publicPlanId + "/public")).andExpect(status().isOk());
  }

  /** S-02 (AC-04): a private plan answers exactly like a missing id, never 200 with content. */
  @Test
  public void PVP_S02_getPlanPrivadoAnonimoNoResponde200ConContenido() throws Exception {
    MvcResult privateResult =
      this.mvc.perform(get("/plans/" + this.privatePlanId + "/public"))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    MvcResult missingResult =
      this.mvc.perform(get("/plans/999999/public"))
        .andExpect(status().is3xxRedirection())
        .andReturn();

    assertThat(
      privateResult.getResponse().getRedirectedUrl(),
      is(missingResult.getResponse().getRedirectedUrl())
    );
    assertThat(
      privateResult.getResponse().getContentAsString(),
      not(containsString(PRIVATE_PLAN_NAME))
    );
  }

  /** S-03 (AC-10): permitAll covers GET only — a POST still goes through the login gate. */
  @Test
  public void PVP_S03_postPlanPublicoAnonimoRedirigeALogin() throws Exception {
    this.mvc.perform(post("/plans/" + this.publicPlanId + "/public").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login"));
  }

  /** S-04 (AC-04): no participant emails, no shortCode and no private-plan data in the HTML. */
  @Test
  public void PVP_S04_getPlanPublicoNoFiltraEmailsNiDatosDePlanesPrivados() throws Exception {
    String html =
      this.mvc.perform(get("/plans/" + this.publicPlanId + "/public"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(html, not(containsString(PARTICIPANT_EMAIL)));
    assertThat(html, not(containsString(OWNER_EMAIL)));
    assertThat(html, not(containsString(PUBLIC_SHORT_CODE)));
    assertThat(html, not(containsString(PRIVATE_PLAN_NAME)));
    assertThat(html, containsString(PUBLIC_PLAN_NAME));
  }

  private Long givenPlan(String name, boolean isPublic) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setIsPublic(isPublic);
    plan.setAdministrator(this.userRepository.findByEmail(OWNER_EMAIL).orElseThrow());
    return this.planRepository.save(plan).getId();
  }
}
