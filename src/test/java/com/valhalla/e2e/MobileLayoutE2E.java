package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.WebPage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * [MOBILE] T-MOB-002 a T-MOB-007: las vistas a 390x844 (iPhone 14) no se desbordan y la
 * navegacion sigue siendo completa; el desktop 1280x720 no regresa.
 *
 * <p>El patron es el de PlanPlaceDetailE2E: fijar el viewport, medir
 * {@code scrollWidth <= innerWidth} (AC-01), y volver a desktop para el resto de las
 * aserciones. Los arrange crean datos con la sesion admin y, cuando hace falta un observador
 * sin sesion, se abre un contexto nuevo (misma base, otra cookies).
 */
public class MobileLayoutE2E extends E2eBase {

  private static final int PHONE_W = 390;
  private static final int PHONE_H = 844;
  private static final int DESKTOP_W = 1280;
  private static final int DESKTOP_H = 720;

  private static final String PLAN_NAME = "Plan mobile E2E";

  /**
   * Tailwind compila en el browser (sin build step): hasta que inyecta el CSS las clases no tienen
   * efecto y medir ahora daria basura. Es el mismo umbral del reveal de layouts/base.
   */
  private void awaitStyles() {
    page.waitForFunction(
      "() => { const styles = document.querySelectorAll('head style');" +
      " for (const s of styles) { if (s.textContent.length > 2000) return true; }" +
      " return false; }"
    );
  }

  @Test
  void T_MOB_002_feedUnaColumnaEnPhoneYTresEnDesktop() {
    // arrange: un plan publico visible para cualquiera (el del admin no le llega al admin)
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, "Para el feed", "2026-12-31");
    newContext();

    // --- phone: sin scroll lateral y grilla en una columna (AC-01, AC-03) ---
    page.setViewportSize(PHONE_W, PHONE_H);
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/for-you");
    page.waitForSelector("main article");
    awaitStyles();
    waitForGridColumns(1);
    assertNoSideScroll("/for-you");
    assertThat("una sola columna en phone", gridColumns(), is(1));

    // --- desktop: la grilla recupera tres columnas (AC-07) ---
    page.setViewportSize(DESKTOP_W, DESKTOP_H);
    waitForGridColumns(3);
    assertThat("tres columnas en desktop", gridColumns(), is(3));
    assertNoSideScroll("/for-you desktop");
  }

  @Test
  void T_MOB_003_loginEnPhoneSinScrollYInputsLegibles() {
    page.setViewportSize(PHONE_W, PHONE_H);
    new LoginPage(page, "/auth/login");
    awaitStyles();

    assertNoSideScroll("/auth/login");
    // AC-05: input con font >= 16px, si no iOS hace zoom automatico al enfocar
    Object fontSize = page
      .locator("#email")
      .evaluate("el => parseFloat(getComputedStyle(el).fontSize)");
    assertThat(
      "el input de email mide 16px o mas",
      ((Number) fontSize).doubleValue(),
      is(greaterThan(15.0))
    );
  }

  @Test
  void T_MOB_004_exploradorMapaArribaYTabsPlacesPlans() {
    signInAsAdmin();
    page.setViewportSize(PHONE_W, PHONE_H);
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/explore");
    page.waitForSelector("#map");
    awaitStyles();

    // AC-01: la pagina no scrollea de lado con el explorador cargado
    assertNoSideScroll("/explore");
    Locator map = page.locator("#map");
    Locator placesSheet = page.locator("#app > aside").first();
    Locator plansSheet = page.locator("#plans-aside");
    Locator tabPlaces = page.locator("label[for='tab-places']");
    Locator tabPlans = page.locator("label[for='tab-plans']");

    // Estado inicial: el mapa ocupa toda el area (arriba de todo) y los dos paneles esperan
    // detras de su tab. invisible (visibility:hidden) los deja fuera de isVisible() pero con
    // caja medida, y pointer-events-none evita que escondidos tapen markers del mapa.
    assertThat("la tab Places esta en la barra", tabPlaces.isVisible(), is(true));
    assertThat("la tab Plans esta en la barra", tabPlans.isVisible(), is(true));
    assertThat("el panel Places esta oculto al cargar", placesSheet.isVisible(), is(false));
    assertThat("el panel Plans esta oculto al cargar", plansSheet.isVisible(), is(false));
    assertThat(
      "el mapa llena casi toda la altura de la pantalla",
      map.boundingBox().height,
      is(greaterThan(PHONE_H * 0.70))
    );

    // El estado de datos llega asincronico (Vue pega /api/places al montar): sin esperarlo la
    // captura sale con "0 place(s)" y el mapa sin tiles, que no es como se ve el explorador.
    page.waitForFunction(
      "() => document.querySelectorAll('#app > aside article').length > 0" +
      " && document.querySelectorAll('#map .leaflet-marker-icon').length > 0" +
      " && document.querySelectorAll('#map .leaflet-tile-loaded').length > 0",
      null,
      new com.microsoft.playwright.Page.WaitForFunctionOptions().setTimeout(15000)
    );
    page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/explore-mobile.png")));

    // Tab Places: despliega filtros + listado sentado sobre la barra de tabs (tope 40vh) y,
    // como son pestañas excluyentes, Plans sigue escondido.
    tabPlaces.click();
    assertThat("Places se despliega al elegir la tab", placesSheet.isVisible(), is(true));
    assertThat(
      "el panel Places respeta el tope de 40vh",
      placesSheet.boundingBox().height,
      is(lessThanOrEqualTo(PHONE_H * 0.40 + 1))
    );
    assertThat("Places y Plans son excluyentes", plansSheet.isVisible(), is(false));
    double placesBottom = placesSheet.boundingBox().y + placesSheet.boundingBox().height;
    assertThat(
      "el panel Places queda sentado sobre la barra de tabs (48px)",
      Math.abs(placesBottom - (PHONE_H - 48)),
      is(lessThan(6.0))
    );

    // La cruz del panel oculta el desplegable y devuelve el mapa a pantalla completa: apunta a
    // tab-none, un tercer radio del mismo grupo (un radio no se destilda con su propio label).
    page.locator("label[for='tab-none']:visible").click();
    assertThat("la cruz cierra Places", placesSheet.isVisible(), is(false));
    assertThat("y no abre el otro panel por accidente", plansSheet.isVisible(), is(false));

    // Tab Plans: intercambia el panel (Plans entra, Places sale) con su tope de 35vh.
    tabPlans.click();
    assertThat("al elegir Plans se cierra Places", placesSheet.isVisible(), is(false));
    assertThat("Plans se despliega", plansSheet.isVisible(), is(true));
    assertThat(
      "el panel Plans respeta el tope de 35vh",
      plansSheet.boundingBox().height,
      is(lessThanOrEqualTo(PHONE_H * 0.35 + 1))
    );
    page.locator("label[for='tab-none']:visible").click();
    assertThat("la cruz cierra Plans", plansSheet.isVisible(), is(false));
    assertThat("ningun panel queda colgando", placesSheet.isVisible(), is(false));

    // El detalle de un place es una card superpuesta centrada sobre el mapa (con margenes a los
    // costados), no un sheet edge-to-edge. Abro un place desde su card en el panel de la tab.
    tabPlaces.click();
    page.locator("#app > aside article").first().click();
    Locator details = page.locator("#place-details-aside");
    details.waitFor();
    assertThat("el detalle de place es visible", details.isVisible(), is(true));
    double cardLeft = details.boundingBox().x;
    double cardRight = PHONE_W - (details.boundingBox().x + details.boundingBox().width);
    assertThat(
      "la card deja margen a los costados (no pega los bordes)",
      cardLeft >= 10.0,
      is(true)
    );
    assertThat(
      "la card esta centrada horizontalmente (margenes iguales)",
      Math.abs(cardLeft - cardRight) <= 4.0,
      is(true)
    );
    assertThat(
      "la card tiene la altura acotada (scroll interno, no tapa toda la pantalla)",
      details.boundingBox().height <= PHONE_H * 0.60 + 1,
      is(true)
    );
    page.screenshot(
      new Page.ScreenshotOptions().setPath(Path.of("target/explore-detail-card.png"))
    );

    // Desktop: sin tabs, los paneles vuelven al layout de dos columnas aunque los radios
    // conserven la ultima tab elegida (md:visible/md:* mandan sobre group-has).
    page.setViewportSize(DESKTOP_W, DESKTOP_H);
    assertThat("sidebar de escritorio w-80", Math.round(placesSheet.boundingBox().width), is(320L));
    assertThat(
      "en desktop el sidebar de lugares esta visible sin tocar ninguna tab",
      placesSheet.isVisible(),
      is(true)
    );
    assertThat(
      "en desktop el panel de planes vuelve a overlay lateral de altura completa",
      plansSheet.boundingBox().height > PHONE_H * 0.5,
      is(true)
    );
  }

  @Test
  void T_MOB_005_adminTablasConScrollPropioEnPhone() {
    signInAsAdmin();
    page.setViewportSize(PHONE_W, PHONE_H);
    WebPage web = new WebPage(page);

    for (String path : new String[] { "/admin/users", "/admin/places" }) {
      web.navigate(web.baseUrl() + path);
      page.waitForSelector("main table");
      awaitStyles();
      assertNoSideScroll(path);
      Object overflow = page
        .locator("main table")
        .evaluate("el => getComputedStyle(el.parentElement).overflowX");
      assertThat(
        path + ": la tabla vive en un contenedor con scroll propio",
        "auto".equals(overflow),
        is(true)
      );
    }
  }

  @Test
  void T_MOB_006_hamburguesaEnPhoneYNavbarDeDesktopIntacto() {
    // sin sesion: el navbar anonimo nunca ofrece hamburguesa (decision fijada en PPV)
    page.setViewportSize(PHONE_W, PHONE_H);
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/");
    awaitStyles();
    assertThat("anonimo no tiene hamburguesa", page.locator("#navbar-menu-btn").count(), is(0));
    // [MOBILE] El toggle de tema es hijo directo del nav con order-last: en mobile comparte
    // fila con el logo en vez de quedar huerfano en una tercera fila debajo de los links.
    Locator anonTheme = page.locator("nav > button[data-theme-toggle]");
    assertThat("el toggle de tema esta en el navbar anonimo", anonTheme.isVisible(), is(true));
    double rowDelta = Math.abs(
      anonTheme.boundingBox().y - page.locator(".navbar-brand").first().boundingBox().y
    );
    assertThat(
      "el toggle comparte fila con el logo (<40px de diferencia)",
      rowDelta,
      is(lessThan(40.0))
    );
    page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/navbar-anon-mobile.png")));

    // con sesion: la hamburguesa esta, mide 44px y abre el menu con los destinos del nav
    signInAsAdmin();
    web.navigate(web.baseUrl() + "/for-you");
    awaitStyles();
    Locator burger = page.locator("#navbar-menu-btn");
    assertThat("hamburguesa visible en phone", burger.isVisible(), is(true));
    assertThat(
      "target tactil >= 44px (AC-02)",
      burger.boundingBox().height,
      is(greaterThanOrEqualTo(44.0))
    );
    burger.click();
    page.waitForSelector("#navbar-menu:not([hidden])");
    assertThat(
      "el menu replica los destinos del nav",
      page.locator("#navbar-menu a:has-text('Explore')").first().isVisible(),
      is(true)
    );
    assertNoSideScroll("/for-you con menu abierto");

    // desktop: la barra vuelve a ser la de siempre, sin hamburguesa visible (AC-07)
    page.setViewportSize(DESKTOP_W, DESKTOP_H);
    assertThat("hamburguesa oculta en desktop", burger.isVisible(), is(false));
    assertThat(
      "items del navbar de escritorio",
      web.getNavbarItems(),
      contains("PlanIt", "Para vos", "Explore", "Plans", "A", "Cambiar tema")
    );
  }

  /**
   * [MOBILE] T-MOB-008: el detalle del plan a 320px, el ancho de los modelos viejos. Con los
   * paddings encadenados (container px-5 + #plan-app + panel) la parada quedaba en ~156px y las
   * acciones le robaban ~70: el titulo, con overflow-wrap: anywhere, se partia letra por letra
   * sin desbordar (por eso el fitsOnPhone existente no lo veia). El ancho del titulo es la
   * asercion; target/detail-mobile-320.png es la prueba visual.
   */
  @Test
  void T_MOB_008_detalleDelPlanLegibleA320() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.navigateToCreatePlanModal();
    plans.typeName("Detalle chico 320");
    plans.typeDescription("Para ver el itinerario en una pantalla angosta");
    plans.typeEventDate("2026-12-05");
    plans.clickNextStep();
    plans.addFirstDraftPlace();
    plans.clickNextStep();
    plans.clickCreate();
    plans.waitForDetailPath();

    page.setViewportSize(320, 752);
    page.waitForSelector(".stop-title");
    awaitStyles();
    assertNoSideScroll("/plans/{id} a 320px");

    double titleWidth = page.locator(".stop-title").first().boundingBox().width;
    assertThat(
      "el titulo de la parada no se parte letra por letra (>= 60px)",
      titleWidth,
      is(greaterThan(60.0))
    );

    page.locator(".itinerary-panel").scrollIntoViewIfNeeded();
    page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/detail-mobile-320.png")));
  }

  private void assertNoSideScroll(String where) {
    Object ok = page.evaluate(
      "() => document.documentElement.scrollWidth <= window.innerWidth + 1"
    );
    assertThat(where + ": la pagina no debe scrollear de lado", Boolean.TRUE.equals(ok), is(true));
  }

  private void waitForGridColumns(int expected) {
    page.waitForFunction(
      "n => { const g = document.querySelector('main .grid'); if (!g) return false;" +
      " return getComputedStyle(g).gridTemplateColumns.split(' ').length === n; }",
      expected
    );
  }

  private int gridColumns() {
    Object columns = page.evaluate(
      "() => { const g = document.querySelector('main .grid');" +
      " return g ? getComputedStyle(g).gridTemplateColumns.split(' ').length : -1; }"
    );
    return ((Number) columns).intValue();
  }
}
