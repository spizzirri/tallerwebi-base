package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.WebPage;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Browser coverage of the Places map, filters, selected-place panel, and scroll behavior. */
public class PlacesViewE2E extends E2eBase {

  /** Seed dataset size (AC-07). Pinned on purpose: a place added or removed must fail here. */
  private static final int TOTAL_PLACES = 12;

  private static final ScheduledExecutorService DELAYER =
    Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "e2e-route-delay");
      thread.setDaemon(true);
      return thread;
    });

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
    waitForPlaceCount(TOTAL_PLACES);
  }

  /**
   * AC-13: el navbar se decide por área, no por rol. El arrange de esta clase es justo el caso
   * reportado —una sesión ADMIN parada en /explore— y tiene que ver el menú de usuario (sin
   * "Users"); el conjunto se pinnea entero para que un item fantasma también falle. El último
   * item es el toggle de modo oscuro: un botón de solo ícono cuyo único texto es el sr-only.
   */
  @Test
  void shouldShowTheUserNavbarToAnAdminBrowsingTheExplore() {
    assertThat(
      new WebPage(page).getNavbarItems(),
      contains("PlanIt", "Para vos", "Explore", "Plans", "A")
    );
  }

  @Test
  void shouldRenderSeededPlacesAndKeepMarkersInSyncWithFilters() {
    assertThat(page.locator("#map .leaflet-tile").count(), is(greaterThan(0)));
    assertThat(markers().count(), is(equalTo(TOTAL_PLACES)));
    assertThat(placeCards().count(), is(equalTo(TOTAL_PLACES)));
    // AC-09: el marker se identifica por su categoria, no por el hex que la pinta.
    assertThat(
      page.locator("#map .leaflet-marker-icon div[data-category='RESTAURANT']").count(),
      is(equalTo(2))
    );

    page.locator("#explore-app > aside select").selectOption("RESTAURANT");
    waitForPlaceCount(2);
    assertThat(
      placeCards().allTextContents().stream().allMatch(cardText -> cardText.contains("RESTAURANT")),
      is(true)
    );

    page.locator("#explore-app > aside select").selectOption("");
    page.locator("#explore-app > aside input[type='text']").fill("Don");
    waitForPlaceCount(1);
    assertThat(placeCards().first().textContent(), containsString("Don"));

    page
      .locator("#explore-app > aside button")
      .filter(new Locator.FilterOptions().setHasText("Clear Filters"))
      .click();
    waitForPlaceCount(TOTAL_PLACES);
  }

  @Test
  void shouldShowDetailsFromMarkerWithoutLeavingPlacesPage() {
    markers().first().click();

    Locator panel = detailsPanel();
    panel.waitFor();
    page.locator(".leaflet-popup").waitFor();

    assertThat(panel.locator("h2").textContent(), containsString("El Sanjuanino"));
    assertThat(page.locator(".leaflet-popup").textContent(), containsString("RESTAURANT"));
    // [MAPS] El popup ganó el link "Cómo llegar": es el único contenido linkeable, y el
    // marcador clickeado siempre tiene dirección (la del lugar que se muestra en la ficha).
    Locator directions = page.locator(".leaflet-popup-content a");
    assertThat(directions.count(), is(equalTo(1)));
    assertThat(directions.textContent(), is(equalTo("Cómo llegar")));
    assertThat(
      directions.getAttribute("href"),
      // El destino es la dirección del lugar (url-encoded), no la coordenada.
      matchesPattern(".*google\\.com/maps/dir/\\?api=1&destination=([^&\\s]|%[0-9A-F]{2})+.*")
    );
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

    // [APL] Sin sesión no hay planes propios, así que el picker inline del panel no se
    // pinta y tampoco hay diálogo: la ficha sigue siendo solo informativa para anónimos.
    assertThat(
      panel.locator("button").filter(new Locator.FilterOptions().setHasText("Add to plan")).count(),
      is(equalTo(0))
    );
    assertThat(panel.locator("#plan-picker").count(), is(equalTo(0)));
    assertThat(page.locator("div[role='dialog']").count(), is(equalTo(0)));

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

  /**
   * [T12] Sin sesión el "+" "Create plan" del panel de planes manda al login conservando el
   * retorno: abrir el wizard para un anónimo solo lo mandaría a fallar en el submit (POST
   * /plans exige sesión).
   */
  @Test
  void createPlanButtonSendsAnonymousUsersToLogin() {
    newContext();
    page.navigate(placesUrl());
    // Sin sesión el "+" es un <a> al login, no un <button> que abre el wizard.
    String createPlan = "aside[aria-label='Plans'] a:has-text('Create plan')";
    page.waitForSelector(createPlan);

    page.locator(createPlan).click();

    page.waitForSelector("input[type='email']");
    assertThat(page.url(), containsString("/auth/login?redirect="));
  }

  @Test
  void shouldKeepPageFixedWhilePlacesSidebarScrolls() {
    Locator sidebar = page.locator("#explore-app > aside");
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
    page.waitForFunction("() => document.querySelector('#explore-app > aside').scrollTop > 0");

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

  /**
   * [FLASH] Regresion del primer paint de /explore. El template de Vue viaja en el HTML del
   * servidor: hasta que places-list.js monta, los v-if y los {{ }} son HTML inerte. Antes del fix
   * se veian el banner de error y el aside con "{{selectedPlace.name}}"
   * y el logo a su tamano intrínseco (1366 px).
   *
   * <p>Se retrasa el modulo que monta Vue 1,5 s (con un scheduler aparte: dormir dentro del
   * handler de la ruta bloquea el dispatcher y terminaria retrasando tambien el CSS) para que la
   * ventana entre el reveal y el mount sea ancha y no dependa de la velocidad de la maquina. Un
   * sampler en rAF anota, frame por frame, cualquier elemento visible que delate el bug; con el
   * codigo viejo falla con "root visible before Vue mounted" y los tres sintomas, con el fix el
   * root queda oculto por v-cloak y el logo mide 48 px desde el atributo width/height.
   */
  @Test
  void shouldNotPaintTheRawTemplateOrAnOversizedLogoOnFirstPaint() {
    // Contexto nuevo (cache vacia): el arrange de la clase ya cargo places-list.js, y desde
    // cache el modulo monta antes de que el CSS compile, escondiendo justo el orden que el bug
    // necesita. Un contexto limpio reproduce la primera visita.
    newContext();

    // El orden real es: el CSS compila antes de que el modulo que monta Vue termine de cargar.
    // El delay del modulo (1,5 s) hace determinista esa ventana: si el reveal del layout
    // levanta v-cloak, el template crudo queda a la vista cientos de milisegundos.
    page.route("**/js/tailwind-browser.js", route -> delay(route, 100));
    page.route("**/js/pages/places-list.js", route -> delay(route, 1500));

    page.addInitScript(
      """
      (function () {
        if (window.__firstPaint) return;
        var state = {
          frames: 0,
          violations: [],
          sawMountedRoot: false,
          revealedBeforeMount: false
        };
        window.__firstPaint = state;
        var visible = function (element) {
          if (!element) return false;
          var style = getComputedStyle(element);
          if (style.display === "none" || style.visibility === "hidden") return false;
          var box = element.getBoundingClientRect();
          return box.width > 0 && box.height > 0;
        };
        var flag = function (message) {
          if (state.violations.length < 5) state.violations.push(message);
        };
        var sample = function () {
          state.frames += 1;
          var root = document.getElementById("explore-app");
          var revealed = getComputedStyle(document.documentElement).visibility === "visible";
          if (root) {
            if (root.hasAttribute("data-v-app")) state.sawMountedRoot = true;
            else if (revealed) state.revealedBeforeMount = true;
            if (visible(root)) {
              if (!root.hasAttribute("data-v-app")) flag("root visible before Vue mounted");
              if (root.textContent.indexOf("{{") !== -1) flag("raw Vue template visible");
              if (visible(root.querySelector("[role='alert']"))) flag("error banner visible");
              if (visible(root.querySelector("[role='dialog']"))) flag("dialog visible");
            }
          }
          var logo = document.querySelector("nav img[alt='Logo']");
          if (visible(logo) && logo.getBoundingClientRect().width > 48.5) {
            flag("logo painted at " + Math.round(logo.getBoundingClientRect().width) + " px");
          }
          requestAnimationFrame(sample);
        };
        requestAnimationFrame(sample);
      })();
      """
    );

    page.navigate(placesUrl(), new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForSelector("#explore-app[data-v-app]");
    page.waitForFunction("() => !document.querySelector('#explore-app[v-cloak]')");

    @SuppressWarnings("unchecked")
    List<String> violations = (List<String>) page.evaluate("() => window.__firstPaint.violations");
    assertThat("first paint violations: " + violations, violations.isEmpty(), is(true));
    // Que no pase por vacio: el sampler tiene que haber visto frames y el mount.
    assertThat(
      "frames sampled",
      ((Number) page.evaluate("() => window.__firstPaint.frames")).intValue(),
      is(greaterThan(5))
    );
    assertThat(
      "sampler saw the mounted root",
      (Boolean) page.evaluate("() => window.__firstPaint.sawMountedRoot"),
      is(true)
    );
    // Anti-vacio: el sampler tiene que haber mirado la ventana donde el bug vive, es decir un
    // frame con el shield ya levantado y Vue todavia sin montar. Con el fix el root esta cloaked
    // en esa ventana; con el reveal viejo el root ya era visible y el flag de arriba dispara.
    assertThat(
      "sampler caught the reveal-before-mount window",
      (Boolean) page.evaluate("() => window.__firstPaint.revealedBeforeMount"),
      is(true)
    );
  }

  /**
   * Responde un request con retraso sin bloquear el dispatcher de Playwright: dormir dentro del
   * handler serializa todos los requests de la pagina, asi que el delay de un modulo tambien
   * retrasaba el script que compila el CSS y borraba la ventana que este test necesita mirar.
   */
  private static void delay(Route route, long millis) {
    DELAYER.schedule(
      () -> {
        try {
          route.resume();
        } catch (RuntimeException e) {
          // La pagina pudo cerrarse mientras el request estaba en cola: no hay nada que reanudar.
        }
      },
      millis,
      TimeUnit.MILLISECONDS
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
    // [ITI] El badge con data-category es un DIV HIJO del marcador (divIcon html), no el
    // root: este helper mide pines de lugares; los del borrador no llevan data-category.
    return page.locator("#map .leaflet-marker-icon div[data-category]");
  }

  private String placesUrl() {
    return new WebPage(page).baseUrl() + "/explore";
  }

  private Locator placeCards() {
    return page.locator("#explore-app > aside article");
  }

  private void waitForMarkerCentered(int markerIndex) {
    page.waitForFunction(
      "() => { const map = document.querySelector('#map'); " +
      "const marker = map.querySelectorAll('.leaflet-marker-icon div[data-category]')[" +
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
    // [ITI] data-category en el conteo de marcadores: los del borrador no se cuentan como
    // lugares (AC-14), así la igualdad exacta tarjeta↔marcador sobrevive al overlay.
    page.waitForFunction(
      "() => document.querySelectorAll('#explore-app > aside article').length === " +
      expectedCount +
      " && document.querySelectorAll('#map .leaflet-marker-icon div[data-category]').length === " +
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
