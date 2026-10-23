package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.PlaceFormPage;
import com.valhalla.e2e.views.PlacesAdminPage;
import com.valhalla.e2e.views.WebPage;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * E-04..E-06: el ABM de places visto desde el navegador. Alta, edicion y baja corren en un solo
 * flujo a proposito: el dataset sembrado lo comparten todas las clases E2E y {@code ResetDatabase}
 * ya no borra places, asi que esta suite tiene que dejarlo como lo encontro. La baja (E-06) es la
 * limpieza; si un paso anterior falla, la fila queda con un nombre unico y ninguna otra corrida
 * choca contra ella.
 */
public class PlaceViewABME2E extends E2eBase {

  private static final String PLACE_NAME =
    "E2E Place " + UUID.randomUUID().toString().substring(0, 8);
  private static final String EDITED_NAME = PLACE_NAME + " edited";
  private static final String EDITED_CATEGORY = "MUSEUM";

  @Test
  void shouldCreateEditAndDeleteAPlaceWithoutDisturbingTheSeededDataset() {
    signInAsAdmin();
    int seededPlaces = openExploreAndCountPlaces();

    PlacesAdminPage places = new PlacesAdminPage(page);
    places.navigateToPlaces();

    // E-04: alta desde /admin/places/new -> aparece en el listado y en /explore.
    places.clickNewPlace();
    assertThat(placeForm().getHeading(), is("New Place"));
    placeForm().typeName(PLACE_NAME);
    placeForm().selectCategory("RESTAURANT");
    placeForm().typeLatitude("-34.60");
    placeForm().typeLongitude("-58.38");
    // Los tres datos opcionales del importador (la web, con el esquema que exige el form).
    placeForm().typeOpeningHours("Mo-Su 12:00-23:00");
    placeForm().typePhone("+54 11 4444-5555");
    placeForm().typeWebsite("https://parrilla.example.com");
    placeForm().clickSave();
    places.waitForPath("/admin/places");
    assertThat(places.hasRowWithName(PLACE_NAME), is(true));
    thenExploreShows(PLACE_NAME, "RESTAURANT", seededPlaces + 1);
    thenExploreDetailsShow(PLACE_NAME);

    // E-05: edicion del nombre y la categoria -> los cambios se ven en las dos vistas.
    places.navigateToPlaces();
    places.clickEditOnRow(PLACE_NAME);
    assertThat(placeForm().getHeading(), is("Edit Place"));
    placeForm().typeName(EDITED_NAME);
    placeForm().selectCategory(EDITED_CATEGORY);
    placeForm().clickSave();
    places.waitForPath("/admin/places");
    assertThat(places.hasRowWithName(EDITED_NAME), is(true));
    thenExploreShows(EDITED_NAME, EDITED_CATEGORY, seededPlaces + 1);
    // El update no pierde los campos opcionales: la tarjeta del sidebar los sigue mostrando.
    thenExploreDetailsShow(EDITED_NAME);

    // E-06: baja desde el ABM -> desaparece de las dos vistas y el dataset vuelve a su tamano.
    places.navigateToPlaces();
    places.clickDeleteOnRow(EDITED_NAME);
    places.waitForPath("/admin/places");
    assertThat(places.hasRowWithName(EDITED_NAME), is(false));
    thenExploreHides(EDITED_NAME, seededPlaces);
  }

  private PlaceFormPage placeForm() {
    return new PlaceFormPage(page);
  }

  /**
   * Abre /explore y devuelve el tamano del dataset sembrado, ya con cards y markers sincronizados:
   * es la linea base contra la que se mide el alta y la baja.
   */
  private int openExploreAndCountPlaces() {
    navigateToExplore();
    page.waitForFunction(
      "() => { const cards = document.querySelectorAll('#explore-app > aside article').length; " +
      "return cards > 0 && cards === document.querySelectorAll('#map .leaflet-marker-icon').length; }"
    );
    return placeCards().count();
  }

  private void thenExploreShows(String name, String category, int expectedCount) {
    openExploreAndWaitForCount(expectedCount);
    Locator card = placeCards().filter(new Locator.FilterOptions().setHasText(name));
    assertThat("the place has one card on /explore", card.count(), is(1));
    assertThat(
      "the card carries the place category",
      card.first().textContent(),
      containsString(category)
    );
  }

  /**
   * Abre el sidebar del place en /explore (click en la card, el mismo gesto del usuario) y verifica
   * los tres datos opcionales: horarios, telefono y web. La web se guardo sin esquema y se espera
   * abierta como https; el telefono, como tel: sin espacios.
   */
  private void thenExploreDetailsShow(String name) {
    placeCards().filter(new Locator.FilterOptions().setHasText(name)).first().click();
    Locator details = page.locator("#place-details-aside");
    details.waitFor();
    assertThat(details.textContent(), containsString("Mo-Su 12:00-23:00"));
    assertThat(details.textContent(), containsString("+54 11 4444-5555"));
    assertThat(
      "el telefono se puede marcar",
      details.locator("a[href='tel:+541144445555']").count(),
      is(1)
    );
    assertThat(
      "la web sin esquema se abre como https",
      details.locator("a[href='https://parrilla.example.com']").count(),
      is(1)
    );
  }

  private void thenExploreHides(String name, int expectedCount) {
    openExploreAndWaitForCount(expectedCount);
    assertThat(
      "the deleted place has no card on /explore",
      placeCards().filter(new Locator.FilterOptions().setHasText(name)).count(),
      is(0)
    );
  }

  /** Abre /explore y espera a que cards y markers lleguen juntos al total esperado. */
  private void openExploreAndWaitForCount(int expectedCount) {
    navigateToExplore();
    page.waitForFunction(
      "() => document.querySelectorAll('#explore-app > aside article').length === " +
      expectedCount +
      " && document.querySelectorAll('#map .leaflet-marker-icon').length === " +
      expectedCount
    );
  }

  /**
   * COMMIT, no LOAD: /explore carga Leaflet desde un CDN y el mapa se arma con fetch('/api/places')
   * despues del evento load. PlacesViewE2E espera igual, y aca se replica para no depender de la red
   * mas de lo que ya depende la vista.
   */
  private void navigateToExplore() {
    page.navigate(
      new WebPage(page).baseUrl() + "/explore",
      new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT)
    );
    page.waitForSelector(
      "#map.leaflet-container",
      new Page.WaitForSelectorOptions().setTimeout(10000)
    );
  }

  private Locator placeCards() {
    return page.locator("#explore-app > aside article");
  }
}
