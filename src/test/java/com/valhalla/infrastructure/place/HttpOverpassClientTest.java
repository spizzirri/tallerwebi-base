package com.valhalla.infrastructure.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.valhalla.domain.place.OsmPoi;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * U-10..U-12: el parseo se prueba con un fixture de Overpass, sin red (AC-04, AC-10). El camino
 * HTTP y el reintento del 429 quedan fuera a proposito: ningun test debe dormir 30 segundos ni
 * depender de internet.
 */
public class HttpOverpassClientTest {

  /** Tres elementos: un way con center y todos los tags, un node con lat/lon y solo contact:*, y
   * uno sin name. */
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

  // --- query: allowlist y escape ---

  @Test
  public void shouldBuildTheCabaQueryForAnAllowedKey() {
    assertThat(
      HttpOverpassClient.buildQuery("amenity", "restaurant"),
      is("[out:json][timeout:20];nwr[\"amenity\"=\"restaurant\"](area=3600345769);out center tags;")
    );
    assertThat(
      HttpOverpassClient.buildQuery(" shop ", "Clothes"),
      is("[out:json][timeout:20];nwr[\"shop\"=\"clothes\"](area=3600345769);out center tags;")
    );
  }

  @Test
  public void shouldRejectAKeyOutsideTheAllowlist() {
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("color", "red")
    );
    assertThrows(IllegalArgumentException.class, () -> HttpOverpassClient.buildQuery(null, "red"));
  }

  @Test
  public void shouldRejectAValueThatWouldInjectQuerySyntax() {
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("amenity", "restaurant\"](area=1);out;")
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> HttpOverpassClient.buildQuery("amenity", " ")
    );
  }

  // --- contrato de salida: POST form-urlencoded al endpoint configurado, sin red ---

  @Test
  public void shouldPostTheQueryAsAFormBodyWithAnIdentifiableUserAgent() {
    HttpRequest request = HttpOverpassClient.buildRequest(
      "http://localhost:1234/api/interpreter",
      HttpOverpassClient.buildQuery("amenity", "restaurant")
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
}
