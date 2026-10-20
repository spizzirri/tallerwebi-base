package com.valhalla.infrastructure.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.place.BoundingBox;
import com.valhalla.domain.place.OsmPoi;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * U-10..U-12: el parseo se prueba con un fixture de Overpass, sin red (AC-04, AC-10). El camino
 * HTTP se prueba con un {@link java.net.http.HttpClient} inyectado y el delay del 429 en cero, asi
 * que ningun test duerme ni depende de internet.
 */
public class HttpOverpassClientTest {

  /** Tres elementos: un way con center y todos los tags, un node con lat/lon y solo contact:*, y
   * uno sin name. */
  /** Caja concreta y distinta de CABA: prueba que la query usa la que recibe, no una constante. */
  private static final BoundingBox BOX = new BoundingBox(-34.62, -58.45, -34.58, -58.37);

  private static final String FIXTURE =
    """
    {
      "version": 0.6,
      "elements": [
        {
          "type": "way",
          "id": 1,
          "center": { "lat": -34.6037, "lon": -58.3816 },
          "tags": {
            "name": "Parrilla del Puerto",
            "amenity": "restaurant",
            "addr:street": "Av. Corrientes",
            "addr:housenumber": "1234",
            "opening_hours": "Mo-Su 12:00-23:00",
            "phone": "+54 11 4444-5555",
            "website": "https://parrilla.example.com"
          }
        },
        {
          "type": "node",
          "id": 2,
          "lat": -34.61,
          "lon": -58.37,
          "tags": {
            "name": "Bar Sin Datos",
            "amenity": "bar",
            "contact:phone": "+54 11 6666-7777",
            "contact:website": "https://bar.example.com"
          }
        },
        {
          "type": "node",
          "id": 3,
          "lat": -34.62,
          "lon": -58.36,
          "tags": { "amenity": "cafe", "shop": "clothes" }
        }
      ]
    }
    """;

  private static final String ENDPOINT = "http://overpass.test/api/interpreter";

  /** Un POI con name pero sin categoria, sin coordenadas, sin numero y con tags raros. */
  private static final String SPARSE_FIXTURE =
    """
    {
      "elements": [
        {
          "type": "node",
          "id": 9,
          "tags": {
            "name": "Kiosco de la esquina",
            "addr:street": "Av. Rivadavia",
            "opening_hours": 24,
            "website": "   "
          }
        }
      ]
    }
    """;

  // --- U-10 ---

  @Test
  public void shouldParseAnOverpassElementIntoAFullOsmPoi() {
    OsmPoi poi = HttpOverpassClient.parsePois(FIXTURE).getFirst();

    assertThat(poi.name(), is("Parrilla del Puerto"));
    assertThat(poi.address(), is("Av. Corrientes 1234"));
    assertThat(poi.latitude(), is(-34.6037));
    assertThat(poi.longitude(), is(-58.3816));
    assertThat(poi.openingHours(), is("Mo-Su 12:00-23:00"));
    assertThat(poi.phone(), is("+54 11 4444-5555"));
    assertThat(poi.website(), is("https://parrilla.example.com"));
    assertThat(poi.osmKey(), is("amenity"));
    assertThat(poi.osmValue(), is("restaurant"));
  }

  // --- U-11 ---

  @Test
  public void shouldLeaveAbsentTagsNullAndFallBackToTheContactTags() {
    OsmPoi poi = HttpOverpassClient.parsePois(FIXTURE).get(1);

    assertThat(poi.name(), is("Bar Sin Datos"));
    assertThat(poi.latitude(), is(-34.61));
    assertThat(poi.longitude(), is(-58.37));
    assertThat(poi.address(), is(nullValue()));
    assertThat(poi.openingHours(), is(nullValue()));
    assertThat(poi.phone(), is("+54 11 6666-7777"));
    assertThat(poi.website(), is("https://bar.example.com"));
  }

  /**
   * OSM suele traer la web sin esquema y el form del ABM usa {@code type="url"}: sin normalizar,
   * el alta pre-llenada queda bloqueada por el navegador.
   */
  @Test
  public void shouldAssumeHttpsForAWebsiteWithoutScheme() {
    String body =
      """
      {"elements":[{"type":"node","lat":-34.6,"lon":-58.38,
      "tags":{"name":"Bar","amenity":"bar","website":"bar.example.com"}}]}
      """;

    assertThat(
      HttpOverpassClient.parsePois(body).getFirst().website(),
      is("https://bar.example.com")
    );
  }

  // --- U-12 ---

  @Test
  public void shouldDiscardElementsWithoutName() {
    List<OsmPoi> pois = HttpOverpassClient.parsePois(FIXTURE);

    assertThat(pois, hasSize(2));
    assertThat(
      pois.stream().map(OsmPoi::name).toList(),
      contains("Parrilla del Puerto", "Bar Sin Datos")
    );
  }

  @Test
  public void shouldReturnAnEmptyListWhenTheResponseHasNoElements() {
    assertThat(HttpOverpassClient.parsePois("{\"elements\":[]}"), hasSize(0));
    assertThat(HttpOverpassClient.parsePois("{}"), hasSize(0));
  }

  @Test
  public void shouldRejectAResponseThatIsNotJson() {
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.parsePois("<html>429</html>")
    );
  }

  // --- tolerancia del parseo: nada de lo que falte puede lanzar ---

  @Test
  public void shouldTolerateAnElementWithoutCategoryCoordinatesOrStreetNumber() {
    OsmPoi poi = HttpOverpassClient.parsePois(SPARSE_FIXTURE).getFirst();

    assertThat(poi.name(), is("Kiosco de la esquina"));
    assertThat(poi.address(), is("Av. Rivadavia"));
    assertThat(poi.latitude(), is(nullValue()));
    assertThat(poi.longitude(), is(nullValue()));
    assertThat(poi.osmKey(), is(nullValue()));
    assertThat(poi.osmValue(), is(nullValue()));
    assertThat(poi.openingHours(), is(nullValue()));
    assertThat(poi.website(), is(nullValue()));
  }

  // --- query: allowlist y escape ---

  @Test
  public void shouldBuildTheQueryForTheBoxItReceives() {
    assertThat(
      HttpOverpassClient.buildQuery("amenity", "restaurant", BOX),
      is(
        "[out:json][timeout:60];nwr[\"amenity\"=\"restaurant\"][\"name\"](-34.620000,-58.450000,-34.580000,-58.370000);out center tags 300;"
      )
    );
    assertThat(
      HttpOverpassClient.buildQuery(" shop ", "Clothes", BOX),
      is(
        "[out:json][timeout:60];nwr[\"shop\"=\"clothes\"][\"name\"](-34.620000,-58.450000,-34.580000,-58.370000);out center tags 300;"
      )
    );
  }

  @Test
  public void shouldRejectAKeyOutsideTheAllowlist() {
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("color", "red", BOX)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery(null, "red", BOX)
    );
  }

  @Test
  public void shouldRejectAValueThatWouldInjectQuerySyntax() {
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("amenity", "restaurant\"](area=1);out;", BOX)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("amenity", " ", BOX)
    );
  }

  // --- contrato de salida: POST form-urlencoded al endpoint configurado, sin red ---

  @Test
  public void shouldPostTheQueryAsAFormBodyWithAnIdentifiableUserAgent() {
    HttpRequest request = HttpOverpassClient.buildRequest(
      "http://localhost:1234/api/interpreter",
      HttpOverpassClient.buildQuery("amenity", "restaurant", BOX)
    );

    assertThat(request.method(), is("POST"));
    assertThat(request.uri(), is(URI.create("http://localhost:1234/api/interpreter")));
    assertThat(
      request.headers().firstValue("Content-Type").orElseThrow(),
      is("application/x-www-form-urlencoded")
    );
    assertThat(
      request.headers().firstValue("User-Agent").orElseThrow(),
      containsString("Valhalla")
    );
  }

  // --- camino HTTP: cliente inyectado, sin red y con el delay del 429 en cero ---

  @Test
  public void shouldRetryOnceAfterRateLimitAndReturnTheParsedPois() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(429, ""), response(200, FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    List<OsmPoi> pois = overpass.findPois("amenity", "restaurant", BOX);

    assertThat(pois, hasSize(2));
    verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  public void shouldRetryOnceWhenOverpassIsAtCapacity() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(502, ""), response(200, FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    List<OsmPoi> pois = overpass.findPois("amenity", "restaurant", BOX);

    assertThat(pois, hasSize(2));
    verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  /** Tope de reintentos: dos, ni uno mas; si el endpoint sigue saturado se propaga el status. */
  @Test
  public void shouldStopRetryingAfterTwoAttempts() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(502, ""), response(503, ""));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    IllegalStateException error = assertThrows(
      IllegalStateException.class,
      () -> overpass.findPois("amenity", "restaurant", BOX)
    );

    assertThat(error.getMessage(), containsString("503"));
    verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  /** El gateway timeout tambien es transitorio: Overpass lo usa cuando se le pasa su presupuesto. */
  @Test
  public void shouldRetryWhenOverpassTimesOut() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(504, ""), response(200, FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    assertThat(overpass.findPois("amenity", "restaurant", BOX), hasSize(2));
    verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  /** Una consulta cuesta ~30 s: la segunda vez que se pide la misma categoria sale del cache. */
  @Test
  public void shouldServeASecondCallFromTheCache() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(200, FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    List<OsmPoi> first = overpass.findPois("amenity", "restaurant", BOX);
    List<OsmPoi> second = overpass.findPois(" amenity ", "RESTAURANT", BOX);

    assertThat(first, hasSize(2));
    assertThat(second, is(first));
    verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  /** El cache es por caja: la misma categoria en otra zona es otra consulta. */
  @Test
  public void shouldNotShareTheCacheBetweenBoxes() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(200, FIXTURE), response(200, SPARSE_FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    assertThat(overpass.findPois("amenity", "restaurant", BOX), hasSize(2));
    BoundingBox anotherNeighbourhood = new BoundingBox(-34.7, -58.5, -34.65, -58.45);
    assertThat(overpass.findPois("amenity", "restaurant", anotherNeighbourhood), hasSize(1));
    verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  /** El cache vence: con TTL 0 la segunda llamada vuelve a consultar (y a fallar si el stub no alcanza). */
  @Test
  public void shouldNotServeAnExpiredCacheEntry() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(200, FIXTURE), response(200, SPARSE_FIXTURE));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ZERO
    );

    assertThat(overpass.findPois("amenity", "restaurant", BOX), hasSize(2));
    assertThat(overpass.findPois("amenity", "restaurant", BOX), hasSize(1));
    verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  public void shouldThrowWhenOverpassAnswersSomethingOtherThanOk() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(500, "upstream boom"));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    IllegalStateException error = assertThrows(
      IllegalStateException.class,
      () -> overpass.findPois("amenity", "restaurant", BOX)
    );

    assertThat(error.getMessage(), containsString("500"));
    verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }

  @Test
  public void shouldWrapAnUnreachableOverpass() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    doThrow(new IOException("connection reset")).when(httpClient).send(any(), any());
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    IllegalStateException error = assertThrows(
      IllegalStateException.class,
      () -> overpass.findPois("amenity", "restaurant", BOX)
    );

    assertThat(error.getMessage(), containsString("unreachable"));
  }

  @Test
  public void shouldKeepTheInterruptFlagWhenTheRequestIsInterrupted() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    doThrow(new InterruptedException()).when(httpClient).send(any(), any());
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      0L,
      0L,
      Duration.ofMinutes(10)
    );

    try {
      IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> overpass.findPois("amenity", "restaurant", BOX)
      );

      assertThat(error.getMessage(), containsString("interrupted"));
      assertThat(Thread.currentThread().isInterrupted(), is(true));
    } finally {
      Thread.interrupted();
    }
  }

  /**
   * El reintento tambien puede interrumpirse mientras espera: el delay es de 1ms, no de 30s, y la
   * bandera se prende antes para que el sleep corte de inmediato.
   */
  @Test
  public void shouldFailTheRetryWhenTheWaitIsInterrupted() throws Exception {
    HttpClient httpClient = mock(HttpClient.class);
    stubSend(httpClient, response(429, ""));
    HttpOverpassClient overpass = new HttpOverpassClient(
      ENDPOINT,
      httpClient,
      1L,
      0L,
      Duration.ofMinutes(10)
    );

    Thread.currentThread().interrupt();
    try {
      IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> overpass.findPois("amenity", "restaurant", BOX)
      );

      assertThat(error.getMessage(), containsString("retry interrupted"));
    } finally {
      Thread.interrupted();
    }
  }

  @SuppressWarnings("unchecked")
  private static HttpResponse<String> response(int status, String body) {
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(status);
    when(response.body()).thenReturn(body);
    return response;
  }

  @SuppressWarnings("unchecked")
  private static void stubSend(
    HttpClient httpClient,
    HttpResponse<String> first,
    HttpResponse<String>... rest
  ) throws IOException, InterruptedException {
    doReturn(first, (Object[]) rest)
      .when(httpClient)
      .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }
}
