package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.valhalla.domain.bookmark.PlanBookmark;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * [MARK] Integration coverage of "Guardados" (docs/specs/20-MARK.md, I-01..I-13): the MVC and REST
 * endpoints against a real database, the snapshot that survives the plan, the three card states and
 * the surfaces that show the toggle.
 *
 * <p>Los usuarios viven en la DB de test: {@code @WithMockUser} solo trae el email, el controller lo
 * cruza por email. Cada test corre en su transaccion, asi que los guards no se pisan.
 */
@WebIntegrationTest
@Transactional
public class BookmarkControllerIntegrationTest {

  private static final String SELF_EMAIL = "mark.self@test.com";
  private static final String OTHER_EMAIL = "mark.owner@test.com";
  private static final String PASSWORD = "la-password-de-siempre";
  private static final String PLAN_NAME = "Plan publico de Grace";
  private static final String PRIVATE_PLAN_NAME = "Plan privado de Grace";

  /** El {@code GlobalExceptionHandler} manda {@code PlanNotFoundException} al listado. */
  private static final String NOT_FOUND_REDIRECT = "/plans?error=Plan+not+found";

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

  private UUID selfId;
  private UUID otherId;

  @BeforeEach
  public void setUp() {
    this.mockMvc =
      MockMvcBuilders
        .webAppContextSetup(this.wac)
        .apply(springSecurity())
        .addFilter((Filter) new HiddenHttpMethodFilter())
        .build();

    User self = user("Ada", "Lovelace", SELF_EMAIL);
    this.userRepository.save(self);
    this.selfId = self.getId();

    User other = user("Grace", "Hopper", OTHER_EMAIL);
    this.userRepository.save(other);
    this.otherId = other.getId();
  }

  // --- I-01: guardar desde el form MVC ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I01_savingAPublicPlanCreatesTheBookmarkAndRedirectsToThePlan() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 2);

    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId + "/public"));

    assertThat(
      this.planBookmarkRepository.findByUserIdAndPlanId(this.selfId, planId).isPresent(),
      is(true)
    );
  }

  // --- I-02: el guardado persiste el snapshot ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I02_theBookmarkPersistsTheHeaderAndItinerarySnapshot() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 2);

    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()));

    PlanBookmark bookmark =
      this.planBookmarkRepository.findByUserIdAndPlanId(this.selfId, planId).orElseThrow();
    assertThat(bookmark.getHeader().getName(), is(PLAN_NAME));
    assertThat(bookmark.getHeader().getDescription(), is("Descripcion de " + PLAN_NAME));
    assertThat(bookmark.getHeader().getPlaceCount(), is(2));
    assertThat(bookmark.getHeader().getAdministratorName(), is("Grace Hopper"));
    assertThat(bookmark.getPlaces(), hasSize(2));
    assertThat(bookmark.getPlaces().get(0).getPlaceName(), is(PLAN_NAME + " lugar 1"));
    assertThat(bookmark.getPlaces().get(0).getSortOrder(), is(1));
    assertThat(bookmark.getPlaces().get(1).getSortOrder(), is(2));
  }

  // --- I-03: guardar dos veces no duplica ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I03_savingTwiceLeavesASingleRow() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);

    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()));
    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()));

    assertThat(this.planBookmarkRepository.findByUserId(this.selfId), hasSize(1));
  }

  // --- I-04: un plan privado responde como un id inexistente ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I04_savingAPrivatePlanAnswersLikeAMissingIdAndCreatesNothing() throws Exception {
    UUID planId = givenPrivatePlan(PRIVATE_PLAN_NAME);

    this.mockMvc.perform(post("/bookmarks/" + planId).with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(NOT_FOUND_REDIRECT));

    assertThat(this.planBookmarkRepository.findByUserId(this.selfId), hasSize(0));
  }

  // --- I-05: quitar borra el guardado ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I05_removingTheBookmarkDeletesTheRow() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    this.mockMvc.perform(post("/bookmarks/" + planId + "/remove").with(csrf()))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/plans/" + planId + "/public"));

    assertThat(this.planBookmarkRepository.findByUserId(this.selfId), hasSize(0));
  }

  // --- I-06: la seccion Guardados del perfil propio ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I06_ownProfileShowsTheCardAndTheEmptyState() throws Exception {
    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/edit"))
      .andExpect(content().string(containsString("Todavia no guardaste planes")))
      .andExpect(content().string(not(containsString("data-bookmark-badge"))));

    UUID planId = givenPublicPlan(PLAN_NAME, 1);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(PLAN_NAME)))
      .andExpect(content().string(containsString("Clonar plan")))
      .andExpect(content().string(containsString("Quitar")))
      .andExpect(content().string(containsString("action=\"/bookmarks/" + planId + "/remove\"")))
      .andExpect(content().string(not(containsString("Todavia no guardaste planes"))))
      .andExpect(content().string(not(containsString("data-bookmark-badge"))));
  }

  // --- I-07: el plan borrado deja la card con badge y tooltip (snapshot) ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I07_aDeletedPlanKeepsTheCardWithTheSnapshotNameAndTheEliminadoBadge()
    throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    Plan live = this.planRepository.findById(planId).orElseThrow();
    live.setName("Nombre cambiado por el autor");
    this.planRepository.save(live);
    this.planService.deleteOwnedPlan(planId, OTHER_EMAIL);

    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString(PLAN_NAME)))
      .andExpect(content().string(not(containsString("Nombre cambiado por el autor"))))
      .andExpect(content().string(containsString("data-bookmark-badge")))
      .andExpect(content().string(containsString("Eliminado")))
      .andExpect(content().string(containsString("Podes clonarlo para seguir usandolo")));
  }

  // --- I-08: el plan que paso a privado deja la card con badge "Privado" ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I08_aPlanThatBecamePrivateIsMarkedPrivado() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    Plan live = this.planRepository.findById(planId).orElseThrow();
    live.setIsPublic(false);
    this.planRepository.save(live);

    this.mockMvc.perform(get("/profile/" + this.selfId))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("data-bookmark-badge")))
      .andExpect(content().string(containsString("Privado")))
      .andExpect(content().string(containsString("Podes clonarlo para seguir usandolo")));
  }

  // --- I-09: clonar un plan borrado reconstruye la copia desde el snapshot ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I09_cloningADeletedPlanRebuildsItFromTheSnapshotSkippingMissingPlaces()
    throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 2);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    Plan live = this.planRepository.findById(planId).orElseThrow();
    UUID gonePlaceId = live.getPlanPlaces().get(1).getPlace().getId();
    this.planService.deleteOwnedPlan(planId, OTHER_EMAIL);
    this.placeRepository.deleteById(gonePlaceId);

    MvcResult result =
      this.mockMvc.perform(post("/bookmarks/" + planId + "/clone").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andReturn();

    String location = result.getResponse().getRedirectedUrl();
    assertThat(location, startsWith("/plans/"));
    UUID copyId = UUID.fromString(location.substring("/plans/".length()));

    Plan copy = this.planRepository.findById(copyId).orElseThrow();
    assertThat(copy.getName(), is(PLAN_NAME));
    assertThat(copy.getDescription(), is("Descripcion de " + PLAN_NAME));
    assertThat(copy.getIsPublic(), is(false));
    assertThat(copy.isAdministrator(SELF_EMAIL), is(true));
    assertThat(copy.getPlanPlaces(), hasSize(1));
    assertThat(copy.getPlanPlaces().get(0).getPlace().getName(), is(PLAN_NAME + " lugar 1"));
  }

  // --- I-10: los guardados no se ven en el perfil de otro ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I10_someoneElsesProfileNeverRendersTheGuardadosSection() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);
    this.planBookmarkService.save(planId, SELF_EMAIL);

    this.mockMvc.perform(get("/profile/" + this.otherId))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/profile/view"))
      .andExpect(content().string(not(containsString("id=\"bookmarks\""))))
      .andExpect(content().string(not(containsString("Clonar plan"))));
  }

  // --- I-11: el modal del navbar linkea al ancla ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I11_theNavbarMenuRendersGuardadosAsALinkToTheProfileAnchor() throws Exception {
    this.mockMvc.perform(get("/for-you"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("id=\"menu-bookmarks\"")))
      .andExpect(content().string(containsString("href=\"/profile#bookmarks\"")));
  }

  // --- I-12: el icono se pinta relleno o de contorno segun el estado ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I12_theFeedAndThePublicViewPaintTheToggleFromTheBookmarkState() throws Exception {
    UUID savedPlanId = givenPublicPlan(PLAN_NAME, 1);
    givenPublicPlan("Otro plan publico", 1);
    this.planBookmarkService.save(savedPlanId, SELF_EMAIL);

    String feed = html("/for-you", "pages/plans/public-list");
    assertThat(countOf(feed, "data-bookmarked=\"true\""), is(1));
    assertThat(countOf(feed, "data-bookmarked=\"false\""), is(1));
    assertThat(feed, containsString("aria-pressed=\"true\""));

    this.mockMvc.perform(get("/plans/" + savedPlanId + "/public"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("data-bookmark-toggle=\"true\"")))
      .andExpect(content().string(containsString("data-bookmarked=\"true\"")))
      .andExpect(content().string(containsString("aria-label=\"Quitar de Guardados\"")));
  }

  // --- I-13: el toggle REST del sidebar ---

  @Test
  @WithMockUser(username = SELF_EMAIL)
  public void I13_theRestToggleAnswers204AndThePublicFeedReflectsTheState() throws Exception {
    UUID planId = givenPublicPlan(PLAN_NAME, 1);

    this.mockMvc.perform(post("/api/plans/" + planId + "/bookmarks").with(csrf()))
      .andExpect(status().isNoContent());
    assertThat(publicFeed(), containsString("\"bookmarked\":true"));

    this.mockMvc.perform(delete("/api/plans/" + planId + "/bookmarks").with(csrf()))
      .andExpect(status().isNoContent());
    assertThat(publicFeed(), containsString("\"bookmarked\":false"));

    // REST necesita un 404 real (el handler HTML redirige): privado e inexistente responden igual.
    UUID privatePlanId = givenPrivatePlan(PRIVATE_PLAN_NAME);
    this.mockMvc.perform(post("/api/plans/" + privatePlanId + "/bookmarks").with(csrf()))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("Not found")));
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

  /**
   * Plan de Grace con {@code placeCount} lugares. Fecha futura: el feed solo descubre planes por
   * venir, y estos tests miran las cards del feed.
   */
  private UUID givenPublicPlan(String name, int placeCount) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setDescription("Descripcion de " + name);
    plan.setEventDate(LocalDate.now().plusDays(10));
    plan.setIsPublic(true);
    plan.setAdministrator(this.userRepository.findById(this.otherId).orElseThrow());
    UUID id = this.planService.createPlan(plan, OTHER_EMAIL).getId();
    addPlaces(id, name, placeCount);
    return id;
  }

  private UUID givenPrivatePlan(String name) {
    Plan plan = new Plan();
    plan.setName(name);
    plan.setIsPublic(false);
    plan.setAdministrator(this.userRepository.findById(this.otherId).orElseThrow());
    return this.planService.createPlan(plan, OTHER_EMAIL).getId();
  }

  private void addPlaces(UUID planId, String name, int placeCount) {
    Plan saved = this.planRepository.findById(planId).orElseThrow();
    for (int i = 1; i <= placeCount; i++) {
      Place place = new Place();
      place.setName(name + " lugar " + i);
      place.setCategory(PlaceCategory.MUSEUM);
      this.placeRepository.save(place);
      PlanPlace entry = new PlanPlace();
      entry.setPlace(place);
      entry.setSortOrder(i);
      saved.addPlanPlace(entry);
    }
    this.planRepository.save(saved);
  }

  private String html(String url, String viewName) throws Exception {
    return this.mockMvc.perform(get(url))
      .andExpect(status().isOk())
      .andExpect(view().name(viewName))
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String publicFeed() throws Exception {
    return this.mockMvc.perform(get("/api/plans/public"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private static int countOf(String haystack, String needle) {
    int count = 0;
    int from = 0;
    while ((from = haystack.indexOf(needle, from)) >= 0) {
      count++;
      from += needle.length();
    }
    return count;
  }
}
