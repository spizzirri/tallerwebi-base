package com.valhalla.infrastructure.place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.OsmPoi;
import com.valhalla.domain.place.OverpassClient;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Unica implementacion con red de {@link OverpassClient}: un POST form-urlencoded al endpoint
 * configurable ({@code overpass.url}) y el parseo del JSON de respuesta. Sin API key y sin
 * dependencia nueva: {@link HttpClient} es del JDK.
 *
 * <p>El query param nunca se interpola crudo: el key se valida contra un allowlist y el value por
 * forma, porque el endpoint es autenticado pero la inyeccion en Overpass QL no deja de ser un
 * agujero innecesario.
 *
 * <p>El parseo vive en {@link #parsePois(String)}, un metodo estatico que recibe el body: los
 * tests lo alimentan con un fixture y no tocan la red (U-10..U-12, AC-10).
 */
@Component
public class HttpOverpassClient implements OverpassClient {

  /** Los unicos tags que el importador ofrece; tambien ordena el osmKey que sale de la respuesta. */
  private static final Set<String> ALLOWED_KEYS = Set.of("amenity", "shop");

  /** Area de Overpass de la relation de CABA: 3600000000 + el id de la relation en OSM. */
  private static final String CABA_AREA = "3600345769";
  private static final String OSM_VALUE_PATTERN = "[a-z0-9_]+";
  private static final String USER_AGENT = "PlanIt-Valhalla/1.0 (Taller Web I, UNLaM)";
  private static final int OK = 200;
  private static final int RATE_LIMITED = 429;
  private static final long RETRY_DELAY_MILLIS = 30_000L;
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final HttpClient httpClient;
  private final String endpoint;

  @Autowired
  public HttpOverpassClient(
    @Value("${overpass.url:https://overpass-api.de/api/interpreter}") String endpoint
  ) {
    this.endpoint = endpoint;
    this.httpClient =
      HttpClient
        .newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  @Override
  public List<OsmPoi> findCabaPois(String osmKey, String osmValue) {
    String query = buildQuery(osmKey, osmValue);
    HttpResponse<String> response = post(query);
    if (response.statusCode() == RATE_LIMITED) {
      // Overpass limita por IP: se espera 30s y se reintenta una sola vez.
      waitBeforeRetry();
      response = post(query);
    }
    if (response.statusCode() != OK) {
      throw new IllegalStateException("Overpass answered HTTP " + response.statusCode());
    }
    return parsePois(response.body());
  }

  /**
   * Arma la query de Overpass con el key validado contra el allowlist y el value validado por
   * forma. Package-private para poder probarlo sin red.
   */
  static String buildQuery(String osmKey, String osmValue) {
    String key = normalize(osmKey);
    if (!ALLOWED_KEYS.contains(key)) {
      throw new IllegalArgumentException("Unsupported OSM key: " + osmKey);
    }
    String value = normalize(osmValue);
    if (!value.matches(OSM_VALUE_PATTERN)) {
      throw new IllegalArgumentException("Unsupported OSM value: " + osmValue);
    }
    return (
      "[out:json][timeout:20];nwr[\"" +
      key +
      "\"=\"" +
      value +
      "\"](area=" +
      CABA_AREA +
      ");out center tags;"
    );
  }

  /**
   * Parsea el body de Overpass ({@code elements[]}) sin tocar la red. Los elementos sin
   * {@code name} se descartan porque el ABM no tiene como identificarlos; los tags ausentes quedan
   * en {@code null} y nunca lanzan.
   */
  static List<OsmPoi> parsePois(String json) {
    JsonNode elements;
    try {
      elements = MAPPER.readTree(json).path("elements");
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Unreadable Overpass response", exception);
    }
    List<OsmPoi> pois = new ArrayList<>();
    for (JsonNode element : elements) {
      OsmPoi poi = toPoi(element);
      if (poi != null) {
        pois.add(poi);
      }
    }
    return pois;
  }

  private static OsmPoi toPoi(JsonNode element) {
    JsonNode tags = element.path("tags");
    String name = text(tags, "name");
    if (name == null) {
      return null;
    }
    String osmKey = ALLOWED_KEYS
      .stream()
      .filter(key -> text(tags, key) != null)
      .findFirst()
      .orElse(null);
    return new OsmPoi(
      name,
      address(tags),
      coordinate(element, "lat"),
      coordinate(element, "lon"),
      text(tags, "opening_hours"),
      firstText(tags, "phone", "contact:phone"),
      firstText(tags, "website", "contact:website"),
      osmKey,
      osmKey == null ? null : text(tags, osmKey)
    );
  }

  /** {@code out center} da center.lat/lon para ways y relations; los nodes traen lat/lon directo. */
  private static Double coordinate(JsonNode element, String field) {
    JsonNode direct = element.path(field);
    if (direct.isNumber()) {
      return direct.asDouble();
    }
    JsonNode center = element.path("center").path(field);
    return center.isNumber() ? center.asDouble() : null;
  }

  private static String address(JsonNode tags) {
    String street = text(tags, "addr:street");
    String number = text(tags, "addr:housenumber");
    if (street == null) {
      return number;
    }
    return number == null ? street : street + " " + number;
  }

  private static String firstText(JsonNode tags, String field, String fallback) {
    String value = text(tags, field);
    return value != null ? value : text(tags, fallback);
  }

  private static String text(JsonNode container, String field) {
    JsonNode value = container.path(field);
    return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private HttpResponse<String> post(String query) {
    HttpRequest request = buildRequest(endpoint, query);
    try {
      return httpClient.send(request, BodyHandlers.ofString());
    } catch (IOException exception) {
      throw new IllegalStateException("Overpass is unreachable", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Overpass request interrupted", exception);
    }
  }

  /**
   * El contrato de salida —POST form-urlencoded con {@code data=<query>}, User-Agent
   * identificable y el endpoint configurado— se arma aparte para poder probarlo sin red.
   */
  static HttpRequest buildRequest(String endpoint, String query) {
    return HttpRequest
      .newBuilder(URI.create(endpoint))
      .timeout(REQUEST_TIMEOUT)
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("User-Agent", USER_AGENT)
      .POST(BodyPublishers.ofString("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8)))
      .build();
  }

  /**
   * Espera del 429. Vive en su propio metodo para que el camino quede fuera de los tests: ninguna
   * suite debe dormir 30 segundos (AC-10).
   */
  private void waitBeforeRetry() {
    try {
      Thread.sleep(RETRY_DELAY_MILLIS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Overpass retry interrupted", exception);
    }
  }
}
