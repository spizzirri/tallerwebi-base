package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.microsoft.playwright.Route;
import com.valhalla.e2e.views.PlaceFormPage;
import com.valhalla.e2e.views.PlaceImportPage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * E-07..E-10: el importador visto desde el navegador. El proxy de Overpass se stubbea con {@code
 * page.route}, asi que la vista real corre en Chromium pero el navegador nunca sale a internet
 * (AC-10). El stub tambien es el unico modo de ejercitar el error sin depender de que Overpass este
 * saturado justo en la corrida, y de contar cuantas consultas se hicieron.
 */
public class PlaceImportE2E extends E2eBase {

  private static final String PARRILLA = "Parrilla del Puerto";

  /** Tres POIs: uno completo, uno sin datos opcionales y uno sin coordenadas (no se dibuja). */
  private static final String FIXTURE =
    """
    [
      {
        "name": "Parrilla del Puerto",
        "address": "Av. Corrientes 1234",
        "latitude": -34.6037,
        "longitude": -58.3816,
        "openingHours": "Mo-Su 12:00-23:00",
        "phone": "+54 11 4444-5555",
        "website": "https://parrilla.example.com",
        "osmKey": "amenity",
        "osmValue": "restaurant"
      },
      {
        "name": "Bar Sin Datos",
        "address": null,
        "latitude": -34.61,
        "longitude": -58.37,
        "openingHours": null,
        "phone": null,
        "website": null,
        "osmKey": "amenity",
        "osmValue": "bar"
      },
      {
        "name": "Café Sin Coordenadas",
        "address": "Av. Rivadavia 100",
        "latitude": null,
        "longitude": null,
        "openingHours": null,
        "phone": null,
        "website": null,
        "osmKey": "amenity",
        "osmValue": "restaurant"
      }
    ]
    """;

  @Test
  void shouldQueryOnlyWhenTheLoadButtonIsPressed() {
    signInAsAdmin();
    List<String> requests = new ArrayList<>();
    stubPois(200, FIXTURE, requests);

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForLoadButton();

    assertThat("al entrar no se consulta nada", requests, hasSize(0));
    assertThat("sin lista no hay buscador", view.hasSearchInput(), is(false));
    assertThat(view.hintText(), containsString("Cargar puntos de esta zona"));

    view.clickLoad();
    view.waitForPois(3);

    assertThat("una sola consulta por click", requests, hasSize(1));
    assertThat("con resultados aparece el buscador", view.hasSearchInput(), is(true));
  }

  @Test
  void shouldListTheStubbedPoisAndOpenThePrefilledForm() {
    signInAsAdmin();
    stubPois(200, FIXTURE);

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForLoadButton();
    view.clickLoad();
    view.waitForPois(3);

    assertThat(view.statusText(), is("3 punto(s) encontrados"));
    assertThat("el POI sin coordenadas no tiene marker", view.markerCount(), is(2));

    view.selectPoi(PARRILLA);
    view.waitForPath("/admin/places/new");
    page.waitForFunction("() => document.querySelector('#name')?.value === '" + PARRILLA + "'");

    PlaceFormPage form = new PlaceFormPage(page);
    assertThat(form.getName(), is(PARRILLA));
    assertThat("la categoria sugiere la del selector", form.getCategory(), is("RESTAURANT"));
    assertThat(form.getAddress(), is("Av. Corrientes 1234"));
    assertThat(form.getLatitude(), is("-34.6037"));
    assertThat(form.getLongitude(), is("-58.3816"));
    assertThat(form.getOpeningHours(), is("Mo-Su 12:00-23:00"));
    assertThat(form.getPhone(), is("+54 11 4444-5555"));
    assertThat(form.getWebsite(), is("https://parrilla.example.com"));
  }

  @Test
  void shouldFilterThePoisByName() {
    signInAsAdmin();
    stubPois(200, FIXTURE);

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForLoadButton();
    view.clickLoad();
    view.waitForPois(3);

    view.filterByName("PARRILLA");
    view.waitForPois(1);
    assertThat("el filtro no distingue mayusculas", view.poiCount(), is(1));
    assertThat("el mapa muestra solo el punto filtrado", view.markerCount(), is(1));
    assertThat(view.statusText(), is("1 de 3 punto(s)"));

    view.filterByName("cafe");
    view.waitForPois(1);
    assertThat("el filtro no distingue acentos", view.poiCount(), is(1));
    assertThat("el cafe no tiene coordenadas", view.markerCount(), is(0));

    view.filterByName("zzz");
    view.waitForPois(0);
    assertThat(view.markerCount(), is(0));

    view.filterByName("");
    view.waitForPois(3);
    assertThat("al limpiar el filtro vuelven todos", view.markerCount(), is(2));
  }

  /** La consulta viaja con el area visible y se repite solo cuando el usuario vuelve a pedirla. */
  @Test
  void shouldQueryTheAreaOnScreenAndNoticeWhenTheMapMoves() {
    signInAsAdmin();
    List<String> requests = new ArrayList<>();
    stubPois(200, FIXTURE, requests);

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForLoadButton();
    view.clickLoad();
    view.waitForPois(3);

    String first = requests.get(0);
    assertThat("la consulta lleva el area visible", hasAllBorders(first), is(true));
    double citySpan = latitudeSpan(first);

    view.zoomTo(17);
    assertThat("acercarse no invalida la lista ya cargada", view.hasStaleNotice(), is(false));

    view.clickLoad();
    page.waitForCondition(() -> requests.size() == 2);
    assertThat(
      "zoom 17 consulta una zona mas chica que la vista inicial",
      latitudeSpan(requests.get(1)),
      lessThan(citySpan)
    );

    /* Con la zona chica ya cargada, dos arrastres salen del area consultada. */
    view.dragMap(600);
    view.dragMap(600);
    view.waitForStaleNotice();
    assertThat("la lista no se vacia al mover el mapa", view.poiCount(), is(3));

    view.clickLoad();
    page.waitForCondition(() -> requests.size() == 3);
    view.waitForStaleNoticeGone();
    assertThat("al recargar la zona nueva se apaga el aviso", view.hasStaleNotice(), is(false));
  }

  @Test
  void shouldShowTheErrorMessageWhenTheProxyFails() {
    signInAsAdmin();
    stubPois(502, "{\"error\":\"Overpass is not available right now\"}");

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForLoadButton();
    view.clickLoad();
    view.waitForError();

    assertThat(view.hasError(), is(true));
    assertThat(
      view.errorText(),
      containsString("No se pudieron traer los puntos de OpenStreetMap")
    );
    assertThat("no queda ningun POI en la lista", view.poiCount(), is(0));
  }

  private boolean hasAllBorders(String url) {
    return Stream
      .of("south", "west", "north", "east")
      .allMatch(name -> urlParam(url, name) != null && !urlParam(url, name).isBlank());
  }

  private double latitudeSpan(String url) {
    return Math.abs(
      Double.parseDouble(urlParam(url, "north")) - Double.parseDouble(urlParam(url, "south"))
    );
  }

  /** Valor crudo de un query param: los numeros no necesitan decodificacion. */
  private String urlParam(String url, String name) {
    for (String pair : url.substring(url.indexOf('?') + 1).split("&")) {
      int separator = pair.indexOf('=');
      if (separator > 0 && pair.substring(0, separator).equals(name)) {
        return pair.substring(separator + 1);
      }
    }
    return null;
  }

  /** Intercepta el proxy en el navegador: el backend no participa y Overpass no se golpea. */
  private void stubPois(int status, String body) {
    stubPois(status, body, new ArrayList<>());
  }

  /** Igual que {@link #stubPois}, guardando cada URL pedida: el test verifica si hubo consulta. */
  private void stubPois(int status, String body, List<String> requested) {
    page.route(
      "**/admin/places/import/pois**",
      route -> {
        requested.add(route.request().url());
        route.fulfill(
          new Route.FulfillOptions()
            .setStatus(status)
            .setContentType("application/json; charset=utf-8")
            .setBody(body)
        );
      }
    );
  }
}
