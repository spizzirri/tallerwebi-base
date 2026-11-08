package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.MapPoi;
import com.valhalla.domain.place.MapPoiRepository;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.OverpassClient;
import com.valhalla.domain.place.Place;
import com.valhalla.domain.place.PlaceCategory;
import com.valhalla.domain.place.PlaceRepository;
import com.valhalla.domain.place.PoiStatus;
import com.valhalla.infrastructure.place.PoiIngestService;
import com.valhalla.presentation.place.PoiView;
import com.valhalla.presentation.shared.LoginRedirects;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = "place.import.freshness-minutes=0")
public class PlaceImportControllerTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";
  private static final String USER_EMAIL = "user@unlam.edu.ar";
  private static final String ERROR_FORBIDDEN_QUERY = "Unsupported query: use amenity or shop";
  private static final String ERROR_OUTSIDE_CABA = "The visible area must be inside CABA";

  /** Area visible de ejemplo, ya dentro de CABA: el recorte no la toca. */
  private static final BoundingBox VISIBLE = new BoundingBox(-34.62, -58.45, -34.58, -58.37);
  private static final String VISIBLE_PARAMS = "&south=-34.62&west=-58.45&north=-34.58&east=-58.37";

  private static final OsmPoi FIXTURE_POI = new OsmPoi(
    "Parrilla del Puerto",
    "Av. Corrientes 1234",
    -34.6037,
    -58.3816,
    "Mo-Su 12:00-23:00",
    "+54 11 4444-5555",
    "https://parrilla.example.com",
    "amenity",
    "restaurant",
    "way",
    1L
  );

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private PlaceRepository placeRepository;

  @Autowired
  private MapPoiRepository mapPoiRepository;

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
      // El endpoint y la attribution del tile viven en pages/place-import.js desde la fase 3.
      .andExpect(content().string(containsString("/js/pages/place-import.js")))
      // El boton reemplaza a la carga automatica: sin click no hay consulta.
      .andExpect(content().string(containsString("id=\"load-pois\"")))
      // La caja de CABA viaja en el modelo hacia la vista, que decide si el boton se habilita.
      .andExpect(model().attribute("cabaBounds", hasEntry("south", -34.7058)))
      .andExpect(content().string(containsString("-34.7058")));

    String module = Files.readString(
      Paths.get("src", "main", "webapp", "resources", "core", "js", "pages", "place-import.js")
    );
    assertThat(module, containsString("/admin/places/import/pois"));

    verifyNoInteractions(this.overpassClient);
  }

  // --- I-16 ---

  /**
   * Leer pinta lo que ya esta guardado. Es el punto del cambio: entrar al importador o mover el
   * mapa no puede costar una consulta de 30 s.
   */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldServeTheStoredPoisWithoutTouchingOverpass() throws Exception {
    mapPoiRepository.saveAll(List.of(storedPoi("way", 1L, "amenity", "restaurant")));

    List<PoiView> pois = poisFrom(
      get("/admin/places/import/pois?amenity=restaurant" + VISIBLE_PARAMS)
    );

    assertThat(pois, hasSize(1));
    assertThat(pois.getFirst().name(), is(FIXTURE_POI.name()));
    assertThat(pois.getFirst().address(), is(FIXTURE_POI.address()));
    assertThat(pois.getFirst().latitude(), is(FIXTURE_POI.latitude()));
    assertThat(pois.getFirst().longitude(), is(FIXTURE_POI.longitude()));
    assertThat(pois.getFirst().openingHours(), is(FIXTURE_POI.openingHours()));
    assertThat(pois.getFirst().externalId(), is("osm:way/1"));
    assertThat(
      "el punto todavia no es un lugar guardado",
      pois.getFirst().savedPlaceId(),
      is(nullValue())
    );
    verifyNoInteractions(this.overpassClient);
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldAlsoAcceptTheShopKey() throws Exception {
    mapPoiRepository.saveAll(List.of(storedPoi("node", 5L, "shop", "clothes")));

    assertThat(
      poisFrom(get("/admin/places/import/pois?shop=clothes" + VISIBLE_PARAMS)),
      hasSize(1)
    );
    verifyNoInteractions(this.overpassClient);
  }

  /** Una vista mas grande que la ciudad se recorta: el importador no consulta fuera de CABA. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldClipTheVisibleAreaToCaba() throws Exception {
    BoundingBox zona = PoiIngestService.zoneOf(BoundingBox.CABA);
    given(this.overpassClient.findPois("amenity", "restaurant", zona))
      .willReturn(List.of(FIXTURE_POI));

    List<PoiView> pois = poisFrom(
      get("/admin/places/import/refresh?amenity=restaurant&south=-35&west=-59&north=-34&east=-58")
    );

    assertThat(pois, hasSize(1));
  }

  /** El marcador se pinta por categoria y el alta reusa la identidad: los dos viajan por punto. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldServeTheCategoryAndTheIdentityOfEachPoi() throws Exception {
    mapPoiRepository.saveAll(
      List.of(storedPoi("way", 1L, "amenity", "restaurant"), storedPoiWithoutOsmId())
    );

    JsonNode pois = envelopeFrom(
      get("/admin/places/import/pois?amenity=restaurant" + VISIBLE_PARAMS)
    )
      .path("pois");

    assertThat(pois.size(), is(2));
    assertThat(poiWithExternalId(pois, "osm:way/1").path("category").asText(), is("RESTAURANT"));
    assertThat(poiWithExternalId(pois, "osm:way/1").path("externalId").asText(), is("osm:way/1"));
    assertThat(
      "sin id de OSM no hay identidad que mandar al alta",
      poiWithoutExternalId(pois).path("externalId").isNull(),
      is(true)
    );
  }

  /**
   * El punto que ya es un lugar guardado se marca con su id, y solo ese. La suite es transaccional,
   * asi que la fila no se escapa al baseline de 12 lugares de las demas suites.
   */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldFlagThePoiThatIsAlreadyAPlace() throws Exception {
    Place saved = new Place();
    saved.setName("Parrilla del Puerto");
    saved.setCategory(PlaceCategory.RESTAURANT);
    saved.setExternalId("osm:node/9001");
    placeRepository.save(saved);
    mapPoiRepository.saveAll(
      List.of(
        storedPoi("node", 9001L, "amenity", "restaurant"),
        storedPoi("way", 1L, "amenity", "restaurant")
      )
    );

    List<PoiView> pois = poisFrom(
      get("/admin/places/import/pois?amenity=restaurant" + VISIBLE_PARAMS)
    );

    assertThat(pois, hasSize(2));
    assertThat(savedPlaceIdOf(pois, "osm:node/9001"), is(saved.getId()));
    assertThat(savedPlaceIdOf(pois, "osm:way/1"), is(nullValue()));
  }

  // --- ingesta desde el boton ---

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldIngestTheZoneAndAnswerTheSameEnvelope() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));

    JsonNode envelope = envelopeFrom(
      get("/admin/places/import/refresh?amenity=restaurant" + VISIBLE_PARAMS)
    );

    assertThat(envelope.path("pois").size(), is(1));
    assertThat(envelope.path("pois").get(0).path("name").asText(), is("Parrilla del Puerto"));
    assertThat(envelope.path("fetchedAt").isTextual(), is(true));
    // Y la lectura siguiente ya sale de la base, sin volver a Overpass.
    assertThat(
      poisFrom(get("/admin/places/import/pois?amenity=restaurant" + VISIBLE_PARAMS)),
      hasSize(1)
    );
    verify(this.overpassClient, times(1))
      .findPois(anyString(), anyString(), any(BoundingBox.class));
  }

  /**
   * Recargar la misma zona con los mismos datos no escribe nada: es el diff, no un reemplazo. La
   * fila conserva su id, que es la prueba de que se actualizo la misma y no se borro y recreo.
   */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldApplyOnlyTheDiffWhenTheSameZoneIsLoadedTwice() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));

    envelopeFrom(get("/admin/places/import/refresh?amenity=restaurant" + VISIBLE_PARAMS));
    UUID firstId = mapPoiRepository.findInBox("amenity", "restaurant", VISIBLE).getFirst().getId();
    envelopeFrom(get("/admin/places/import/refresh?amenity=restaurant" + VISIBLE_PARAMS));

    List<MapPoi> stored = mapPoiRepository.findInBox("amenity", "restaurant", VISIBLE);
    assertThat(stored, hasSize(1));
    assertThat(stored.getFirst().getId(), is(firstId));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldReportNoTimestampForAZoneThatWasNeverLoaded() throws Exception {
    JsonNode envelope = envelopeFrom(
      get("/admin/places/import/pois?amenity=restaurant" + VISIBLE_PARAMS)
    );

    assertThat(envelope.path("pois").size(), is(0));
    assertThat(envelope.path("fetchedAt").isNull(), is(true));
  }

  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldAnswerBadGatewayWhenOverpassFails() throws Exception {
    given(this.overpassClient.findPois(anyString(), anyString(), any(BoundingBox.class)))
      .willThrow(new IllegalStateException("Overpass answered HTTP 504"));

    String body =
      this.mockMvc.perform(get("/admin/places/import/refresh?amenity=restaurant" + VISIBLE_PARAMS))
        .andExpect(status().isBadGateway())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);

    assertThat(jsonOf(body).path("error").asText(), is("Overpass is not available right now"));
  }

  /** Un 502 de Overpass no puede vaciar el mapa: lo guardado queda como estaba. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldKeepTheStoredPoisWhenOverpassFails() throws Exception {
    mapPoiRepository.saveAll(List.of(storedPoi("way", 1L, "amenity", "restaurant")));
    given(this.overpassClient.findPois(anyString(), anyString(), any(BoundingBox.class)))
      .willThrow(new IllegalStateException("Overpass answered HTTP 502"));

    this.mockMvc.perform(get("/admin/places/import/refresh?amenity=restaurant" + VISIBLE_PARAMS))
      .andExpect(status().isBadGateway());

    List<MapPoi> stored = mapPoiRepository.findInBox("amenity", "restaurant", VISIBLE);
    assertThat(stored, hasSize(1));
    assertThat(stored.getFirst().getStatus(), is(PoiStatus.ACTIVE));
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
          .param("defaultDurationMinutes", "90")
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
              hasProperty("category", is(PlaceCategory.RESTAURANT)),
              hasProperty("defaultDurationMinutes", is(90))
            )
          )
      )
      .andExpect(content().string(containsString("Parrilla del Puerto")));
  }

  /** [SCH] OSM no trae duracion: una basura en el param no rompe el form, queda vacia. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void ignorarUnaDuracionTipicaQueNoEsUnNumero() throws Exception {
    this.mockMvc.perform(
        get("/admin/places/new")
          .param("name", "Sin Duracion")
          .param("defaultDurationMinutes", "noventa")
      )
      .andExpect(status().isOk())
      .andExpect(
        model().attribute("placeForm", hasProperty("defaultDurationMinutes", nullValue()))
      );
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
      this.mockMvc.perform(get("/admin/places/import/pois?color=red" + VISIBLE_PARAMS))
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    assertThat(jsonOf(body).path("error").asText(), is(ERROR_FORBIDDEN_QUERY));

    // Parametro desconocido al lado de uno valido.
    this.mockMvc.perform(
        get("/admin/places/import/pois?amenity=restaurant&evil=x" + VISIBLE_PARAMS)
      )
      .andExpect(status().isBadRequest());
    // Dos tags en la misma consulta.
    this.mockMvc.perform(
        get("/admin/places/import/pois?amenity=restaurant&shop=clothes" + VISIBLE_PARAMS)
      )
      .andExpect(status().isBadRequest());
    // Sin tag, o con el tag vacio.
    this.mockMvc.perform(
        get("/admin/places/import/pois?south=-34.62&west=-58.45&north=-34.58&east=-58.37")
      )
      .andExpect(status().isBadRequest());
    this.mockMvc.perform(get("/admin/places/import/pois?amenity=" + VISIBLE_PARAMS))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(this.overpassClient);
  }

  /** La caja tambien es entrada del usuario: sin bordes, fuera de CABA o invertida es 400. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldRejectAVisibleAreaThatCannotBeQueried() throws Exception {
    String body =
      this.mockMvc.perform(get("/admin/places/import/pois?amenity=restaurant"))
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    assertThat(jsonOf(body).path("error").asText(), is(ERROR_OUTSIDE_CABA));

    // Rosario: la vista no toca CABA.
    this.mockMvc.perform(
        get(
          "/admin/places/import/pois?amenity=restaurant&south=-33&west=-60.7&north=-32.9&east=-60.6"
        )
      )
      .andExpect(status().isBadRequest());
    // Caja invertida.
    this.mockMvc.perform(
        get(
          "/admin/places/import/pois?amenity=restaurant&south=-34&west=-58.4&north=-35&east=-58.3"
        )
      )
      .andExpect(status().isBadRequest());
    // Coordenada que no es un numero.
    this.mockMvc.perform(
        get(
          "/admin/places/import/pois?amenity=restaurant&south=sur&west=-58.4&north=-33&east=-58.3"
        )
      )
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
   * GET a un endpoint del importador y parseo del sobre con Jackson: el classpath de test no trae
   * json-path, asi que las aserciones van sobre el objeto (mismo seam que PlaceAdminControllerTest
   * usa para la API).
   */
  private JsonNode envelopeFrom(MockHttpServletRequestBuilder request) throws Exception {
    String body =
      this.mockMvc.perform(request)
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString(StandardCharsets.UTF_8);
    return new ObjectMapper().readTree(body);
  }

  private List<PoiView> poisFrom(MockHttpServletRequestBuilder request) throws Exception {
    return Arrays.asList(
      new ObjectMapper().readValue(envelopeFrom(request).path("pois").toString(), PoiView[].class)
    );
  }

  /** El punto con esa identidad, o un fallo explicito si el sobre no lo trae. */
  private JsonNode poiWithExternalId(JsonNode pois, String externalId) {
    for (JsonNode poi : pois) {
      if (externalId.equals(poi.path("externalId").asText())) {
        return poi;
      }
    }
    throw new AssertionError("el sobre no trae el POI " + externalId);
  }

  /** El unico punto que no viaja con identidad OSM. */
  private JsonNode poiWithoutExternalId(JsonNode pois) {
    for (JsonNode poi : pois) {
      if (poi.path("externalId").isNull()) {
        return poi;
      }
    }
    throw new AssertionError("el sobre no trae un POI sin identidad OSM");
  }

  private UUID savedPlaceIdOf(List<PoiView> pois, String externalId) {
    return pois
      .stream()
      .filter(poi -> externalId.equals(poi.externalId()))
      .findFirst()
      .orElseThrow()
      .savedPlaceId();
  }

  /** Una fila ya guardada, con los mismos datos del fixture para poder comparar por equals. */
  private static MapPoi storedPoi(String type, Long id, String tagKey, String tagValue) {
    OsmPoi poi = new OsmPoi(
      FIXTURE_POI.name(),
      FIXTURE_POI.address(),
      FIXTURE_POI.latitude(),
      FIXTURE_POI.longitude(),
      FIXTURE_POI.openingHours(),
      FIXTURE_POI.phone(),
      FIXTURE_POI.website(),
      tagKey,
      tagValue,
      type,
      id
    );
    return MapPoi.from(poi, PlaceCategory.fromOsm(tagKey, tagValue), tagKey, tagValue);
  }

  /** Fila sin id de OSM: su clave cae al fallback, asi que no tiene identidad que ofrecer. */
  private static MapPoi storedPoiWithoutOsmId() {
    OsmPoi poi = new OsmPoi(
      "Kiosco del Puerto",
      null,
      FIXTURE_POI.latitude(),
      FIXTURE_POI.longitude(),
      null,
      null,
      null,
      "amenity",
      "restaurant",
      null,
      null
    );
    return MapPoi.from(
      poi,
      PlaceCategory.fromOsm("amenity", "restaurant"),
      "amenity",
      "restaurant"
    );
  }

  private JsonNode jsonOf(String body) throws Exception {
    return new ObjectMapper().readTree(body);
  }
}
