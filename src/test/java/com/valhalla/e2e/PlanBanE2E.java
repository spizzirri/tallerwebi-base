package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

public class PlanBanE2E extends E2eBase {

  @Test
  void banAndUnbanInlineSectionPreservesPrivatePlanApproval() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan BAN", "Recorrido privado", "2026-12-31");
    String detailUrl = page.url();
    try (BrowserContext visitor = browser.newContext()) {
      Page guest = visitor.newPage();
      registerGuest(guest);
      guest.navigate(detailUrl + "/public");
      guest.locator("button:has-text('Solicitar participación')").click();
      page.navigate(detailUrl);
      page.locator("form[action*='/join-requests/'] button[value='accept']").click();
      guest.navigate(detailUrl);
      assertThat(guest.locator("main h2")).containsText("Plan BAN");
      assertThat(guest.locator("#plan-options-menu")).hasCount(0);
      page.locator("form[action$='/participants/ban'] button").click();
      guest.reload();
      assertThat(guest.locator("main")).containsText("Fuiste baneado");
      page.locator("[aria-controls='plan-options-menu']").click();
      page.locator("[aria-controls='banned-participants']").click();
      assertThat(page.locator("#banned-participants")).isVisible();
      assertThat(page.locator("#banned-participants")).containsText("Visitante BAN");
      assertThat(page.locator("#banned-participants"))
        .not()
        .containsText("visitante.ban@unlam.edu.ar");
      page.locator("form[action$='/participants/unban'] button").click();
      guest.navigate(detailUrl + "/public");
      assertThat(guest.locator("button:has-text('Solicitar participación')")).isVisible();
      assertThat(guest.locator(".stop-details")).hasCount(0);
      guest.locator("button:has-text('Solicitar participación')").click();
      page.reload();
      page.locator("form[action*='/join-requests/'] button[value='accept']").click();
      guest.navigate(detailUrl);
      assertThat(guest.locator("main h2")).containsText("Plan BAN");
      assertThat(guest.locator(".stop-details")).hasCount(1);
    }
  }

  private void registerGuest(Page guest) {
    RegisterPage register = new RegisterPage(guest);
    register.typeFirstName("Visitante");
    register.typeLastName("BAN");
    register.typeEmail("visitante.ban@unlam.edu.ar");
    register.typePassword("secret123");
    register.typeConfirmPassword("secret123");
    register.clickRegister();
    new WebPage(guest).waitForPath(LoginRedirects.USER_LANDING);
  }
}
