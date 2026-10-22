package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.valhalla.domain.bookmark.PlanBookmarkRepository;
import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.plan.PlanService;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import jakarta.servlet.Filter;
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
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * [MARK] Security coverage of "Guardados" (docs/specs/20-MARK.md, S-01..S-06): sin sesion no hay
 * guardado, sin CSRF no hay POST, un guardado ajeno no se clona y la vista publica no filtra nada
 * de la lista privada de nadie.
 */
@WebIntegrationTest
@Transactional
public class BookmarkSecurityTest {

  private static final String OWNER_EMAIL = "mark.sec.owner@test.com";
  private static final String SAVER_EMAIL = "mark.sec.saver@test.com";
  private static final String INTRUDER_EMAIL = "mark.sec.intruder@test.com";
  private static final String PASSWORD = "la-password-de-siempre";
  private static final String PLAN_NAME = "Plan publico de Grace";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlaceRepository placeRepository;

  @Autowired
  private PlanBookmarkRepository planBookmarkRepository;

  @Autowired
  private PlanBookmarkService planBookmarkService;

  @Autowired
  private PlanService planService;

  private MockMvc mockMvc;

  private UUID ownerId;
  private UUID saverId;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter((Filter) new HiddenHttpMethodFilter())
        .build();

    User owner = user("Grace", "Hopper", OWNER_EMAIL);
    this.userRepository.save(owner);
    this.ownerId = owner.getId();

    User saver = user("Ada", "Lovelace", SAVER_EMAIL);
    this.userRepository.save(saver);
    this.saverId = saver.getId();

    User intruder = user("Eva", "Intrusa", INTRUDER_EMAIL);
    this.userRepository.save(intruder);
  }

  // --- S-01 / S-02: sin sesion, el POST cae en el login y no escribe ---

  @Test
  public void S01_anonymousSaveRedirectsToLoginAndCreatesNothing() throws Exception {
    UUID planId = givenPublicPlan();

    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));

    assertThat(this.planBookmarkRepository.findPlanIdsByUserId(this.saverId), hasSize(0));
  }

  @Test
  public void S02_anonymousRemoveRedirectsToLoginAndDeletesNothing() throws Exception {
    UUID planId = givenPublicPlan();
    this.planBookmarkService.save(planId, SAVER_EMAIL);

    this.mockMvc.perform(post("/bookmarks/" + planId + "/remove").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrlPattern("**/auth/login*"));

    assertThat(this.planBookmarkRepository.findPlanIdsByUserId(this.saverId), hasSize(1));
  }

  // --- S-03 / S-04: sin token CSRF no hay POST ---

  @Test
  @WithMockUser(username = SAVER_EMAIL)
  public void S03_mvcPostWithoutCsrfTokenIsForbidden() throws Exception {
    UUID planId = givenPublicPlan();

    this.mockMvc.perform(post("/bookmarks/" + planId)).andExpect(status().isForbidden());

    assertThat(this.planBookmarkRepository.findPlanIdsByUserId(this.saverId), hasSize(0));
  }

  @Test
  @WithMockUser(username = SAVER_EMAIL)
  public void S04_restToggleWithoutCsrfTokenIsForbidden() throws Exception {
    UUID planId = givenPublicPlan();

    this.mockMvc.perform(post("/api/plans/" + planId + "/bookmarks"))
      .andExpect(status().isForbidden());
    this.mockMvc.perform(delete("/api/plans/" + planId + "/bookmarks"))
      .andExpect(status().isForbidden());

    assertThat(this.planBookmarkRepository.findPlanIdsByUserId(this.saverId), hasSize(0));
  }

  // --- S-05: el guardado es privado, no se clona desde otra cuenta ---

  @Test
  @WithMockUser(username = INTRUDER_EMAIL)
  public void S05_anotherUserCannotCloneSomeoneElsesBookmark() throws Exception {
    UUID planId = givenPublicPlan();
    this.planBookmarkService.save(planId, SAVER_EMAIL);

    this.mockMvc.perform(post("/bookmarks/" + planId + "/clone").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans?error=Plan+not+found"));

    assertThat(this.planService.getPlansByUserEmail(INTRUDER_EMAIL), hasSize(0));
  }

  // --- S-06: la vista publica no filtra datos de los guardados ---

  @Test
  public void S06_thePublicPlanPageLeaksNothingAboutAnyonesBookmarks() throws Exception {
    UUID planId = givenPublicPlan();
    this.planBookmarkService.save(planId, SAVER_EMAIL);

    this.mockMvc.perform(get("/plans/" + planId + "/public"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(PLAN_NAME)))
      .andExpect(content().string(not(containsString("data-bookmark-toggle"))))
      .andExpect(content().string(not(containsString("Quitar de Guardados"))))
      .andExpect(content().string(not(containsString("menu-bookmarks"))));
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

  private UUID givenPublicPlan() {
    Plan plan = new Plan();
    plan.setName(PLAN_NAME);
    plan.setDescription("Descripcion de " + PLAN_NAME);
    plan.setEventDate(LocalDate.now().plusDays(10));
    plan.setIsPublic(true);
    plan.setAdministrator(this.userRepository.findById(this.ownerId).orElseThrow());
    UUID id = this.planService.createPlan(plan, OWNER_EMAIL).getId();

    Plan saved = this.planRepository.findById(id).orElseThrow();
    Place place = new Place();
    place.setName(PLAN_NAME + " lugar 1");
    place.setCategory(PlaceCategory.MUSEUM);
    this.placeRepository.save(place);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setSortOrder(1);
    saved.addPlanPlace(entry);
    this.planRepository.save(saved);
    return id;
  }
}
