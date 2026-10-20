package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Route;
import com.valhalla.e2e.views.PlaceFormPage;
import com.valhalla.e2e.views.PlaceImportPage;
import org.junit.jupiter.api.Test;

/**
 * E-07..E-08: el importador visto desde el navegador. El proxy de Overpass se stubbea con {@code
 * page.route}, asi que la vista real corre en Chromium pero el navegador nunca sale a internet
 * (AC-10). El stub tambien es el unico modo de ejercitar el error sin depender de que Overpass este
 * saturado justo en la corrida.
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
        "name": "Sin Coordenadas",
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
  void shouldListTheStubbedPoisAndOpenThePrefilledForm() {
    signInAsAdmin();
    stubPois(200, FIXTURE);

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
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
  void shouldShowTheErrorMessageWhenTheProxyFails() {
    signInAsAdmin();
    stubPois(502, "{\"error\":\"Overpass is not available right now\"}");

    PlaceImportPage view = new PlaceImportPage(page);
    view.navigateToImport();
    view.waitForError();

    assertThat(view.hasError(), is(true));
    assertThat(
      view.errorText(),
      containsString("No se pudieron traer los puntos de OpenStreetMap")
    );
    assertThat("no queda ningun POI en la lista", view.poiCount(), is(0));
  }

  /** Intercepta el proxy en el navegador: el backend no participa y Overpass no se golpea. */
  private void stubPois(int status, String body) {
    page.route(
      "**/admin/places/import/pois**",
      route ->
        route.fulfill(
          new Route.FulfillOptions()
            .setStatus(status)
            .setContentType("application/json; charset=utf-8")
            .setBody(body)
        )
    );
  }
}
