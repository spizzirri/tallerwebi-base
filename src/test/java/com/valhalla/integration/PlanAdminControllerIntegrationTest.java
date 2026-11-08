package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceService;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * [PABM] Integracion del ABM admin de planes (I-01 a I-11 de 25-PABM.md, mas el reorden y la
 * invitacion que fallaron en el navegador). Corre contra la base real con transaccion por test.
 */
@WebIntegrationTest
@Transactional
public class PlanAdminControllerIntegrationTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";
  private static final String APP_EMAIL = "app@planit.com";
  private static final String VISIBLE_NAME = "Plan visible PABM";
  private static final String HIDDEN_NAME = "Plan oculto PABM";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlanService planService;

  @Autowired
  private PlanPlaceService planPlaceService;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlaceRepository placeRepository;

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

  // --- I-01 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void listShowsEveryPlanIncludingThePlanItOnes() throws Exception {
    Plan userPlan = givenUserPlan("Plan de usuario PABM");
    Plan planIt = givenPlanItPlan("Plan de PlanIt PABM");

    this.mockMvc.perform(get("/admin/plans"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/plans"))
      .andExpect(content().string(containsString(userPlan.getName())))
      .andExpect(content().string(containsString(planIt.getName())));
  }

  // --- I-02 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void newFormShowsTheThreeStepWizardInCreateMode() throws Exception {
    this.mockMvc.perform(get("/admin/plans/new"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/plan-form"))
      .andExpect(model().attribute("isEdit", false))
      .andExpect(content().string(containsString("app@planit.com")));
  }

  // --- I-03 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void createAlwaysAssignsThePlanItUserAndTheDefaultDayEnd() throws Exception {
    MvcResult result =
      this.mockMvc.perform(
          post("/admin/plans")
            .with(csrf())
            .param("name", "Promo de la app")
            .param("eventDate", "2026-12-01")
            .param("eventTime", "10:00")
            .param("transportMode", "WALK")
            .param("isPublic", "true")
            .param("active", "true")
            .param("settings.handleRequests", "OWNER")
            .param("settings.inviteInternal", "OWNER")
            .param("settings.inviteByEmail", "OWNER")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/admin/plans/*"))
        .andReturn();

    Plan created = planRepository.findById(idFromRedirect(result)).orElseThrow();
    // AC-03: a nombre de PlanIt, nunca del admin logueado. AC-21: el dia termina en el default.
    assertThat(created.getAdministrator().getEmail(), is(equalTo(APP_EMAIL)));
    assertThat(created.getDayEndTime(), is(equalTo(LocalTime.of(23, 0))));
    assertThat(created.getActive(), is(true));
  }

  // --- I-04 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void createWithABlankNameReturnsTheFormWithErrorsAndSavesNothing() throws Exception {
    int before = planRepository.findAll().size();

    this.mockMvc.perform(
        post("/admin/plans")
          .with(csrf())
          .param("name", "")
          .param("eventDate", "2026-12-01")
          .param("transportMode", "WALK")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/plan-form"))
      .andExpect(model().attributeHasFieldErrors("planForm", "name"));

    assertThat(planRepository.findAll().size(), is(before));
  }

  // --- I-05 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void editPreloadsTheStoredPlanInEditMode() throws Exception {
    Plan plan = givenUserPlan("Plan a editar PABM");

    this.mockMvc.perform(get("/admin/plans/{id}/edit", plan.getId()))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/plan-form"))
      .andExpect(model().attribute("isEdit", true))
      .andExpect(content().string(containsString("Plan a editar PABM")));
  }

  // --- I-06 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void updateChangesTheFieldsAndKeepsTheStoredDayEnd() throws Exception {
    Plan plan = givenUserPlan("Antes");
    givenStoredDayEnd(plan.getId(), LocalTime.of(22, 0));

    this.mockMvc.perform(
        post("/admin/plans/{id}", plan.getId())
          .with(csrf())
          .param("name", "Despues")
          .param("eventDate", "2026-12-05")
          .param("transportMode", "BUS")
          .param("isPublic", "true")
          .param("active", "true")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/plans/" + plan.getId() + "/edit"));

    Plan stored = planRepository.findById(plan.getId()).orElseThrow();
    assertThat(stored.getName(), is(equalTo("Despues")));
    assertThat(stored.getEventDate(), is(equalTo(LocalDate.of(2026, 12, 5))));
    // AC-21: el update del admin no pisa la ventana del dia guardada.
    assertThat(stored.getDayEndTime(), is(equalTo(LocalTime.of(22, 0))));
  }

  // --- I-07 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void toggleActiveDeactivatesAndReactivatesThePlan() throws Exception {
    Plan plan = givenUserPlan("Para alternar");

    this.mockMvc.perform(
        post("/admin/plans/{id}/toggle-active", plan.getId()).with(csrf()).param("active", "false")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/plans"));
    assertThat(planRepository.findById(plan.getId()).orElseThrow().getActive(), is(false));

    this.mockMvc.perform(
        post("/admin/plans/{id}/toggle-active", plan.getId()).with(csrf()).param("active", "true")
      )
      .andExpect(status().is3xxRedirection());
    assertThat(planRepository.findById(plan.getId()).orElseThrow().getActive(), is(true));
  }

  // --- I-08 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void deleteRemovesThePlanAndRedirectsToTheList() throws Exception {
    Plan plan = givenUserPlan("Para borrar");

    this.mockMvc.perform(post("/admin/plans/{id}/delete", plan.getId()).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/plans"));

    assertThat(planRepository.findById(plan.getId()), is(equalTo(Optional.empty())));
  }

  // --- I-09 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void detailIsReadOnlyAndShowsTheWholePlan() throws Exception {
    Plan plan = givenUserPlan("Plan con detalle PABM");

    this.mockMvc.perform(get("/admin/plans/{id}", plan.getId()))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/plan-detail"))
      .andExpect(model().attribute("plan", notNullValue()))
      .andExpect(content().string(containsString("Plan con detalle PABM")));
  }

  // --- I-10 / I-11: un plan inactivo no aparece en los listados publicos ---

  @Test
  public void inactivePlanIsHiddenFromForYouWhileTheActiveOneIsShown() throws Exception {
    givenPublicPlan(VISIBLE_NAME, true);
    givenPublicPlan(HIDDEN_NAME, false);

    this.mockMvc.perform(get("/for-you"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(VISIBLE_NAME)))
      .andExpect(content().string(not(containsString(HIDDEN_NAME))));
  }

  @Test
  public void inactivePlanIsHiddenFromThePublicFeedUsedByExplore() throws Exception {
    givenPublicPlan(VISIBLE_NAME, true);
    givenPublicPlan(HIDDEN_NAME, false);

    this.mockMvc.perform(get("/api/plans/public"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(VISIBLE_NAME)))
      .andExpect(content().string(not(containsString(HIDDEN_NAME))));
  }

  // --- Reorden: regresion del HTTP 500 al mover una parada ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void movingAStopDownReordersAndRedirectsToTheEdit() throws Exception {
    Plan plan = givenPlanItPlan("Con paradas PABM");
    PlanPlace first = planPlaceService.addPlaceToPlan(plan.getId(), anyPlaceId(0), APP_EMAIL);
    PlanPlace second = planPlaceService.addPlaceToPlan(plan.getId(), anyPlaceId(1), APP_EMAIL);

    this.mockMvc.perform(
        post("/admin/plans/{id}/places/{placeId}/move-down", plan.getId(), first.getId())
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/plans/" + plan.getId() + "/edit"));

    List<UUID> order = planService
      .getAnyPlan(plan.getId())
      .getPlanPlaces()
      .stream()
      .map(PlanPlace::getId)
      .toList();
    assertThat(order, is(equalTo(List.of(second.getId(), first.getId()))));
  }

  // --- Invitacion desde el edit (AC-20) ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void inviteFromTheEditCreatesAPendingInvitationForTheRecipient() throws Exception {
    Plan plan = givenPlanItPlan("Con invitados PABM");
    String recipient = "invitado.pabm@test.com";
    givenUser(recipient);

    this.mockMvc.perform(
        post("/admin/plans/{id}/participants/invite", plan.getId())
          .with(csrf())
          .param("participantEmail", recipient)
          .param("role", "editor")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/plans/" + plan.getId() + "/edit"));

    Plan stored = planService.getAnyPlan(plan.getId());
    assertThat(stored.getInvitations(), hasSize(1));
    assertThat(stored.getInvitations().get(0).getRecipient().getEmail(), is(equalTo(recipient)));
    assertThat(stored.getInvitations().get(0).getRole(), is(equalTo("editor")));
  }

  // --- helpers ---

  private Plan givenUserPlan(String name) {
    return givenPlan(name, givenUser("dueno.pabm." + UUID.randomUUID() + "@test.com"), false);
  }

  private Plan givenPlanItPlan(String name) {
    Plan draft = new Plan();
    draft.setName(name);
    draft.setEventDate(LocalDate.of(2026, 12, 1));
    draft.setEventTime(LocalTime.of(10, 0));
    draft.setIsPublic(false);
    return planService.createAdminPlan(draft);
  }

  private void givenPublicPlan(String name, boolean active) {
    Plan plan = givenPlan(name, givenUser("publico." + UUID.randomUUID() + "@test.com"), true);
    plan.setActive(active);
    planRepository.save(plan);
  }

  private Plan givenPlan(String name, User owner, boolean isPublic) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setEventDate(LocalDate.of(2026, 12, 1));
    plan.setEventTime(LocalTime.of(10, 0));
    plan.setIsPublic(isPublic);
    plan.setAdministrator(owner);
    plan.setShortCode(shortCodeFor(name));
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

  private void givenStoredDayEnd(UUID planId, LocalTime dayEnd) {
    Plan plan = planRepository.findById(planId).orElseThrow();
    plan.setDayEndTime(dayEnd);
    planRepository.save(plan);
  }

  private UUID anyPlaceId(int index) {
    List<Place> places = placeRepository.findAll();
    assertThat(places.size() > index, is(true));
    return places.get(index).getId();
  }

  /** Codigo de 8 caracteres unico: el shortCode tiene restriccion de unicidad en la base. */
  private static String shortCodeFor(String name) {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
  }

  private static UUID idFromRedirect(MvcResult result) {
    String url = result.getResponse().getRedirectedUrl();
    return UUID.fromString(url.substring(url.lastIndexOf('/') + 1));
  }
}
