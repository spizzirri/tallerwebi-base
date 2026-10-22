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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanPermission;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
public class PlanJoinRequestSecurityTest {

  private static final String OWNER = "owner.pet@test.com";
  private static final String REQUESTER = "requester.pet@test.com";
  private static final String EDITOR = "editor.pet@test.com";
  private static final String NAME = "Encuentro privado PET";
  private static final String NOT_FOUND = "/plans?error=Plan+not+found";

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository users;

  @Autowired
  private PlanRepository plans;

  @Autowired
  private EntityManager entityManager;

  private MockMvc mvc;
  private Plan plan;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    User owner = createUser(OWNER);
    createUser(REQUESTER);
    User editor = createUser(EDITOR);
    plan = new Plan();
    plan.setName(NAME);
    plan.setDescription("Resumen autorizado");
    plan.setShortCode("PETSEC01");
    plan.setAdministrator(owner);
    plan.getSettings().setShareLink(PlanPermission.EDITORS);
    plan.getParticipants().add(editor);
    plan.getEditors().add(editor);
    plans.save(plan);
    entityManager.flush();
  }

  @Test
  public void PET_S01_anonymousLandingRedirectsToLoginAndPostsRequireAuthentication()
    throws Exception {
    mvc.perform(get(landing())).andExpect(redirectedUrl("/auth/login?redirect=" + landing()));
    mvc.perform(post(requests()).with(csrf())).andExpect(redirectedUrlPattern("**/auth/login"));
    assertThat(plan.getJoinRequests(), hasSize(0));
  }

  @Test
  public void PET_S02_landingExposesSummaryWithoutMembershipOrShareCode() throws Exception {
    mvc
      .perform(get(landing()).with(user(REQUESTER)))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(NAME)))
      .andExpect(content().string(not(containsString(OWNER))))
      .andExpect(content().string(not(containsString(EDITOR))))
      .andExpect(content().string(not(containsString(plan.getShortCode()))));
    mvc
      .perform(get("/plans/" + plan.getId()).with(user(REQUESTER)))
      .andExpect(redirectedUrl(NOT_FOUND));
  }

  @Test
  public void PET_S03_disabledLinkHidesSummaryAndRejectsNewRequests() throws Exception {
    plan.getSettings().setShareLink(PlanPermission.OWNER);
    mvc
      .perform(get(landing()).with(user(REQUESTER)))
      .andExpect(status().isOk())
      .andExpect(content().string(not(containsString(NAME))))
      .andExpect(content().string(not(containsString("Resumen autorizado"))))
      .andExpect(content().string(not(containsString("Solicitar participación</button>"))));
    mvc
      .perform(post(requests()).with(user(REQUESTER)).with(csrf()))
      .andExpect(redirectedUrl(NOT_FOUND));
    assertThat(plan.getJoinRequests(), hasSize(0));
  }

  @Test
  public void PET_S04_requestsAndResponsesRequireCsrf() throws Exception {
    mvc.perform(post(requests()).with(user(REQUESTER))).andExpect(status().isForbidden());
    assertThat(plan.getJoinRequests(), hasSize(0));
    submitRequest();
    mvc
      .perform(post(response()).with(user(OWNER)).param("action", "accept"))
      .andExpect(status().isForbidden());
    assertThat(plan.isParticipant(REQUESTER), is(false));
    assertThat(plan.getJoinRequests(), hasSize(1));
  }

  @Test
  public void PET_S05_requestIdentityComesFromSessionAndVisitorCannotRespond() throws Exception {
    mvc
      .perform(post(requests()).with(user(REQUESTER)).with(csrf()).param("userEmail", OWNER))
      .andExpect(redirectedUrl(landing()));
    assertThat(plan.getJoinRequests().getFirst().getRequester().getEmail(), is(REQUESTER));
    mvc
      .perform(post(response()).with(user(REQUESTER)).with(csrf()).param("action", "accept"))
      .andExpect(redirectedUrl(NOT_FOUND));
    assertThat(plan.getJoinRequests(), hasSize(1));
    assertThat(plan.isParticipant(REQUESTER), is(false));
  }

  @Test
  public void PET_S06_editorNeedsDelegationAndCannotChangeSettings() throws Exception {
    submitRequest();
    mvc
      .perform(post(response()).with(user(EDITOR)).with(csrf()).param("action", "accept"))
      .andExpect(redirectedUrl(NOT_FOUND));
    mvc
      .perform(get("/plans/" + plan.getId() + "/settings").with(user(EDITOR)))
      .andExpect(redirectedUrl(NOT_FOUND));
    mvc
      .perform(
        post("/plans/" + plan.getId() + "/settings")
          .with(user(EDITOR))
          .with(csrf())
          .param("shareLink", "PARTICIPANTS")
          .param("handleRequests", "EDITORS")
          .param("inviteInternal", "PARTICIPANTS")
          .param("inviteByEmail", "PARTICIPANTS")
      )
      .andExpect(redirectedUrl(NOT_FOUND));
    assertThat(plan.getSettings().getHandleRequests(), is(PlanPermission.OWNER));
    plan.getSettings().setHandleRequests(PlanPermission.EDITORS);
    mvc
      .perform(post(response()).with(user(EDITOR)).with(csrf()).param("action", "accept"))
      .andExpect(redirectedUrl("/plans/" + plan.getId()));
    assertThat(plan.isParticipant(REQUESTER), is(true));
  }

  @Test
  public void PET_duplicateRequestPersistsOnceAndAcceptGrantsAccess() throws Exception {
    submitRequest();
    submitRequest();
    entityManager.flush();
    entityManager.clear();
    plan = plans.findById(plan.getId()).orElseThrow();
    assertThat(plan.getJoinRequests(), hasSize(1));
    mvc
      .perform(get(landing()).with(user(REQUESTER)))
      .andExpect(content().string(containsString("Ya enviaste tu solicitud")));
    mvc
      .perform(post(response()).with(user(OWNER)).with(csrf()).param("action", "accept"))
      .andExpect(redirectedUrl("/plans/" + plan.getId()));
    entityManager.flush();
    entityManager.clear();
    plan = plans.findById(plan.getId()).orElseThrow();
    assertThat(plan.getJoinRequests(), hasSize(0));
    assertThat(plan.isParticipant(REQUESTER), is(true));
    assertThat(plan.isEditor(REQUESTER), is(false));
    mvc
      .perform(get(landing()).with(user(REQUESTER)))
      .andExpect(redirectedUrl("/plans/" + plan.getId()));
    mvc.perform(get("/plans/" + plan.getId()).with(user(REQUESTER))).andExpect(status().isOk());
  }

  @Test
  public void PET_rejectionRemovesRequestAndAllowsRetry() throws Exception {
    submitRequest();
    mvc
      .perform(post(response()).with(user(OWNER)).with(csrf()).param("action", "reject"))
      .andExpect(redirectedUrl("/plans/" + plan.getId()));
    entityManager.flush();
    entityManager.clear();
    plan = plans.findById(plan.getId()).orElseThrow();
    assertThat(plan.getJoinRequests(), hasSize(0));
    assertThat(plan.isParticipant(REQUESTER), is(false));
    submitRequest();
    assertThat(plan.getJoinRequests(), hasSize(1));
  }

  @Test
  public void PET_disablingLinkPreservesExistingRequestForOwner() throws Exception {
    submitRequest();
    plan.getSettings().setShareLink(PlanPermission.OWNER);
    mvc
      .perform(post(response()).with(user(OWNER)).with(csrf()).param("action", "accept"))
      .andExpect(redirectedUrl("/plans/" + plan.getId()));
    assertThat(plan.isParticipant(REQUESTER), is(true));
  }

  private void submitRequest() throws Exception {
    mvc
      .perform(post(requests()).with(user(REQUESTER)).with(csrf()))
      .andExpect(redirectedUrl(landing()));
    entityManager.flush();
  }

  private String landing() {
    return "/plans/" + plan.getId() + "/public";
  }

  private String requests() {
    return "/plans/" + plan.getId() + "/join-requests";
  }

  private String response() {
    return requests() + "/" + plan.getJoinRequests().getFirst().getId() + "/respond";
  }

  private User createUser(String email) {
    User account = new User();
    account.setEmail(email);
    account.setPassword("password123");
    account.setRole("USER");
    account.setFirstName("Usuario PET");
    account.setLastName("Prueba");
    users.save(account);
    return account;
  }
}
