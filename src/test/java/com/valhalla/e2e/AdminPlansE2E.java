package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.WebPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * [PABM] E-01 de 25-PABM.md: el admin crea un plan desde el alta de tres pasos, lo ve en el
 * listado, revisa el detalle de solo lectura y lo desactiva y borra desde la lista.
 *
 * <p>El alta exige al menos una parada: el flujo agrega un lugar desde el picker de mapa, igual
 * que lo haria el admin.
 */
public class AdminPlansE2E extends E2eBase {

  private static final Pattern DETAIL_URL = Pattern.compile(".*/admin/plans/[0-9a-f-]{36}$");

  @Test
  void E01_adminCreatesAPlanFromTheThreeStepWizardAndSeesItInTheList() {
    signInAsAdmin();
    String name = "Plan admin E2E uno";

    createPlanFromWizard(name);

    // Al crear, el alta redirige al detalle del plan nuevo.
    page.waitForURL(DETAIL_URL);
    assertTrue(page.locator("main").textContent().contains(name));

    page.navigate(url("/admin/plans"));
    assertTrue(rowFor(name).count() == 1, "the new plan must be listed");
  }

  @Test
  void E02_detailPageIsReadOnlyAndOnlyLinksToTheEdit() {
    signInAsAdmin();
    String name = "Plan admin E2E detalle";
    createPlanFromWizard(name);
    page.waitForURL(DETAIL_URL);

    // AC-08: el detalle no tiene formularios; la edicion vive en /edit.
    assertEquals(0, page.locator("main form").count(), "the detail page must not post anything");
    assertEquals(
      1,
      page.locator("main a[href$='/edit']").count(),
      "a single link must lead to the edit page"
    );
  }

  @Test
  void E03_adminDeactivatesAndDeletesAPlanFromTheList() {
    signInAsAdmin();
    String name = "Plan admin E2E borrar";
    createPlanFromWizard(name);
    page.waitForURL(DETAIL_URL);

    page.navigate(url("/admin/plans"));
    toggleActiveFor(name);
    page.waitForURL(url("/admin/plans"));
    assertTrue(
      page.locator("body").textContent().contains("Plan desactivado."),
      "deactivating must confirm with a toast"
    );

    page.navigate(url("/admin/plans"));
    deleteFor(name);
    page.waitForURL(url("/admin/plans"));
    assertTrue(
      page.locator("body").textContent().contains("Plan eliminado."),
      "deleting must confirm with a toast"
    );
    assertEquals(0, rowFor(name).count(), "the deleted plan must be gone from the list");
  }

  /**
   * Recorre el alta de tres pasos: datos basicos, una parada desde el picker de mapa, y el paso
   * de privacidad antes de crear.
   */
  private void createPlanFromWizard(String name) {
    page.navigate(url("/admin/plans/new"));
    page.fill("#name", name);
    page.fill("#eventDate", isoDateIn(30));
    page.click("#plan-next");

    // Paso 2: el picker muestra los lugares de la zona visible; se agrega el primero.
    page.waitForFunction(
      "() => document.querySelector(\"ul[aria-label='Lugares de la zona visible'] button\") !== null"
    );
    page.locator("ul[aria-label='Lugares de la zona visible'] button").first().click();
    page.click("#plan-places-add");
    page.click("#plan-next");

    // Paso 3: privacidad y permisos con los defaults, y crear.
    page.click("#plan-submit");
  }

  /** Url absoluta de la app de prueba, la misma que usan las demas paginas del suite. */
  private String url(String path) {
    return new WebPage(page).baseUrl() + path;
  }

  private Locator rowFor(String name) {
    return page.locator("table tbody tr").filter(new Locator.FilterOptions().setHasText(name));
  }

  private void toggleActiveFor(String name) {
    rowFor(name).locator("form[action*='toggle-active'] button").click();
  }

  private void deleteFor(String name) {
    rowFor(name).locator("form[action*='/delete'] button").click();
  }
}
