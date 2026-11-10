package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.WebPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * [PSHARE] La ficha publica de un lugar en el navegador: T-PSHARE-003 (el boton de compartir
 * copia la URL absoluta de la ficha) y T-PSHARE-004 (el popup del mapa y el panel lateral de
 * /explore linkean a /places/{id}).
 *
 * <p>Anonimo a proposito: la ficha es el destino de un link compartido, asi que llega sin sesion,
 * igual que la ve quien abre el mensaje de WhatsApp.
 */
public class PlaceShareE2E extends E2eBase {

  private static final String SHEET_PATH = "/places/";
  /**
   * Lugar fijado por el seed: el primer marcador de /explore es "El Sanjuanino" (lo pinnea
   * PlacesViewE2E), con direccion y coordenadas, asi que el popup siempre tiene contenido.
   */
  private static final String FIRST_PLACE_NAME = "El Sanjuanino";

  private String baseUrl;

  @BeforeEach
  void openExploreAndFindTheSeedPlace() {
    baseUrl = new WebPage(page).baseUrl();
    page.navigate(
      baseUrl + "/explore",
      new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT)
    );
    page.waitForSelector("#map.leaflet-container");
    page.waitForFunction(
      "() => document.querySelectorAll('#explore-app > aside article').length > 0" +
      " && document.querySelectorAll('#map .leaflet-marker-icon div[data-category]').length > 0"
    );
  }

  // --- T-PSHARE-003 ---

  @Test
  void shareButtonCopiesTheAbsolutePlaceUrl() {
    String placeId = placeIdNamed(FIRST_PLACE_NAME);
    page.navigate(baseUrl + SHEET_PATH + placeId);
    page.waitForSelector("[data-share-plan]");

    // Determinista: sin navigator.share el boton cae al portapapeles, y el writeText se
    // registra en la pagina en vez de depender del permiso de clipboard del navegador (que
    // Playwright otorga por contexto, no por origen). El try/catch cubre ambos bindings:
    // operation en el prototipo (data property) u accessor, redefinible en la instancia.
    page.evaluate(
      """
      () => {
        try {
          Object.defineProperty(Navigator.prototype, 'share', { value: undefined, configurable: true });
        } catch (error) {
          Object.defineProperty(navigator, 'share', { value: undefined, configurable: true });
        }
        window.__copiedUrl = null;
        navigator.clipboard.writeText = (text) => {
          window.__copiedUrl = text;
          return Promise.resolve();
        };
      }
      """
    );

    page.click("[data-share-plan]");
    page.waitForFunction("() => window.__copiedUrl !== null");
    String copied = (String) page.evaluate("() => window.__copiedUrl");

    assertThat(copied, is(equalTo(baseUrl + SHEET_PATH + placeId)));
    // El feedback visual del boton: el texto cambia a "Link copiado" tras copiar.
    assertThat(page.locator("[data-share-plan]").textContent(), is(equalTo("Link copiado")));
  }

  // --- T-PSHARE-004 ---

  @Test
  void popupAndSidePanelLinkToThePlaceSheet() {
    String placeId = placeIdNamed(FIRST_PLACE_NAME);

    // El popup del marcador.
    page.locator("#map .leaflet-marker-icon div[data-category]").first().click();
    Locator popupLink = page.locator(".leaflet-popup-content a[href^='/places/']");
    popupLink.waitFor();
    assertThat(popupLink.count(), is(equalTo(1)));
    assertThat(popupLink.textContent(), is(equalTo("Ver ficha")));
    assertThat(popupLink.getAttribute("href"), is(equalTo(SHEET_PATH + placeId)));

    // El panel lateral, con el mismo gesto de usuario: click en la card.
    page
      .locator("#explore-app > aside article")
      .filter(new Locator.FilterOptions().setHasText(FIRST_PLACE_NAME))
      .first()
      .click();
    Locator panelLink = page.locator("#place-details-aside a[href^='/places/']");
    panelLink.waitFor();
    assertThat(panelLink.count(), is(equalTo(1)));
    assertThat(panelLink.textContent(), is(equalTo("Ver ficha")));
    assertThat(panelLink.getAttribute("href"), is(equalTo(SHEET_PATH + placeId)));
  }

  // --- helpers ---

  /** Id del lugar con ese nombre en /api/places: el seed genera los ids, no se pueden fijar. */
  private String placeIdNamed(String name) {
    Object id = page.evaluate(
      "name => fetch('/api/places')" +
      "  .then(response => response.json())" +
      "  .then(places => places.find(place => place.name === name).id)",
      name
    );
    return String.valueOf(id);
  }
}
