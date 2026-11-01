package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.valhalla.e2e.views.PlansPage;
import org.junit.jupiter.api.Test;

/**
 * [T-PLN-B06] La configuración del plan vive en el detalle y se guarda sin navegar a
 * {@code /plans/{id}/settings}. El dueño cambia los datos básicos y las capacidades de los roles
 * desde la misma página, y el detalle sigue siendo la URL actual.
 */
public class PlanDetailConfigurationE2E extends E2eBase {

  @Test
  void ownerConfiguresThePlanFromTheDetailWithoutLeavingThePage() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan configurable", "Configuración inicial", "2026-12-31");
    String detailUrl = page.url();

    assertThat(page.locator("#plan-app")).isVisible();
    assertThat(page.locator("main h2")).containsText("Plan configurable");

    page.locator("a:has-text('Configuración del plan')").click();
    assertThat(page.locator("#configuracion")).isVisible();
    assertThat(page.locator("#config-datos")).isVisible();
    assertThat(page.locator("#config-roles")).isVisible();
    assertThat(page.locator("#banned-participants")).isVisible();
    assertThat(page.locator("#config-solicitudes")).isVisible();
    assertThat(page.locator("form[action*='/participants/invite']")).isVisible();

    assertThat(page.getByText("Datos, solicitudes, roles y baneos en un solo lugar.")).hasCount(0);
    assertThat(page.locator("#configuracion nav")).hasCount(0);
    assertThat(page.locator("#configuracion a[href='#config-datos']")).hasCount(0);
    assertThat(page.locator("#configuracion a[href='#config-solicitudes']")).hasCount(0);
    assertThat(page.locator("#configuracion a[href='#config-roles']")).hasCount(0);
    assertThat(page.locator("#configuracion a[href='#banned-participants']")).hasCount(0);

    Boolean requestsAfterSummaryBeforeInvite = (Boolean) page.evaluate(
      """
      () => {
        const summary = document.getElementById('plan-summary');
        const requests = document.getElementById('config-solicitudes');
        const invite = document.querySelector("form[action*='/participants/invite']");
        return Boolean(
          summary &&
            requests &&
            invite &&
            (summary.compareDocumentPosition(requests) & Node.DOCUMENT_POSITION_FOLLOWING) &&
            (requests.compareDocumentPosition(invite) & Node.DOCUMENT_POSITION_FOLLOWING)
        );
      }
      """
    );
    assertTrue(requestsAfterSummaryBeforeInvite);

    page.locator("#config-datos input[name='name']").fill("Plan configurable ajustado");
    page
      .locator("#config-datos textarea[name='description']")
      .fill("Configuración actualizada desde el detalle");
    page.locator("#config-datos input[name='eventDate']").fill("2027-01-15");
    page.locator("#config-datos input[name='eventTime']").fill("19:30");
    page.locator("#config-datos button[type='submit']").click();
    plans.waitForDetailPath();

    assertThat(page).hasURL(detailUrl);
    assertThat(page.locator("main h2")).containsText("Plan configurable ajustado");
    assertThat(page.locator("#plan-description"))
      .containsText("Configuración actualizada desde el detalle");

    page.locator("#permission-inviteInternal").selectOption("PARTICIPANTS");
    page.locator("#permission-inviteByEmail").selectOption("EDITORS");
    page.locator("#permission-handleRequests").selectOption("EDITORS");
    page.locator("#config-roles button[type='submit']").click();
    plans.waitForDetailPath();

    assertThat(page).hasURL(detailUrl + "#config-roles");
    assertThat(page.locator("#configuracion .alert-success"))
      .containsText("Configuración guardada");
    assertThat(page.locator("#permission-inviteInternal")).hasValue("PARTICIPANTS");
    assertThat(page.locator("#permission-inviteByEmail")).hasValue("EDITORS");
    assertThat(page.locator("#permission-handleRequests")).hasValue("EDITORS");
    assertThat(page.locator("#config-solicitudes")).containsText("Solicitudes");
    assertThat(page.locator("#plan-share-link")).hasCount(0);
    assertThat(page.locator("#banned-participants"))
      .containsText("No hay usuarios baneados en este plan.");
    assertThat(page.locator("form[action$='/settings']")).hasCount(1);
  }
}
