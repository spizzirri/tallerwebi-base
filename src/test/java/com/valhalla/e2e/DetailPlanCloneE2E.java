package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of the "Clonar plan" CTA on the plan detail view (E-02 of
 * docs/specs/15-CLO.md): a participant clones an accessible plan from {@code /plans/{id}} and
 * lands on the copy's own detail, with the itinerary copied over.
 */
public class DetailPlanCloneE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan a clonar E2E";
  private static final String EMAIL = "participante.clo@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void E02_clonePlanFromDetailViewLandsOnACopyWithTheItinerary() {
    String shortCode = givenAnAdminOwnsAPlanWithAPlace();

    // A fresh, signed-out context for the participant: cloning has to work on its own session,
    // and the admin session would silently own the copy instead.
    newContext();
    givenARegisteredUserJoinsThePlan(shortCode);

    PlansPage plans = new PlansPage(page);
    String originalId = currentPlanId();
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

  /** The admin owns the only plan and it carries a place, so the copy has an itinerary to show. */
  private String givenAnAdminOwnsAPlanWithAPlace() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPublicPlanViaExplore(PLAN_NAME, "Plan con un lugar para clonar", "2026-12-31");
    return this.page.locator("small:has-text('CÓDIGO') + span").textContent().trim();
  }

  /** Registration opens the session; joining by shortCode makes this user a participant. */
  private void givenARegisteredUserJoinsThePlan(String shortCode) {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Paula");
    register.typeLastName("Participante");
    register.typeEmail(EMAIL);
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    new WebPage(page).waitForPath(LoginRedirects.USER_LANDING);

    PlansPage plans = new PlansPage(page);
    plans.navigateToPlans();
    plans.waitForPath("/plans");
    this.page.fill("#shortCode", shortCode);
    this.page.locator("button:has-text('Unirme al plan')").click();
    plans.waitForDetailPath();
  }

  private String currentPlanId() {
    return this.page.url().substring(this.page.url().lastIndexOf('/') + 1);
  }
}
