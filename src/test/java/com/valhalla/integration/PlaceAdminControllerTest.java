package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.planplace.PlanPlace;
import com.valhalla.domain.planplace.PlanPlaceRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.presentation.place.EditPlaceRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@WebIntegrationTest
@Transactional
public class PlaceAdminControllerTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";
  private static final int SEEDED_TOTAL = 12;
  private static final String SEEDED_PLACE = "Parrilla Don Julio";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlaceRepository placeRepository;

  @Autowired
  private PlaceService placeService;

  @Autowired
  private PlanRepository planRepository;

  @Autowired
  private PlanPlaceRepository planPlaceRepository;

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

  // --- I-06 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldListTheSeededPlaces() throws Exception {
    this.mockMvc.perform(get("/admin/places"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/places"))
      .andExpect(model().attributeExists("places"))
      .andExpect(content().string(containsString(SEEDED_PLACE)));

    assertThat(placesFrom(get("/api/places")), hasSize(SEEDED_TOTAL));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldShowTheNewPlaceForm() throws Exception {
    this.mockMvc.perform(get("/admin/places/new"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attributeExists("placeForm"))
      .andExpect(model().attribute("isEdit", false));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldShowTheEditPlaceFormPrefilled() throws Exception {
    Place place = givenPlace("Para Editar", PlaceCategory.BAR, null);

    this.mockMvc.perform(get("/admin/places/" + place.getId() + "/edit"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attribute("isEdit", true))
      .andExpect(model().attribute("placeId", place.getId()))
      .andExpect(model().attribute("placeForm", hasProperty("name", is("Para Editar"))));
  }

  // --- I-07 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldCreateAPlaceAndShowItInTheListAndInTheApi() throws Exception {
    this.mockMvc.perform(
        post("/admin/places")
          .with(csrf())
          .param("name", "Cafe Nuevo")
          .param("category", "CAFE")
          .param("address", "Av. Siempreviva 742")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));

    this.mockMvc.perform(get("/admin/places"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Cafe Nuevo")));

    List<Place> places = placesFrom(get("/api/places"));
    assertThat(places, hasSize(SEEDED_TOTAL + 1));
    assertThat(namesOf(places), hasItem("Cafe Nuevo"));
  }

  // --- I-08 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldUpdateNameCategoryAndImageOfAPlace() throws Exception {
    Place place = givenPlace("Lugar Viejo", PlaceCategory.PARK, null);

    this.mockMvc.perform(
        post("/admin/places/" + place.getId())
          .with(csrf())
          .param("_method", "PUT")
          .param("name", "Lugar Renombrado")
          .param("category", "SHOPPING")
          .param("imageUrl", "https://example.com/foto.png")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));

    Place updated = placeRepository.findById(place.getId()).orElseThrow();
    assertThat(updated.getName(), is("Lugar Renombrado"));
    assertThat(updated.getCategory(), is(PlaceCategory.SHOPPING));
    assertThat(updated.getImageUrl(), is("https://example.com/foto.png"));

    assertThat(namesOf(placesFrom(get("/api/places"))), hasItem("Lugar Renombrado"));
  }

  // --- I-09 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldDeleteAPlaceAndDropItFromTheListAndTheApi() throws Exception {
    Place place = givenPlace("Lugar Borrable", PlaceCategory.CAFE, null);

    this.mockMvc.perform(
        post("/admin/places/" + place.getId() + "/delete").with(csrf()).param("_method", "DELETE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));

    assertThat(placeRepository.findById(place.getId()).isPresent(), is(false));

    List<Place> places = placesFrom(get("/api/places"));
    assertThat(places, hasSize(SEEDED_TOTAL));
    assertThat(namesOf(places), not(hasItem("Lugar Borrable")));
  }

  // --- I-10 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldReRenderTheFormWithoutPersistingWhenNameIsMissing() throws Exception {
    this.mockMvc.perform(post("/admin/places").with(csrf()).param("category", "CAFE"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attribute("error", "Invalid place data"))
      .andExpect(model().attributeExists("org.springframework.validation.BindingResult.placeForm"));

    assertThat(placeRepository.count(), is((long) SEEDED_TOTAL));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldReRenderTheFormWithoutPersistingWhenCategoryIsMissing() throws Exception {
    this.mockMvc.perform(post("/admin/places").with(csrf()).param("name", "Sin categoria"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attribute("error", "Invalid place data"));

    assertThat(placeRepository.count(), is((long) SEEDED_TOTAL));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldReRenderTheEditFormWithErrorWhenTheUpdateInputIsInvalid() throws Exception {
    Place place = givenPlace("Editable", PlaceCategory.PARK, null);

    this.mockMvc.perform(
        post("/admin/places/" + place.getId())
          .with(csrf())
          .param("_method", "PUT")
          .param("name", "   ")
          .param("category", "PARK")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attribute("isEdit", true))
      .andExpect(model().attribute("placeId", place.getId()))
      .andExpect(model().attribute("error", "Invalid place data"));

    assertThat(placeRepository.findById(place.getId()).orElseThrow().getName(), is("Editable"));
  }

  // --- I-11 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldRefuseToDeleteAPlaceThatBelongsToAPlan() throws Exception {
    Place place = placeRepository.findAll().getFirst();
    Plan plan = givenPlanWithPlace(place);

    this.mockMvc.perform(
        post("/admin/places/" + place.getId() + "/delete").with(csrf()).param("_method", "DELETE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"))
      .andExpect(flash().attribute("error", "The place is used by a plan and cannot be deleted"));

    assertThat(placeRepository.findById(place.getId()).isPresent(), is(true));
    assertThat(planPlaceRepository.findByPlanId(plan.getId()), hasSize(1));
  }

  // --- I-12 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldStoreANullImageUrlWhenTheFieldIsOmitted() throws Exception {
    this.mockMvc.perform(
        post("/admin/places").with(csrf()).param("name", "Sin Foto").param("category", "MUSEUM")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));

    Place stored = placeByName("Sin Foto");
    assertThat(stored.getImageUrl(), is(nullValue()));

    Place fromApi = placesFrom(get("/api/places"))
      .stream()
      .filter(place -> place.getName().equals("Sin Foto"))
      .findFirst()
      .orElseThrow();
    assertThat(fromApi.getImageUrl(), is(nullValue()));
  }

  // --- cobertura del camino "id inexistente" en GET /{id}/edit ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldRenderTheErrorPageWhenEditingAnUnknownPlace() throws Exception {
    this.mockMvc.perform(get("/admin/places/0192f0a1-0000-7000-8000-000000000999/edit"))
      .andExpect(status().isInternalServerError())
      .andExpect(view().name("pages/error"));
  }

  // --- helpers ---

  private Place givenPlace(String name, PlaceCategory category, String imageUrl) {
    EditPlaceRequest request = new EditPlaceRequest();
    request.setName(name);
    request.setCategory(category);
    request.setImageUrl(imageUrl);
    return placeService.create(request);
  }

  private Plan givenPlanWithPlace(Place place) {
    User owner = new User();
    owner.setEmail("place-owner@test.com");
    owner.setPassword("password123");
    owner.setRole("USER");
    userRepository.save(owner);
    Plan plan = new Plan();
    plan.setName("Itinerary that blocks the delete");
    plan.setAdministrator(owner);
    planRepository.save(plan);
    PlanPlace entry = new PlanPlace();
    entry.setPlace(place);
    entry.setSortOrder(1);
    plan.addPlanPlace(entry);
    planRepository.save(plan);
    return plan;
  }

  private Place placeByName(String name) {
    return placeRepository
      .findByNameContainingIgnoreCase(name)
      .stream()
      .filter(place -> place.getName().equals(name))
      .findFirst()
      .orElseThrow();
  }

  private List<String> namesOf(List<Place> places) {
    return places.stream().map(Place::getName).toList();
  }

  /** GET a la API y parsea el array de lugares con Jackson (mismo seam que PlaceRestControllerTest). */
  private List<Place> placesFrom(MockHttpServletRequestBuilder request) throws Exception {
    String body =
      this.mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    return Arrays.asList(new ObjectMapper().readValue(body, Place[].class));
  }
}
