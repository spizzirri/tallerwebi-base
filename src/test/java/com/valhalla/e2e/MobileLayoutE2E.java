package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.WebPage;
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
  void T_MOB_004_exploradorFiltrosArribaMapaAbajoEnPhone() {
    signInAsAdmin();
    page.setViewportSize(PHONE_W, PHONE_H);
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/explore");
    page.waitForSelector("#map");
    awaitStyles();

    // AC-01/AC-06: la pagina no se desborda, los filtros viven arriba y el mapa abajo
    assertNoSideScroll("/explore");
    Locator map = page.locator("#map");
    assertThat(
      "el mapa tiene alto visible debajo de los filtros",
      map.boundingBox().height,
      is(greaterThan(150.0))
    );
    double sidebarWidth = page.locator("#app > aside").first().boundingBox().width;
    assertThat(
      "el panel de filtros no puede exceder la pantalla",
      sidebarWidth,
      is(greaterThan(0.0))
    );
    assertThat("el panel de filtros entra en la pantalla", sidebarWidth <= PHONE_W, is(true));

    // AC-07: en desktop vuelve al layout de dos paneles (sidebar de 320px)
    page.setViewportSize(DESKTOP_W, DESKTOP_H);
    double desktopSidebar = page.locator("#app > aside").first().boundingBox().width;
    assertThat("sidebar de escritorio w-80", Math.round(desktopSidebar), is(320L));
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
