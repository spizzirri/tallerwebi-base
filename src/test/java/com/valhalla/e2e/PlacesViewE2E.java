package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.WebPage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Browser coverage of the Places map, filters, selected-place panel, and scroll behavior. */
public class PlacesViewE2E extends E2eBase {

  @BeforeEach
  void signInAndOpenPlaces() {
    signInAsAdmin();
    page.navigate(
      placesUrl(),
      new com.microsoft.playwright.Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT)
    );
    page.waitForSelector(
      "#map.leaflet-container",
      new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000)
    );
    waitForPlaceCount(10);
  }

  /**
   * AC-13: el navbar se decide por área, no por rol. El arrange de esta clase es justo el caso
   * reportado —una sesión ADMIN parada en /explore— y tiene que ver el menú de usuario (sin
   * "Users"); el conjunto se pinnea entero para que un item fantasma también falle.
   */
  @Test
  void shouldShowTheUserNavbarToAnAdminBrowsingTheExplore() {
    assertThat(
      new WebPage(page).getNavbarItems(),
      contains("PlanIt", "Planes públicos", "Explore", "Plans", "Logout")
    );
  }

  @Test
  void shouldRenderSeededPlacesAndKeepMarkersInSyncWithFilters() {
    assertThat(page.locator("#map .leaflet-tile").count(), is(greaterThan(0)));
    assertThat(markers().count(), is(equalTo(10)));
    assertThat(placeCards().count(), is(equalTo(10)));
    assertThat(
      page.locator("#map .leaflet-marker-icon div[style*='background-color:#e74c3c']").count(),
      is(equalTo(2))
    );

    page.locator("#app > aside select").selectOption("RESTAURANT");
    waitForPlaceCount(2);
    assertThat(
      placeCards().allTextContents().stream().allMatch(cardText -> cardText.contains("RESTAURANT")),
      is(true)
    );

    page.locator("#app > aside select").selectOption("");
    page.locator("#app > aside input[type='text']").fill("Don");
    waitForPlaceCount(1);
    assertThat(placeCards().first().textContent(), containsString("Don"));

    page
      .locator("#app > aside button")
      .filter(new Locator.FilterOptions().setHasText("Clear Filters"))
      .click();
    waitForPlaceCount(10);
  }

  @Test
  void shouldShowDetailsFromMarkerWithoutLeavingPlacesPage() {
    markers().first().click();

    Locator panel = detailsPanel();
    panel.waitFor();
    page.locator(".leaflet-popup").waitFor();

    assertThat(panel.locator("h2").textContent(), containsString("El Sanjuanino"));
    assertThat(page.locator(".leaflet-popup").textContent(), containsString("RESTAURANT"));
    // Leaflet's own close button is an <a>; only the content must stay link-free.
    assertThat(page.locator(".leaflet-popup-content a").count(), is(equalTo(0)));
    assertThat(page.url(), equalTo(placesUrl()));
  }

  @Test
  void shouldCenterSelectedCardAndShowClosablePlaceDetails() {
    clickCard("MALBA");
    waitForSelectedPlace("MALBA");

    clickCard("Parrilla Don Julio");
    waitForSelectedPlace("Parrilla Don Julio");
    waitForMarkerCentered(placeCardIndex("Parrilla Don Julio"));

    Locator panel = detailsPanel();
    assertThat(panel.locator("h2").textContent(), equalTo("Parrilla Don Julio"));
    assertThat(panel.innerText(), containsString("Restaurant"));
    assertThat(panel.innerText(), containsString("Guatemala 4699"));
    assertThat(panel.innerText(), containsString("Parrilla galardonada a nivel internacional"));
    assertThat(panel.locator("img").getAttribute("src"), equalTo("/images/place-placeholder.svg"));
    page.waitForFunction(
      "() => { const image = document.querySelector(\"aside[aria-label^='Details for '] img\"); " +
      "return image !== null && image.complete && image.naturalWidth > 0; }"
    );
    assertThat(
      (String) panel.evaluate("element => getComputedStyle(element).borderLeftColor"),
      equalTo("rgb(231, 76, 60)")
    );
    // El icono de Lucide dibuja con stroke="currentColor", así que el atributo ya no
    // lleva el hex: lo que tiene que resolver al color de categoría es el valor computado,
    // que es además lo que el usuario ve. El atributo solo probaría una cadena.
    assertThat(
      (String) panel
        .locator(".flex.items-center svg")
        .first()
        .evaluate("element => getComputedStyle(element).stroke"),
      equalTo("rgb(231, 76, 60)")
    );

    panel.locator("button").filter(new Locator.FilterOptions().setHasText("Add to plan")).click();
    assertThat(detailsPanel().count(), is(equalTo(1)));
    assertThat(page.url(), equalTo(placesUrl()));
    // El modal "Add to plan" recién se abre cuando resuelve el fetch('/api/plans') que le
    // alimenta el select, así que hay que esperarlo y cerrarlo: montado sobre la página
    // (backdrop z-2000) bloquearía cada click siguiente con un intercept de puntero.
    Locator addToPlanDialog = page.locator("div[role='dialog']");
    addToPlanDialog.waitFor();
    addToPlanDialog.locator("button:has-text('Cancelar')").click();
    page.waitForFunction("() => document.querySelector(\"div[role='dialog']\") === null");

    panel.locator("button[aria-label='Close place details']").click();
    page.waitForFunction(
      "() => document.querySelector(\"aside[aria-label^='Details for ']\") === null"
    );
    // Leaflet fades the popup out and only removes it from the DOM 200 ms later.
    page.waitForFunction("() => document.querySelector('.leaflet-popup') === null");
    assertThat(page.locator(".leaflet-popup").count(), is(equalTo(0)));

    cardNamed("MALBA").locator("span:text-is('View details →')").click();
    waitForSelectedPlace("MALBA");
    assertThat(page.url(), equalTo(placesUrl()));
  }

  @Test
  void shouldKeepPageFixedWhilePlacesSidebarScrolls() {
    Locator sidebar = page.locator("#app > aside");
    assertThat(
      (Boolean) sidebar.evaluate("element => element.scrollHeight > element.clientHeight"),
      is(true)
    );
    assertThat(
      (Boolean) page.evaluate("() => document.documentElement.scrollHeight <= window.innerHeight"),
      is(true)
    );

    sidebar.hover();
    page.mouse().wheel(0, 500);
    page.waitForFunction("() => document.querySelector('#app > aside').scrollTop > 0");

    assertThat(
      (Boolean) page.evaluate(
        "() => document.documentElement.scrollTop === 0 && document.body.scrollTop === 0"
      ),
      is(true)
    );
    assertThat(
      (Boolean) page.evaluate(
        "() => document.querySelector('body > nav').getBoundingClientRect().top === 0"
      ),
      is(true)
    );
  }

  private void clickCard(String placeName) {
    cardNamed(placeName).click();
  }

  private Locator cardNamed(String placeName) {
    return placeCards().filter(new Locator.FilterOptions().setHasText(placeName));
  }

  private Locator detailsPanel() {
    return page.locator("aside[aria-label^='Details for ']");
  }

  private int placeCardIndex(String placeName) {
    List<String> cardTexts = placeCards().allTextContents();
    for (int index = 0; index < cardTexts.size(); index++) {
      if (cardTexts.get(index).contains(placeName)) return index;
    }
    throw new AssertionError("No place card found for " + placeName);
  }

  private Locator markers() {
    return page.locator("#map .leaflet-marker-icon");
  }

  private String placesUrl() {
    return new WebPage(page).baseUrl() + "/explore";
  }

  private Locator placeCards() {
    return page.locator("#app > aside article");
  }

  private void waitForMarkerCentered(int markerIndex) {
    page.waitForFunction(
      "() => { const map = document.querySelector('#map'); " +
      "const marker = map.querySelectorAll('.leaflet-marker-icon')[" +
      markerIndex +
      "]; " +
      "if (!marker) return false; " +
      "const mapBounds = map.getBoundingClientRect(); " +
      "const markerBounds = marker.getBoundingClientRect(); " +
      "const mapCenterX = mapBounds.left + mapBounds.width / 2; " +
      "const mapCenterY = mapBounds.top + mapBounds.height / 2; " +
      "const markerCenterX = markerBounds.left + markerBounds.width / 2; " +
      "const markerCenterY = markerBounds.top + markerBounds.height / 2; " +
      "return Math.abs(markerCenterX - mapCenterX) <= 2 && Math.abs(markerCenterY - mapCenterY) <= 2; }"
    );
  }

  private void waitForPlaceCount(int expectedCount) {
    page.waitForFunction(
      "() => document.querySelectorAll('#app > aside article').length === " +
      expectedCount +
      " && document.querySelectorAll('#map .leaflet-marker-icon').length === " +
      expectedCount
    );
  }

  private void waitForSelectedPlace(String placeName) {
    page.waitForFunction(
      "() => document.querySelector(\"aside[aria-label^='Details for '] h2\")?.textContent === '" +
      placeName +
      "'"
    );
  }
}
