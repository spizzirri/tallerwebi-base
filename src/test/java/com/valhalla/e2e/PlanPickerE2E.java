package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.PlansPage;
import org.junit.jupiter.api.Test;

/**
 * [APL] Picker de planes del panel de detalle de /explore, con sesión iniciada (T7).
 *
 * <p>E01 es la reproducción del bug: el filtro anterior escondía el picker entero cuando
 * todos los planes propios ya contenían el lugar, así que el usuario logueado con planes
 * no veía ninguna forma de agregarlo desde el panel.
 */
public class PlanPickerE2E extends E2eBase {

  @Test
  void E01_pickerStaysVisibleForPlaceAlreadyInPlan() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan APL T7", "Reproduccion picker", isoDateIn(30));
    String planId = detailPlanId();

    // El wizard agrega la primera tarjeta del catálogo: ese lugar ya está en el plan.
    String inPlanName = planItineraryFirstName(planId);

    plans.navigateToExplore();
    openDetails(inPlanName);

    // El bloque del picker debe pintarse aunque el plan ya contenga el lugar.
    page.waitForFunction("() => document.querySelector('#plan-picker') !== null");
    Locator option = page.locator("#plan-picker").locator("option[value='" + planId + "']");
    assertEquals(1, option.count(), "the own plan must stay listed for a place it already holds");
    assertTrue(
      option.first().isDisabled(),
      "a plan that already holds the place is not re-addable"
    );
    assertTrue(
      option.first().textContent().contains("(ya contiene el lugar)"),
      "the disabled option must say why it cannot be used"
    );
  }

  @Test
  void E02_addsSelectedPlaceToOwnPlanFromDetailPanel() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan APL T7b", "Agregado desde detalle", isoDateIn(30));
    String planId = detailPlanId();

    plans.navigateToExplore();
    // La última tarjeta nunca es la primera (el catálogo tiene 10+ lugares), así que
    // su lugar queda fuera del plan recién creado.
    page
      .locator("#explore-app > aside article")
      .last()
      .locator("span:text-is('View details →')")
      .click();
    waitForDetailsPanel();
    String placeName = page.locator("aside[aria-label^='Details for '] h2").textContent();

    page.waitForFunction("() => document.querySelector('#plan-picker') !== null");
    page.selectOption("#plan-picker", planId);
    page.click("#place-details-aside button[aria-label='Agregar al plan']");
    page.waitForFunction(
      "() => document.querySelector(\"#place-details-aside p[role='status']\") !== null"
    );
    String message = page.textContent("#place-details-aside p[role='status']");
    assertNotNull(message);
    assertFalse(message.isBlank());

    // La API confirma que el itinerario del plan ahora contiene el lugar agregado.
    Object inItinerary = page.evaluate(
      """
      async planId => {
        const itinerary = await (await fetch('/api/plans/' + planId + '/places')).json();
        return Array.isArray(itinerary) && itinerary.some(e => e.name === '%s');
      }
      """.formatted(placeName),
      planId
    );
    assertEquals(Boolean.TRUE, inItinerary);
  }

  /**
   * [T9] Regresión del reporte del usuario: logueado sin planes el panel de detalle no quedaba
   * mudo. El gate pasó de la cantidad de planes a la sesión, así que ahora se ofrece crear el
   * primer plan con el wizard de la misma página.
   */
  @Test
  void E03_offersCreatePlanWhenUserHasNoPlans() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    // openContext limpia la BD: el admin no tiene planes propios en este test.
    plans.navigateToExplore();
    page
      .locator("#explore-app > aside article")
      .last()
      .locator("span:text-is('View details →')")
      .click();
    waitForDetailsPanel();

    page.waitForFunction("() => document.querySelector(\"#place-details-aside button\") !== null");
    // has-text es substring: "Crear un plan" no matchea "Crear plan" del sidebar/wizard.
    Locator cta = page.locator("#place-details-aside button:has-text('Crear un plan')");
    assertEquals(1, cta.count(), "a plan-less logged-in user must get a create-plan CTA");
    assertEquals(0, page.locator("#plan-picker").count());

    cta.click();
    page.waitForFunction("() => document.querySelector('#create-plan-title') !== null");
    assertTrue(page.locator("#create-plan-title").isVisible());
  }

  /**
   * [T10] Regresión del reporte del usuario: con el wizard de creación abierto el botón del
   * panel de detalle debe agregar el lugar al plan EN PROCESO (borrador), no ofrecer crear
   * otro plan. Mismo destino que el "+" de las tarjetas.
   */
  @Test
  void E04_addsToTheOpenWizardDraftInsteadOfOfferingCreate() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.navigateToExplore();
    plans.navigateToCreatePlanModal();
    // La fecha del borrador queda por delante de la de los planes creados en otros tests (+90).
    plans.typeEventDate(isoDateIn(90));
    plans.clickNextStep();

    page
      .locator("#explore-app > aside article")
      .last()
      .locator("span:text-is('View details →')")
      .click();
    waitForDetailsPanel();

    assertEquals(0, page.locator("#plan-picker").count());
    assertEquals(
      0,
      page.locator("#place-details-aside button:has-text('Crear un plan')").count(),
      "with the wizard open the panel must not offer creating another plan"
    );
    Locator add = page.locator("#place-details-aside button[aria-label='Agregar al plan']");
    assertEquals(1, add.count(), "the button must add to the plan being created");
    add.click();

    // El lugar entró al borrador: la fila del paso 2 del wizard aparece (botón de quitar).
    page.waitForFunction(
      "() => document.querySelectorAll(\"#plans-aside button[aria-label='Quitar lugar']\").length === 1"
    );
    String message = page.textContent("#place-details-aside p[role='status']");
    assertTrue(message.contains("proceso"), "the inline feedback must name the open plan");
  }

  private String detailPlanId() {
    String url = page.url();
    return url.substring(url.lastIndexOf('/') + 1);
  }

  private String planItineraryFirstName(String planId) {
    return (String) page.evaluate(
      """
      async planId => {
        const itinerary = await (await fetch('/api/plans/' + planId + '/places')).json();
        return itinerary[0].name;
      }
      """,
      planId
    );
  }

  private void openDetails(String placeName) {
    page
      .locator("#explore-app > aside article")
      .filter(new Locator.FilterOptions().setHasText(placeName))
      .first()
      .locator("span:text-is('View details →')")
      .click();
    waitForDetailsPanel();
  }

  private void waitForDetailsPanel() {
    page.waitForFunction(
      "() => document.querySelector(\"aside[aria-label^='Details for ']\") !== null"
    );
  }
}
