package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.OverpassClient;
import com.valhalla.domain.place.PoiZone;
import com.valhalla.domain.place.PoiZoneRepository;
import com.valhalla.infrastructure.place.PoiIngestService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
 * La ventana de frescura del importador: mientras lo guardado de una zona+tag siga vigente, el
 * boton "Actualizar" no vuelve a pagar los 25-45 s de Overpass y el sobre lo declara con
 * {@code refreshed: false}.
 *
 * <p>La ventana vive en el controller ({@code place.import.freshness-minutes}), asi que esta clase
 * fija el valor con {@code @TestPropertySource}; {@link PlaceImportControllerTest} corre con la
 * ventana en 0 para seguir probando el diff en cada carga.
 */
@WebIntegrationTest
@Transactional
@TestPropertySource(properties = "place.import.freshness-minutes=15")
public class PlaceImportFreshnessTest {

  private static final String ADMIN_EMAIL = "admin@unlam.edu.ar";

  /** Area visible de ejemplo, ya dentro de CABA. */
  private static final BoundingBox VISIBLE = new BoundingBox(-34.62, -58.45, -34.58, -58.37);
  private static final String VISIBLE_PARAMS = "&south=-34.62&west=-58.45&north=-34.58&east=-58.37";
  private static final String REFRESH = "/admin/places/import/refresh?amenity=restaurant";
  private static final String STORED = "/admin/places/import/pois?amenity=restaurant";

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
  private PoiZoneRepository poiZoneRepository;

  @MockitoBean
  private OverpassClient overpassClient;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
  }

  /** Primera pasada: no hay cobertura, se consulta. Segunda pasada dentro de la ventana: no. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldSkipOverpassWhenTheZoneWasFetchedWithinTheWindow() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));

    JsonNode first = envelopeFrom(get(REFRESH + VISIBLE_PARAMS));
    assertThat(first.path("refreshed").asBoolean(), is(true));
    assertThat(first.path("pois").size(), is(1));
    String firstFetchedAt = first.path("fetchedAt").asText();

    JsonNode second = envelopeFrom(get(REFRESH + VISIBLE_PARAMS));

    assertThat(
      "la segunda pasada no pago Overpass",
      second.path("refreshed").asBoolean(),
      is(false)
    );
    assertThat(second.path("pois").size(), is(1));
    assertThat("la cobertura no se movio", second.path("fetchedAt").asText(), is(firstFetchedAt));
    verify(this.overpassClient, times(1))
      .findPois(anyString(), anyString(), any(BoundingBox.class));
  }

  /** Una zona vencida se vuelve a pedir: la ventana no congela los datos para siempre. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldQueryOverpassAgainWhenTheZoneIsStale() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));

    envelopeFrom(get(REFRESH + VISIBLE_PARAMS));
    ageTheZone(Instant.now().minus(1, ChronoUnit.HOURS));

    JsonNode second = envelopeFrom(get(REFRESH + VISIBLE_PARAMS));

    assertThat(second.path("refreshed").asBoolean(), is(true));
    assertThat(second.path("pois").size(), is(1));
    verify(this.overpassClient, times(2))
      .findPois(anyString(), anyString(), any(BoundingBox.class));
  }

  /** "Nunca se cargo" no es fresco: la primera pasada siempre consulta. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldQueryOverpassWhenTheZoneWasNeverFetched() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));

    JsonNode envelope = envelopeFrom(get(REFRESH + VISIBLE_PARAMS));

    assertThat(envelope.path("refreshed").asBoolean(), is(true));
    verify(this.overpassClient, times(1))
      .findPois(anyString(), anyString(), any(BoundingBox.class));
  }

  /** Leer lo guardado nunca es un refresh, ni siquiera con la zona recien traida. */
  @Test
  @WithMockUser(username = ADMIN_EMAIL, roles = { "ADMIN" })
  public void shouldNeverReportARefreshOnTheStoredRead() throws Exception {
    given(this.overpassClient.findPois("amenity", "restaurant", VISIBLE))
      .willReturn(List.of(FIXTURE_POI));
    envelopeFrom(get(REFRESH + VISIBLE_PARAMS));

    JsonNode stored = envelopeFrom(get(STORED + VISIBLE_PARAMS));

    assertThat(stored.path("refreshed").asBoolean(), is(false));
    assertThat(stored.path("pois").size(), is(1));
    verify(this.overpassClient, times(1))
      .findPois(anyString(), anyString(), any(BoundingBox.class));
  }

  /** Mueve el sello de la cobertura al pasado, que es lo unico que mira la ventana. */
  private void ageTheZone(Instant fetchedAt) {
    String key = PoiIngestService.zoneKey(PoiIngestService.zoneOf(VISIBLE));
    PoiZone zone =
      this.poiZoneRepository.find(key, "amenity", "restaurant")
        .orElseThrow(() -> new AssertionError("la zona no quedo sellada despues del primer refresh")
        );
    zone.setFetchedAt(fetchedAt);
    this.poiZoneRepository.save(zone);
  }

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
}
