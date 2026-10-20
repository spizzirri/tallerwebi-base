package com.valhalla.infrastructure.place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhalla.domain.place.BoundingBox;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

  /** Solo POIs con nombre (el ABM no puede identificar los demas) y un tope para acotar la respuesta. */
  private static final int MAX_POIS = 300;

  /** Presupuesto del servidor; el cliente espera un poco mas para dejar que Overpass conteste. */
  private static final int SERVER_TIMEOUT_SECONDS = 60;

  private static final String OSM_VALUE_PATTERN = "[a-z0-9_]+";
  private static final String USER_AGENT = "PlanIt-Valhalla/1.0 (Taller Web I, UNLaM)";
  private static final int OK = 200;
  private static final int RATE_LIMITED = 429;
  private static final int BAD_GATEWAY = 502;
  private static final int SERVICE_UNAVAILABLE = 503;
  private static final int GATEWAY_TIMEOUT = 504;

  /** El 429 es por IP: la espera tiene que ser del orden del minuto, no de milisegundos. */
  private static final long RATE_LIMIT_DELAY_MILLIS = 30_000L;

  /**
   * En hora pico el endpoint publico responde 502 mientras la cola esta llena y se recupera en
   * segundos: dos reintentos con espera corta alcanzan, sin dejar al usuario esperando un minuto.
   */
  private static final long BUSY_DELAY_MILLIS = 1_000L;
  private static final int MAX_RETRIES = 2;
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

  /**
   * Overpass tarda entre 25 y 45 segundos en una consulta de CABA sin cachear. Con 30s el cliente
   * cortaba antes de que el servidor contestara y el importador fallaba siempre.
   */
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Cada consulta cuesta ~30 s, asi que el resultado se guarda 10 minutos por
   * {@code key=value@caja}: repetir la misma zona con el mismo tag es instantaneo y de paso se
   * golpea menos el endpoint publico.
   */
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private final HttpClient httpClient;
  private final String endpoint;
  private final long rateLimitDelayMillis;
  private final long busyDelayMillis;
  private final Duration cacheTtl;
  private final Map<String, CachedPois> cache = new ConcurrentHashMap<>();

  @Autowired
  public HttpOverpassClient(
    @Value("${overpass.url:https://overpass-api.de/api/interpreter}") String endpoint
  ) {
    this(endpoint, jdkHttpClient(), RATE_LIMIT_DELAY_MILLIS, BUSY_DELAY_MILLIS, CACHE_TTL);
  }

  /**
   * Seam de tests: recibe el {@link HttpClient}, las dos esperas y el TTL para que el camino HTTP
   * (reintentos, error, IOException, interrupcion) y el cache se prueben sin red y sin dormir.
   * Package-private a proposito: produccion entra siempre por el constructor de arriba.
   */
  HttpOverpassClient(
    String endpoint,
    HttpClient httpClient,
    long rateLimitDelayMillis,
    long busyDelayMillis,
    Duration cacheTtl
  ) {
    this.endpoint = endpoint;
    this.httpClient = httpClient;
    this.rateLimitDelayMillis = rateLimitDelayMillis;
    this.busyDelayMillis = busyDelayMillis;
    this.cacheTtl = cacheTtl;
  }

  /** Entrada del cache: la lista ya parseada y el instante en que deja de valer. */
  private record CachedPois(List<OsmPoi> pois, Instant expiresAt) {}

  private static HttpClient jdkHttpClient() {
    return HttpClient
      .newBuilder()
      .connectTimeout(CONNECT_TIMEOUT)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();
  }

  @Override
  public List<OsmPoi> findPois(String osmKey, String osmValue, BoundingBox box) {
    String query = buildQuery(osmKey, osmValue, box);
    String cacheKey = normalize(osmKey) + "=" + normalize(osmValue) + "@" + box.cacheKey();
    List<OsmPoi> cached = cachedPois(cacheKey);
    if (cached != null) {
      return cached;
    }
    HttpResponse<String> response = postWithRetries(query);
    if (response.statusCode() != OK) {
      throw new IllegalStateException("Overpass answered HTTP " + response.statusCode());
    }
    List<OsmPoi> pois = List.copyOf(parsePois(response.body()));
    cache.put(cacheKey, new CachedPois(pois, Instant.now().plus(cacheTtl)));
    return pois;
  }

  /** {@code null} si no hay entrada o ya vencio; el TTL se mide desde que se guardo. */
  private List<OsmPoi> cachedPois(String cacheKey) {
    CachedPois entry = cache.get(cacheKey);
    return entry != null && entry.expiresAt().isAfter(Instant.now()) ? entry.pois() : null;
  }

  /**
   * El endpoint publico falla de dos maneras transitorias: 429 por rate limit (por IP, hay que
   * esperar del orden del minuto) y 502/503/504 cuando la cola esta llena (se recupera en segundos).
   * Dos reintentos cubren las dos; si igual falla, se propaga el status y la vista invita a probar
   * de nuevo.
   */
  private HttpResponse<String> postWithRetries(String query) {
    HttpResponse<String> response = post(query);
    for (
      int attempt = 0;
      attempt < MAX_RETRIES && isTransientFailure(response.statusCode());
      attempt++
    ) {
      waitBeforeRetry(response.statusCode());
      response = post(query);
    }
    return response;
  }

  private static boolean isTransientFailure(int status) {
    return (
      status == RATE_LIMITED ||
      status == BAD_GATEWAY ||
      status == SERVICE_UNAVAILABLE ||
      status == GATEWAY_TIMEOUT
    );
  }

  /**
   * Arma la query de Overpass con el key validado contra el allowlist, el value validado por forma
   * y la caja del area visible. Package-private para poder probarlo sin red.
   */
  static String buildQuery(String osmKey, String osmValue, BoundingBox box) {
    String key = normalize(osmKey);
    if (!ALLOWED_KEYS.contains(key)) {
      throw new IllegalArgumentException("Unsupported OSM key: " + osmKey);
    }
    String value = normalize(osmValue);
    if (!value.matches(OSM_VALUE_PATTERN)) {
      throw new IllegalArgumentException("Unsupported OSM value: " + osmValue);
    }
    return (
      "[out:json][timeout:" +
      SERVER_TIMEOUT_SECONDS +
      "];nwr[\"" +
      key +
      "\"=\"" +
      value +
      "\"][\"name\"](" +
      box.toOverpass() +
      ");out center tags " +
      MAX_POIS +
      ";"
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
      website(tags),
      osmKey,
      osmKey == null ? null : text(tags, osmKey)
    );
  }

  /**
   * OSM carga muchas veces {@code website} sin esquema ("bar.example.com"). El form del ABM valida
   * {@code type="url"}, asi que ese valor bloquearia el guardado: se normaliza a https aca, en el
   * borde por donde entra el dato, y no en cada vista que lo muestra.
   */
  private static String website(JsonNode tags) {
    String website = firstText(tags, "website", "contact:website");
    if (website == null || website.contains("://")) {
      return website;
    }
    return "https://" + website;
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
   * Espera entre reintentos: larga para el 429 (el limite es por IP) y corta para el endpoint
   * saturado. Las dos esperas vienen del constructor para que los tests las pongan en cero: ninguna
   * suite duerme los 30 segundos reales (AC-10).
   */
  private void waitBeforeRetry(int status) {
    long delay = status == RATE_LIMITED ? rateLimitDelayMillis : busyDelayMillis;
    try {
      Thread.sleep(delay);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Overpass retry interrupted", exception);
    }
  }
}
