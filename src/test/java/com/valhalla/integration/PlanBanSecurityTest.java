package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.exception.PlanNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanInvitation;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
@Transactional
public class PlanBanSecurityTest {

  private static final String OWNER = "owner.ban@test.com";
  private static final String EDITOR = "editor.ban@test.com";
  private static final String MEMBER = "member.ban@test.com";
  private static final String PLAN_NAME = "Plan bloqueado";

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private PlanRepository plans;

  @Autowired
  private UserRepository users;

  @Autowired
  private PlanService service;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private com.valhalla.domain.follow.FollowService followService;

  private MockMvc mvc;
  private UUID planId;

  @BeforeEach
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    User owner = createUser(OWNER);
    User editor = createUser(EDITOR);
    User member = createUser(MEMBER);
    Plan plan = new Plan();
    plan.setName(PLAN_NAME);
    plan.setDescription("Contenido que no debe exponerse");
    plan.setShortCode("BAN12345");
    plan.setIsPublic(true);
    plan.setAdministrator(owner);
    plan.getParticipants().addAll(List.of(editor, member));
    plan.getEditors().add(editor);
    planId = plans.save(plan).getId();
  }

  @Test
  public void editorCannotRemoveBanDemoteOwnerOrDeletePlan() throws Exception {
    for (String action : List.of("remove", "ban", "role")) {
      mvc
        .perform(
          post("/plans/{id}/participants/" + action, planId)
            .with(user(EDITOR))
            .with(csrf())
            .param("participantEmail", OWNER)
            .param("role", "guest")
        )
        .andExpect(status().is3xxRedirection());
    }
    mvc
      .perform(post("/plans/{id}/delete", planId).with(user(EDITOR)).with(csrf()))
      .andExpect(status().is3xxRedirection());
    entityManager.flush();
    entityManager.clear();
    Plan plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isAdministrator(OWNER), is(true));
    assertThat(plan.isBanned(OWNER), is(false));
    assertThat(plan.isEditor(EDITOR), is(true));
  }

  @Test
  public void banBlocksDetailPublicViewItineraryJoinAndClone() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    entityManager.flush();
    entityManager.clear();
    for (boolean publicPlan : List.of(true, false)) {
      Plan plan = plans.findById(planId).orElseThrow();
      plan.setIsPublic(publicPlan);
      plans.save(plan);
      for (String path : List.of("/plans/" + planId, "/plans/" + planId + "/public")) {
        mvc
          .perform(get(path).with(user(MEMBER)))
          .andExpect(status().isForbidden())
          .andExpect(content().string(containsString("Fuiste baneado")))
          .andExpect(content().string(not(containsString(plan.getDescription()))));
      }
      mvc
        .perform(get("/api/plans/{id}/places", planId).with(user(MEMBER)))
        .andExpect(status().isNotFound());
      mvc
        .perform(post("/plans/join").with(user(MEMBER)).with(csrf()).param("shortCode", "ban12345"))
        .andExpect(status().isForbidden());
      mvc
        .perform(post("/plans/{id}/clone", planId).with(user(MEMBER)).with(csrf()))
        .andExpect(status().isForbidden());
      assertThat(plan.isParticipant(MEMBER), is(false));
    }
  }

  @Test
  public void bannedPlanShowsNoticeWithoutDetailAndDisappearsFromFeeds() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    entityManager.flush();
    entityManager.clear();
    String html = mvc
      .perform(get("/plans").with(user(MEMBER)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);
    assertThat(html, containsString(PLAN_NAME));
    assertThat(html, containsString("baneo activo para tu cuenta"));
    assertThat(html, not(containsString("/plans/" + planId)));
    followService.follow(
      users.findByEmail(MEMBER).orElseThrow().getId(),
      users.findByEmail(OWNER).orElseThrow().getId()
    );
    for (String path : List.of("/for-you", "/for-you?scope=following", "/api/plans/public")) {
      mvc
        .perform(get(path).with(user(MEMBER)))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString(PLAN_NAME))));
    }
    mvc.perform(get("/plans/{id}/public", planId)).andExpect(status().isOk());
    mvc.perform(get("/plans/{id}", planId).with(user(OWNER))).andExpect(status().isOk());
  }

  @Test
  public void banRejectsPendingInvitationAcceptance() throws Exception {
    User member = users.findByEmail(MEMBER).orElseThrow();
    service.banParticipant(planId, MEMBER, OWNER);
    Plan plan = plans.findById(planId).orElseThrow();
    PlanInvitation invitation = new PlanInvitation(plan, member, plan.getAdministrator(), "editor");
    plan.getInvitations().add(invitation);
    Plan saved = plans.save(plan);
    entityManager.flush();
    UUID invitationId = saved.getInvitations().getFirst().getId();
    mvc
      .perform(
        post("/plans/{id}/invitations/{invitationId}/respond", planId, invitationId)
          .with(user(MEMBER))
          .with(csrf())
          .param("accept", "true")
      )
      .andExpect(status().isForbidden());
    assertThat(plan.isParticipant(MEMBER), is(false));
    assertThat(plan.isEditor(MEMBER), is(false));
  }

  @Test
  public void bannedParticipantsSectionIsOnlyVisibleToOwnerAndEditors() throws Exception {
    User member = users.findByEmail(MEMBER).orElseThrow();
    member.setFirstName("Persona");
    member.setLastName("Baneada");
    service.banParticipant(planId, MEMBER, OWNER);
    entityManager.flush();
    entityManager.clear();
    for (String manager : List.of(OWNER, EDITOR)) {
      mvc
        .perform(get("/plans/{id}", planId).with(user(manager)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"banned-participants\"")))
        .andExpect(content().string(containsString("Persona Baneada")))
        .andExpect(content().string(containsString("Quitar baneo a Persona Baneada")));
    }
    createUser("viewer.ban@test.com");
    service.joinPlan("BAN12345", "viewer.ban@test.com");
    for (String viewer : List.of("viewer.ban@test.com", "outsider@test.com")) {
      mvc
        .perform(get("/plans/{id}", planId).with(user(viewer)))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("id=\"banned-participants\""))))
        .andExpect(content().string(not(containsString(MEMBER))));
      assertThrows(
        PlanNotFoundException.class,
        () -> service.getBannedParticipants(planId, viewer)
      );
    }
    mvc
      .perform(get("/plans/{id}/public", planId).with(user(OWNER)))
      .andExpect(status().isOk())
      .andExpect(content().string(not(containsString(MEMBER))));
  }

  @Test
  public void ownerAndEditorCanUnbanWithoutRestoringMembershipOrEditorRole() throws Exception {
    for (String manager : List.of(OWNER, EDITOR)) {
      Plan plan = plans.findById(planId).orElseThrow();
      plan.setIsPublic(false);
      plan.getEditors().add(users.findByEmail(MEMBER).orElseThrow());
      plans.save(plan);
      service.banParticipant(planId, MEMBER, OWNER);
      mvc
        .perform(
          post("/plans/{id}/participants/unban", planId)
            .with(user(manager))
            .with(csrf())
            .param("participantEmail", MEMBER)
        )
        .andExpect(redirectedUrl("/plans/" + planId))
        .andExpect(flash().attributeExists("participantSuccess"));
      entityManager.flush();
      entityManager.clear();
      plan = plans.findById(planId).orElseThrow();
      assertThat(plan.isBanned(MEMBER), is(false));
      assertThat(plan.isParticipant(MEMBER), is(false));
      assertThat(plan.isEditor(MEMBER), is(false));
      assertThat(service.getBannedPlans(MEMBER).isEmpty(), is(true));
      mvc
        .perform(get("/plans/{id}", planId).with(user(MEMBER)))
        .andExpect(status().is3xxRedirection());
      mvc
        .perform(
          post("/plans/join").with(user(MEMBER)).with(csrf()).param("shortCode", " ban12345 ")
        )
        .andExpect(redirectedUrl("/plans/" + planId + "/public"));
      plan = plans.findById(planId).orElseThrow();
      assertThat(plan.isParticipant(MEMBER), is(false));
      service.respondToJoinRequest(
        planId,
        plan.getJoinRequests().getFirst().getId(),
        "accept",
        OWNER
      );
      entityManager.flush();
      entityManager.clear();
      plan = plans.findById(planId).orElseThrow();
      assertThat(plan.isParticipant(MEMBER), is(true));
      assertThat(plan.isEditor(MEMBER), is(false));
      mvc.perform(get("/plans/{id}", planId).with(user(MEMBER))).andExpect(status().isOk());
    }
  }

  @Test
  public void unauthorizedUsersCannotUnbanAndCsrfIsRequired() throws Exception {
    User viewer = createUser("viewer.ban@test.com");
    service.joinPlan("BAN12345", viewer.getEmail());
    service.banParticipant(planId, MEMBER, OWNER);
    for (String actor : List.of(viewer.getEmail(), "outsider@test.com", MEMBER)) {
      mvc
        .perform(
          post("/plans/{id}/participants/unban", planId)
            .with(user(actor))
            .with(csrf())
            .param("participantEmail", MEMBER)
        )
        .andExpect(actor.equals(MEMBER) ? status().isForbidden() : status().is3xxRedirection());
      assertThat(plans.findById(planId).orElseThrow().isBanned(MEMBER), is(true));
    }
    mvc
      .perform(
        post("/plans/{id}/participants/unban", planId)
          .with(user(OWNER))
          .param("participantEmail", MEMBER)
      )
      .andExpect(status().isForbidden());
    mvc
      .perform(
        post("/plans/{id}/participants/unban", planId)
          .with(csrf())
          .param("participantEmail", MEMBER)
      )
      .andExpect(status().is3xxRedirection());
    entityManager.flush();
    entityManager.clear();
    assertThat(plans.findById(planId).orElseThrow().isBanned(MEMBER), is(true));
  }

  @Test
  public void unbanOnlyAppliesToTheSelectedPlanAndRejectsUnknownBan() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    Plan other = new Plan();
    other.setName("Otro plan");
    other.setShortCode("OTHERBAN");
    other.setAdministrator(users.findByEmail(OWNER).orElseThrow());
    UUID otherId = plans.save(other).getId();
    mvc
      .perform(
        post("/plans/{id}/participants/unban", otherId)
          .with(user(OWNER))
          .with(csrf())
          .param("participantEmail", MEMBER)
      )
      .andExpect(redirectedUrl("/plans?error=Plan+not+found"));
    mvc
      .perform(
        post("/plans/{id}/participants/unban", planId)
          .with(user(OWNER))
          .with(csrf())
          .param("participantEmail", "unknown@test.com")
      )
      .andExpect(redirectedUrl("/plans?error=Plan+not+found"));
    entityManager.flush();
    entityManager.clear();
    assertThat(plans.findById(planId).orElseThrow().isBanned(MEMBER), is(true));
  }

  @Test
  public void unbannedUserCanReceiveAndAcceptANewInvitation() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    service.unbanParticipant(planId, MEMBER, EDITOR);
    service.inviteParticipant(planId, MEMBER, "guest", OWNER);
    entityManager.flush();
    UUID invitationId = plans.findById(planId).orElseThrow().getInvitations().getFirst().getId();
    mvc
      .perform(
        post("/plans/{id}/invitations/{invitationId}/respond", planId, invitationId)
          .with(user(MEMBER))
          .with(csrf())
          .param("accept", "true")
      )
      .andExpect(redirectedUrl("/plans"));
    entityManager.flush();
    entityManager.clear();
    Plan plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isBanned(MEMBER), is(false));
    assertThat(plan.isParticipant(MEMBER), is(true));
  }

  @Test
  public void banEndpointRequiresSessionCsrfAndEditPermission() throws Exception {
    for (String actor : List.of(MEMBER, "outsider@test.com")) {
      mvc
        .perform(
          post("/plans/{id}/participants/ban", planId)
            .with(user(actor))
            .with(csrf())
            .param("participantEmail", EDITOR)
        )
        .andExpect(status().is3xxRedirection());
    }
    mvc
      .perform(
        post("/plans/{id}/participants/ban", planId)
          .with(user(OWNER))
          .param("participantEmail", MEMBER)
      )
      .andExpect(status().isForbidden());
    mvc
      .perform(
        post("/plans/{id}/participants/ban", planId).with(csrf()).param("participantEmail", MEMBER)
      )
      .andExpect(status().is3xxRedirection());
    entityManager.flush();
    entityManager.clear();
    Plan plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(MEMBER), is(true));
    assertThat(plan.isEditor(EDITOR), is(true));
    assertThat(plan.getBannedUsers().isEmpty(), is(true));
  }

  @Test
  public void editorCanBanMemberThroughHttpAndOwnerCanBanEditor() throws Exception {
    mvc
      .perform(
        post("/plans/{id}/participants/ban", planId)
          .with(user(EDITOR))
          .with(csrf())
          .param("participantEmail", MEMBER)
      )
      .andExpect(redirectedUrl("/plans/" + planId))
      .andExpect(flash().attributeExists("participantSuccess"));
    mvc
      .perform(
        post("/plans/{id}/participants/ban", planId)
          .with(user(OWNER))
          .with(csrf())
          .param("participantEmail", EDITOR)
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    entityManager.flush();
    entityManager.clear();
    Plan plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isBanned(MEMBER), is(true));
    assertThat(plan.isBanned(EDITOR), is(true));
    assertThat(plan.isParticipant(MEMBER), is(false));
    assertThat(plan.isEditor(EDITOR), is(false));
  }

  @Test
  public void privateJoinRequestsCannotBypassBanThroughHttp() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    Plan plan = plans.findById(planId).orElseThrow();
    plan.setIsPublic(false);
    com.valhalla.domain.plan.PlanJoinRequest request = new com.valhalla.domain.plan.PlanJoinRequest(
      plan,
      users.findByEmail(MEMBER).orElseThrow()
    );
    plan.getJoinRequests().add(request);
    Plan saved = plans.save(plan);
    entityManager.flush();
    UUID requestId = saved.getJoinRequests().getFirst().getId();
    mvc
      .perform(post("/plans/{id}/join-requests", planId).with(user(MEMBER)).with(csrf()))
      .andExpect(status().isForbidden());
    mvc
      .perform(
        post("/plans/{id}/join-requests/{rid}/respond", planId, requestId)
          .with(user(OWNER))
          .with(csrf())
          .param("action", "accept")
      )
      .andExpect(status().isForbidden());
    entityManager.flush();
    entityManager.clear();
    plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isParticipant(MEMBER), is(false));
    assertThat(plan.getJoinRequests().size(), is(1));
    mvc
      .perform(
        post("/plans/{id}/join-requests/{rid}/respond", planId, requestId)
          .with(user(OWNER))
          .with(csrf())
          .param("action", "reject")
      )
      .andExpect(redirectedUrl("/plans/" + planId));
    entityManager.flush();
    entityManager.clear();
    assertThat(plans.findById(planId).orElseThrow().getJoinRequests().isEmpty(), is(true));
  }

  @Test
  public void emailInvitationTokenCannotRestoreBannedMembership() throws Exception {
    service.banParticipant(planId, MEMBER, OWNER);
    Plan plan = plans.findById(planId).orElseThrow();
    PlanInvitation invitation = new PlanInvitation(
      plan,
      users.findByEmail(MEMBER).orElseThrow(),
      plan.getAdministrator(),
      "editor"
    );
    plan.getInvitations().add(invitation);
    plans.save(plan);
    entityManager.flush();
    mvc
      .perform(
        post("/plans/{id}/invitations/{token}/accept", planId, invitation.getToken())
          .with(user(MEMBER))
          .with(csrf())
      )
      .andExpect(status().isForbidden());
    entityManager.flush();
    entityManager.clear();
    plan = plans.findById(planId).orElseThrow();
    assertThat(plan.isBanned(MEMBER), is(true));
    assertThat(plan.isParticipant(MEMBER), is(false));
    assertThat(plan.isEditor(MEMBER), is(false));
  }

  private User createUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password123");
    user.setRole("USER");
    users.save(user);
    return user;
  }
}
