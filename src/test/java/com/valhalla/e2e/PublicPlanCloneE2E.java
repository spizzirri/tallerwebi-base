package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of the "Clonar plan" CTA on the public plan view (E-01 of
 * docs/specs/15-CLO.md): a registered user with no access to the plan opens
 * {@code /plans/{id}/public} and clones it from the CTA, landing on the copy's own detail with
 * the itinerary copied over.
 *
 * <p>E2eBase wipes the database before every test, so the flow creates its own public plan from
 * the UI (create modal on /explore + the public checkbox on the detail page) and then registers a
 * fresh non-participant in a clean context — the admin session would silently own the copy.
 */
public class PublicPlanCloneE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan publico a clonar E2E";
  private static final String PLAN_DESCRIPTION = "Un plan publico que cualquiera puede clonar";
  private static final String PLAN_DATE = "2026-12-31";
  private static final String EMAIL = "visitante.clo@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void E01_clonePlanFromPublicViewLandsOnACopyWithTheItinerary() {
    String planUrl = givenAPublicPlanUrl();
    String originalId = currentPlanId();

    // A fresh, signed-out context for the visitor: cloning has to work on its own session.
    newContext();
    givenARegisteredUserOpens(planUrl);

    PlansPage plans = new PlansPage(page);
    assertThat(page.locator("button:has-text('Clonar plan')")).isVisible();

    page.locator("button:has-text('Clonar plan')").click();
    plans.waitForDetailPath();

    assertNotEquals(
      originalId,
      currentPlanId(),
      "the CTA has to land on the copy, not back on the source plan"
    );
    assertThat(page.locator("main h2")).containsText(PLAN_NAME);
    assertThat(page.locator(".stop-details")).hasCount(1);
  }

  /** The admin owns a public plan with a place, so the copy has an itinerary to show. */
  private String givenAPublicPlanUrl() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, PLAN_DATE);
    plans.waitForDetailPath();

    String planId = currentPlanId();
    makePlanPublic(plans);

    return plans.baseUrl() + "/plans/" + planId + "/public";
  }

  private void makePlanPublic(PlansPage plans) {
    // The editor submits the itinerary rows from Vue, which mount after the page loads.
    // The row inputs are type=hidden, so wait for attached rather than for visible.
    page
      .locator("input[name='places[0].placeId']")
      .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator("#isPublic").check();
    page.locator("button:has-text('Guardar cambios')").click();
    plans.waitForDetailPath();
  }

  /** Registration opens the session; the visitor never joins the plan, so the CTA stays visible. */
  private void givenARegisteredUserOpens(String planUrl) {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Veronica");
    register.typeLastName("Visitante");
    register.typeEmail(EMAIL);
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    // Contra la constante de landing, no un literal: tolera el cambio a /plans/public (PR #15)
    new WebPage(page).waitForPath(LoginRedirects.USER_LANDING);

    new WebPage(page).navigate(planUrl);
  }

  private String currentPlanId() {
    String path = URI.create(page.url()).getPath();
    return path.substring(path.lastIndexOf('/') + 1);
  }
}
