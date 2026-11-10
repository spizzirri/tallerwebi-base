package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import com.valhalla.e2e.views.WebPage;
import org.junit.jupiter.api.Test;

class CalendarE2E extends E2eBase {

  private static final String CALENDAR = "section[aria-label='Calendario mensual']";
  private static final String AGENDA = "section[aria-label='Planes del día seleccionado']";
  private static final String NAME =
    "Salida del calendario con un nombre largo para comprobar el ajuste";

  @Test
  void avatarMenuOpensCalendarAndNavigationSelectsDaysAndOpensPlans() {
    signInAsAdmin();
    String detailUrl = createPlan(NAME);
    page.navigate(baseUrl() + "/plans");
    page.locator("#navbar-avatar").click();
    page.locator("#menu-calendar").click();
    assertEquals("Mi calendario", page.title());
    page.navigate(baseUrl() + "/plans/calendar?month=2026-08");
    page.locator(CALENDAR + " a[href*='date=2026-08-31']").click();
    assertThat(page.locator(AGENDA + " a")).containsText(NAME);
    assertEquals(
      "2026-08-31",
      page.locator("a[aria-current='date'] time").getAttribute("datetime")
    );
    page.locator(AGENDA + " a").click();
    assertEquals(detailUrl, page.url());
    page.navigate(baseUrl() + "/plans/calendar?month=2026-12");
    page.locator("a[aria-label='Mes siguiente']").click();
    assertTrue(page.url().contains("month=2027-01"));
    page.locator("a[aria-label='Mes anterior']").click();
    assertTrue(page.url().contains("month=2026-12"));
    page.locator("nav[aria-label='Cambiar mes'] a").nth(1).click();
    assertEquals(baseUrl() + "/plans/calendar", page.url());
  }

  @Test
  void mobileAndDesktopFitTheViewportAndKeepDayControlsTouchable() {
    signInAsAdmin();
    createPlan(NAME);
    for (int[] viewport : new int[][] { { 320, 568 }, { 390, 844 }, { 1280, 720 } }) {
      page.setViewportSize(viewport[0], viewport[1]);
      page.navigate(baseUrl() + "/plans/calendar?month=2026-08&date=2026-08-31");
      awaitStyles();
      assertTrue(
        (Boolean) page.evaluate("() => document.documentElement.scrollWidth <= innerWidth")
      );
      assertTrue(
        (Boolean) page
          .locator(CALENDAR)
          .evaluate("el => el.getBoundingClientRect().bottom <= innerHeight")
      );
      Locator day = page.locator("a[aria-current='date']");
      assertTrue(day.boundingBox().height >= 44);
      assertTrue(day.boundingBox().width >= 40);
      assertTrue(
        (Boolean) page
          .locator(CALENDAR + " nav a")
          .evaluateAll(
            "links => links.every(el => { const r = el.getBoundingClientRect(); return r.width >= 44 && r.height >= 44; })"
          )
      );
      day.click();
      // El click recarga la misma URL: el escudo FOUC de base.html vuelve a ocultar la
      // página hasta que Tailwind compila; isVisible() no tiene retry y perdía la carrera.
      awaitStyles();
      assertThat(page.locator(AGENDA + " a")).containsText(NAME);
      assertTrue(page.locator(AGENDA + " a").isVisible());
      if (viewport[0] >= 768) {
        assertTrue(
          (Boolean) page
            .locator(AGENDA)
            .evaluate(
              "el => el.getBoundingClientRect().left >= document.querySelector('section[aria-label=\"Calendario mensual\"]').getBoundingClientRect().right"
            )
        );
        assertTrue(
          (Boolean) page.evaluate("() => document.documentElement.scrollHeight <= innerHeight")
        );
      } else {
        assertTrue(
          (Boolean) page
            .locator(AGENDA)
            .evaluate(
              "el => el.getBoundingClientRect().top >= document.querySelector('section[aria-label=\"Calendario mensual\"]').getBoundingClientRect().bottom"
            )
        );
      }
    }
  }

  @Test
  void manyPlansRemainReachableWithoutGrowingTheCalendar() {
    signInAsAdmin();
    for (int index = 0; index < 9; index++) {
      createPlan("Salida " + index);
    }
    page.setViewportSize(390, 844);
    page.navigate(baseUrl() + "/plans/calendar?month=2026-08&date=2026-08-31");
    awaitStyles();
    assertEquals(9, page.locator(AGENDA + " a").count());
    assertTrue(
      (Boolean) page
        .locator(AGENDA + " div")
        .first()
        .evaluate("el => el.scrollHeight > el.clientHeight")
    );
    page.locator(AGENDA + " a").last().scrollIntoViewIfNeeded();
    assertTrue(page.locator(AGENDA + " a").last().isVisible());
    assertTrue((Boolean) page.evaluate("() => document.documentElement.scrollWidth <= innerWidth"));
    page.evaluate("document.documentElement.setAttribute('data-theme', 'dark')");
    assertTrue(page.locator(AGENDA + " a").last().isVisible());
  }

  private String createPlan(String name) {
    page.navigate(baseUrl() + "/plans");
    String csrf = page.locator("input[name='_csrf']").first().getAttribute("value");
    var response = page
      .request()
      .post(
        baseUrl() + "/plans",
        RequestOptions
          .create()
          .setForm(
            FormData
              .create()
              .set("_csrf", csrf)
              .set("name", name)
              .set("eventDate", "2026-08-31")
              .set("eventTime", "10:30")
          )
      );
    assertEquals(200, response.status());
    assertTrue(response.url().matches(".*/plans/[0-9a-f-]{36}"));
    return response.url();
  }

  private String baseUrl() {
    return new WebPage(page).baseUrl();
  }

  private void awaitStyles() {
    page.waitForFunction(
      "() => [...document.querySelectorAll('head style')].some(style => style.textContent.includes('tailwindcss v')) && getComputedStyle(document.documentElement).visibility === 'visible'"
    );
  }
}
