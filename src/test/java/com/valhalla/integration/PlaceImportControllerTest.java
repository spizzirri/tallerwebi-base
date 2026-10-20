package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.OverpassClient;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.presentation.shared.LoginRedirects;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * I-15..I-19 + el leftover de S-04. {@link OverpassClient} se reemplaza por un mock: ninguna
 * suite golpea Overpass ni Nominatim (AC-10).
 */
@WebIntegrationTest
@Transactional
public class PlaceImportControllerTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";
  private static final String USER_EMAIL = "user@unlam.edu.ar";
  private static final String ERROR_FORBIDDEN_QUERY = "Unsupported query: use amenity or shop";

  private static final OsmPoi FIXTURE_POI = new OsmPoi(
    "Parrilla del Puerto",
    "Av. Corrientes 1234",
    -34.6037,
    -58.3816,
    "Mo-Su 12:00-23:00",
    "+54 11 4444-5555",
    "https://parrilla.example.com",
    "amenity",
    "restaurant"
  );

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlaceRepository placeRepository;

  @MockitoBean
  private OverpassClient overpassClient;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
  }

  // --- I-15 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldRenderTheImportMapView() throws Exception {
    this.mockMvc.perform(get("/admin/places/import"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-import"))
      // La attribution ODbL es obligatoria en cualquier mapa que muestre datos OSM.
      .andExpect(content().string(containsString("OpenStreetMap")))
      .andExpect(content().string(containsString("/admin/places/import/pois")));

    verifyNoInteractions(this.overpassClient);
  }

  // --- I-16 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldProxyThePoisAsJsonWithoutTouchingTheNetwork() throws Exception {
    given(this.overpassClient.findCabaPois("amenity", "restaurant"))
      .willReturn(List.of(FIXTURE_POI));

    List<OsmPoi> pois = poisFrom(get("/admin/places/import/pois?amenity=restaurant"));

    assertThat(pois, hasSize(1));
    // equals del record: nombre, direccion, coordenadas, horarios, telefono, web y tags OSM.
    assertThat(pois.getFirst(), is(FIXTURE_POI));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldAlsoAcceptTheShopKey() throws Exception {
    given(this.overpassClient.findCabaPois("shop", "clothes")).willReturn(List.of());

    assertThat(poisFrom(get("/admin/places/import/pois?shop=clothes")), hasSize(0));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldAnswerBadGatewayWhenOverpassFails() throws Exception {
    given(this.overpassClient.findCabaPois(anyString(), anyString()))
      .willThrow(new IllegalStateException("Overpass answered HTTP 504"));

    String body =
      this.mockMvc.perform(get("/admin/places/import/pois?amenity=restaurant"))
        .andExpect(status().isBadGateway())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    assertThat(jsonOf(body).path("error").asText(), is("Overpass is not available right now"));
  }

  // --- I-17 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldPrefillTheNewPlaceFormFromTheQueryParams() throws Exception {
    this.mockMvc.perform(
        get("/admin/places/new")
          .param("name", "Parrilla del Puerto")
          .param("address", "Av. Corrientes 1234")
          .param("latitude", "-34.6037")
          .param("longitude", "-58.3816")
          .param("openingHours", "Mo-Su 12:00-23:00")
          .param("phone", "+54 11 4444-5555")
          .param("website", "https://parrilla.example.com")
          .param("category", "RESTAURANT")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(model().attribute("isEdit", false))
      .andExpect(
        model()
          .attribute(
            "placeForm",
            allOf(
              hasProperty("name", is("Parrilla del Puerto")),
              hasProperty("address", is("Av. Corrientes 1234")),
              hasProperty("latitude", is(-34.6037)),
              hasProperty("longitude", is(-58.3816)),
              hasProperty("openingHours", is("Mo-Su 12:00-23:00")),
              hasProperty("phone", is("+54 11 4444-5555")),
              hasProperty("website", is("https://parrilla.example.com")),
              hasProperty("category", is(PlaceCategory.RESTAURANT))
            )
          )
      )
      .andExpect(content().string(containsString("Parrilla del Puerto")));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldStartThePrefilledFormInOtherWhenTheOsmCategoryHasNoMapping() throws Exception {
    this.mockMvc.perform(
        get("/admin/places/new").param("name", "Vending").param("category", "vending_machine")
      )
      .andExpect(status().isOk())
      .andExpect(model().attribute("placeForm", hasProperty("category", is(PlaceCategory.OTHER))));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldKeepTheEmptyFormWhenThereAreNoQueryParams() throws Exception {
    this.mockMvc.perform(get("/admin/places/new"))
      .andExpect(status().isOk())
      .andExpect(view().name("pages/admin/place-form"))
      .andExpect(
        model()
          .attribute(
            "placeForm",
            allOf(
              hasProperty("name", nullValue()),
              hasProperty("category", nullValue()),
              hasProperty("latitude", nullValue())
            )
          )
      );
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldIgnoreACoordinateThatIsNotANumber() throws Exception {
    this.mockMvc.perform(
        get("/admin/places/new").param("name", "Sin Coordenada").param("latitude", "sur")
      )
      .andExpect(status().isOk())
      .andExpect(model().attribute("placeForm", hasProperty("latitude", nullValue())));
  }

  // --- I-18 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldCreateThePlaceWithThePrefilledValues() throws Exception {
    this.mockMvc.perform(
        post("/admin/places")
          .with(csrf())
          .param("name", "Parrilla del Puerto")
          .param("category", "RESTAURANT")
          .param("address", "Av. Corrientes 1234")
          .param("latitude", "-34.6037")
          .param("longitude", "-58.3816")
          .param("openingHours", "Mo-Su 12:00-23:00")
          .param("phone", "+54 11 4444-5555")
          .param("website", "https://parrilla.example.com")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/admin/places"));

    Place stored = placeRepository
      .findByNameContainingIgnoreCase("Parrilla del Puerto")
      .stream()
      .filter(place -> place.getName().equals("Parrilla del Puerto"))
      .findFirst()
      .orElseThrow();
    assertThat(stored.getCategory(), is(PlaceCategory.RESTAURANT));
    assertThat(stored.getAddress(), is("Av. Corrientes 1234"));
    assertThat(stored.getLatitude(), is(-34.6037));
    assertThat(stored.getLongitude(), is(-58.3816));
    assertThat(stored.getOpeningHours(), is("Mo-Su 12:00-23:00"));
    assertThat(stored.getPhone(), is("+54 11 4444-5555"));
    assertThat(stored.getWebsite(), is("https://parrilla.example.com"));
  }

  // --- I-19 ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldRejectAQueryOutsideTheAllowlistWithoutCallingTheClient() throws Exception {
    String body =
      this.mockMvc.perform(get("/admin/places/import/pois?color=red"))
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    assertThat(jsonOf(body).path("error").asText(), is(ERROR_FORBIDDEN_QUERY));

    this.mockMvc.perform(get("/admin/places/import/pois?amenity=restaurant&evil=x"))
      .andExpect(status().isBadRequest());

    this.mockMvc.perform(get("/admin/places/import/pois")).andExpect(status().isBadRequest());

    this.mockMvc.perform(get("/admin/places/import/pois?amenity="))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(this.overpassClient);
  }

  // --- S-04 (leftover): el proxy nuevo tampoco esta al alcance de un USER ---

  @Test
  @WithMockUser(username = USER_EMAIL, roles = { "USER" })
  public void shouldNotLetACommonUserReachThePoiProxy() throws Exception {
    this.mockMvc.perform(get("/admin/places/import/pois?amenity=restaurant"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    this.mockMvc.perform(get("/admin/places/import"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl(LoginRedirects.USER_LANDING));

    verifyNoInteractions(this.overpassClient);
  }

  /**
   * GET al proxy y parseo del array con Jackson: el classpath de test no trae json-path, asi que
   * las aserciones van sobre el objeto (mismo seam que PlaceAdminControllerTest usa para la API).
   */
  private List<OsmPoi> poisFrom(MockHttpServletRequestBuilder request) throws Exception {
    String body =
      this.mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    return Arrays.asList(new ObjectMapper().readValue(body, OsmPoi[].class));
  }

  private JsonNode jsonOf(String body) throws Exception {
    return new ObjectMapper().readTree(body);
  }
}
