package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebIntegrationTest
public class PlaceRestControllerTest {

  private static final int SEEDED_TOTAL = 12;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    mockMvc =
      MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  @Test
  public void shouldReturnPlacesAsJson() throws Exception {
    mockMvc
      .perform(get("/api/places"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(content().string(containsString("\"name\":\"MALBA\"")));
  }

  @Test
  public void shouldReturnTheWholeSeededDataset() throws Exception {
    // I-01: el dataset del seeder llega completo (12 lugares)
    List<Place> places = placesFrom(get("/api/places"));

    assertThat(places, hasSize(SEEDED_TOTAL));
  }

  @Test
  public void shouldFilterPlacesByCategoryAsJson() throws Exception {
    mockMvc
      .perform(get("/api/places").param("category", "RESTAURANT"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(content().string(containsString("\"category\":\"RESTAURANT\"")));
  }

  @Test
  public void shouldReturnSportPlaces() throws Exception {
    // I-02: SPORT tenia 0 lugares antes de 12-DATA, ahora al menos uno
    List<Place> places = placesFrom(get("/api/places").param("category", "SPORT"));

    assertThat(places.size(), is(greaterThanOrEqualTo(1)));
    assertThat(places, everyItem(categoryIs(PlaceCategory.SPORT)));
  }

  @Test
  public void shouldReturnOtherPlaces() throws Exception {
    // I-03: OTHER tenia 0 lugares antes de 12-DATA, ahora al menos uno
    List<Place> places = placesFrom(get("/api/places").param("category", "OTHER"));

    assertThat(places.size(), is(greaterThanOrEqualTo(1)));
    assertThat(places, everyItem(categoryIs(PlaceCategory.OTHER)));
  }

  @Test
  public void shouldRejectUnknownCategory() throws Exception {
    mockMvc
      .perform(get("/api/places").param("category", "UNKNOWN"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(content().string("[]"));
  }

  @Test
  public void shouldSearchPlacesByNameAsJson() throws Exception {
    // I-04: la busqueda por nombre sigue exacta sobre el dataset nuevo
    List<Place> places = placesFrom(get("/api/places").param("search", "Don"));

    assertThat(places, hasSize(1));
    assertThat(places.getFirst().getName(), is(equalTo("Parrilla Don Julio")));
  }

  @Test
  public void shouldApplySearchWithinSelectedCategoryAsJson() throws Exception {
    mockMvc
      .perform(get("/api/places").param("category", "RESTAURANT").param("search", "Don"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(content().string(containsString("\"name\":\"Parrilla Don Julio\"")))
      .andExpect(content().string(Matchers.not(containsString("El Sanjuanino"))));
  }

  @Test
  public void shouldReturnNullImageUrlOnEverySeededPlace() throws Exception {
    // I-05: ningun lugar del seeder trae imagen, el placeholder es el default (AC-02)
    List<Place> places = placesFrom(get("/api/places"));

    assertThat(places, hasSize(SEEDED_TOTAL));
    for (Place place : places) {
      assertThat(place.getImageUrl(), is(nullValue()));
    }
  }

  // --- helpers ---

  /** GET a la API y parsea el array de lugares con Jackson (sin json-path en el classpath). */
  private List<Place> placesFrom(MockHttpServletRequestBuilder request) throws Exception {
    String body = mockMvc
      .perform(request)
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andReturn()
      .getResponse()
      .getContentAsString();
    return Arrays.asList(new ObjectMapper().readValue(body, Place[].class));
  }

  private static Matcher<Place> categoryIs(PlaceCategory category) {
    return Matchers.hasProperty("category", is(equalTo(category)));
  }
}
