package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import org.junit.jupiter.api.Test;

/**
 * [T-PLN-B06] La configuración del plan vive en el detalle y se guarda sin navegar a
 * {@code /plans/{id}/settings}. El dueño abre la card centrada desde la cabecera, cambia los datos
 * básicos y las capacidades de los roles, y el detalle sigue siendo la URL actual.
 */
public class PlanDetailConfigurationE2E extends E2eBase {

  /** E-PRE-01: budget editing shares the metadata dialog, including server errors. */
  @Test
  void ownerEditsBudgetInThePlanDialogAndRecoversFromInvalidRange() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan con presupuesto", "Salidas y entradas", isoDateIn(30));
    String detailUrl = page.url();
    Locator config = page.locator("#plan-config-dialog");
    Locator trigger = page.locator("#plan-summary button:has-text('Editar')");
    Locator budget = config.locator("#config-presupuesto");
    Locator summary = page.locator("#presupuesto");

    assertThat(summary).containsText("Sin estimar");
    assertThat(config).isHidden();
    assertThat(page.locator("#plan-summary form[action$='/budget']")).hasCount(0);
    trigger.click();
    assertThat(config).isVisible();
    assertThat(budget).isVisible();
    budget.locator("#budget-min").fill("1500.50");
    budget.locator("#budget-max").fill("2500");
    budget.locator("#budget-notes").fill("Comida y entradas. Transporte aparte.");
    budget.locator("button[type='submit']").click();
    assertThat(page).hasURL(detailUrl + "#presupuesto");
    assertThat(config).isHidden();
    assertThat(summary).containsText("1.500,50");
    assertThat(summary).containsText("2.500,00");
    assertThat(summary).containsText("Comida y entradas. Transporte aparte.");

    page.reload();
    trigger.click();
    assertThat(budget.locator("#budget-min")).hasValue("1500.50");
    assertThat(budget.locator("#budget-max")).hasValue("2500.00");
    budget.locator("#budget-min").fill("3000");
    budget.locator("#budget-max").fill("1000");
    budget.locator("button[type='submit']").click();

    // Both values satisfy HTML constraints; their order must be rejected by the server.
    assertThat(page).hasURL(detailUrl + "/budget");
    assertThat(config).isVisible();
    assertThat(budget.locator("[role='alert']")).isVisible();
    assertThat(budget.locator("#budget-min")).hasValue("3000");
    assertThat(budget.locator("#budget-max")).hasValue("1000");
    assertThat(budget.locator("#budget-notes")).hasValue("Comida y entradas. Transporte aparte.");
    assertThat(summary).containsText("1.500,50");
    assertThat(summary).containsText("2.500,00");

    budget.locator("#budget-min").fill("2000");
    budget.locator("#budget-max").fill("2000");
    budget.locator("button[type='submit']").click();
    assertThat(page).hasURL(detailUrl + "#presupuesto");
    assertThat(config).isHidden();
    trigger.click();
    assertThat(budget.locator("[role='alert']")).hasCount(0);
    assertThat(budget.locator("#budget-min")).hasValue("2000.00");
    assertThat(budget.locator("#budget-max")).hasValue("2000.00");
    page.keyboard().press("Escape");
    assertThat(config).isHidden();
    assertThat(summary).containsText("2.000,00");
  }

  @Test
  void ownerConfiguresThePlanFromTheDetailWithoutLeavingThePage() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan configurable", "Configuración inicial", isoDateIn(30));
    String detailUrl = page.url();

    assertThat(page.locator("#plan-app")).isVisible();
    assertThat(page.locator("main h2")).containsText("Plan configurable");

    // [STD] AC-04: invitar vive plegado dentro de la card centrada de Participantes; se abre esa
    // card y después el accordion (con un modal abierto el resto del documento queda inerte).
    plans.openParticipants();
    page.locator("button[aria-controls='invite-panel']").click();
    assertThat(page.locator("form[action*='/participants/invite']")).isVisible();
    page.keyboard().press("Escape");
    assertThat(page.locator("#participants-dialog")).isHidden();

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

    // [STD] La configuración es una card centrada: se abre desde la cabecera, sin sumar scroll.
    Locator config = page.locator("#plan-config-dialog");
    Locator configTrigger = page.locator(
      ".plan-toolbar button[aria-controls='plan-config-dialog']"
    );
    assertThat(config).isHidden();
    configTrigger.click();
    assertThat(config).isVisible();
    assertThat(page.locator("#config-datos")).isVisible();
    assertThat(page.locator("#config-roles")).isVisible();
    assertThat(page.locator("#config-solicitudes")).containsText("Solicitudes");

    page.locator("#config-datos input[name='name']").fill("Plan configurable ajustado");
    page
      .locator("#config-datos textarea[name='description']")
      .fill("Configuración actualizada desde el detalle");
    // Fecha dinámica: el input del wizard lleva min=${today}, un literal duro vence contra el reloj.
    page.locator("#config-datos input[name='eventDate']").fill(isoDateIn(45));
    page.locator("#config-datos input[name='eventTime']").fill("19:30");
    page.locator("#config-datos button[type='submit']").click();
    plans.waitForDetailPath();

    assertThat(page).hasURL(detailUrl);
    assertThat(page.locator("main h2")).containsText("Plan configurable ajustado");
    assertThat(page.locator("#plan-description"))
      .containsText("Configuración actualizada desde el detalle");

    // Los roles se guardan desde la misma card, que se reabre tras cada redirect.
    configTrigger.click();
    page.locator("#permission-inviteInternal").selectOption("PARTICIPANTS");
    page.locator("#permission-inviteByEmail").selectOption("EDITORS");
    page.locator("#permission-handleRequests").selectOption("EDITORS");
    page.locator("#config-roles button[type='submit']").click();
    plans.waitForDetailPath();

    assertThat(page).hasURL(detailUrl + "#config-roles");
    configTrigger.click();
    assertThat(page.locator("#configuracion .alert-success"))
      .containsText("Configuración guardada");
    assertThat(page.locator("#permission-inviteInternal")).hasValue("PARTICIPANTS");
    assertThat(page.locator("#permission-inviteByEmail")).hasValue("EDITORS");
    assertThat(page.locator("#permission-handleRequests")).hasValue("EDITORS");
    page.keyboard().press("Escape");
    assertThat(config).isHidden();

    // Los baneados tienen su propio diálogo, abierto desde la card centrada de Participantes.
    plans.openParticipants();
    page.locator("[aria-controls='banned-participants']").click();
    assertThat(page.locator("#banned-participants"))
      .containsText("No hay usuarios baneados en este plan.");
    page.keyboard().press("Escape");
    assertThat(page.locator("#banned-participants")).isHidden();
    page.keyboard().press("Escape");
    assertThat(page.locator("#participants-dialog")).isHidden();

    assertThat(page.locator("#config-solicitudes")).containsText("Solicitudes");
    assertThat(page.locator("#plan-share-link")).hasCount(0);
    assertThat(page.locator("form[action$='/settings']")).hasCount(1);
  }
}
