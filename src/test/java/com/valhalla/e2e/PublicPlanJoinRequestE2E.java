package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class PublicPlanJoinRequestE2E extends E2eBase {

  @Test
  void E01_sharedLinkRequestAndAcceptanceGrantPrivatePlanAccess() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Encuentro privado PET", "Resumen del encuentro", "2026-12-31");
    String detailUrl = page.url();
    page.navigate(detailUrl + "/settings");
    page.locator("select[name='shareLink']").selectOption("EDITORS");
    page.locator("main form button[type='submit']").click();
    page.navigate(detailUrl);
    String sharedUrl = URI
      .create(plans.baseUrl())
      .resolve(page.locator("#plan-share-link").inputValue())
      .toString();

    try (BrowserContext visitor = browser.newContext()) {
      Page visitorPage = visitor.newPage();
      RegisterPage register = new RegisterPage(visitorPage);
      register.typeFirstName("Solicitante");
      register.typeLastName("PET");
      register.typeEmail("solicitante.pet@unlam.edu.ar");
      register.typePassword("secret123");
      register.typeConfirmPassword("secret123");
      register.clickRegister();
      new WebPage(visitorPage).waitForPath(LoginRedirects.USER_LANDING);
      visitorPage.navigate(sharedUrl);
      assertThat(visitorPage.locator("main h1")).hasText("Encuentro privado PET");
      assertThat(visitorPage.locator(".stop-details")).hasCount(0);
      visitorPage.locator("button:has-text('Solicitar participación')").click();
      assertThat(visitorPage.locator("main [role='status']"))
        .containsText("Ya enviaste tu solicitud");

      page.reload();
      page.locator("form[action*='/join-requests/'] button[value='accept']").click();
      assertThat(page.locator("form[action*='/join-requests/']")).hasCount(0);
      visitorPage.navigate(sharedUrl);
      new PlansPage(visitorPage).waitForDetailPath();
      assertThat(visitorPage.locator("main h2")).containsText("Encuentro privado PET");
      assertThat(visitorPage.locator(".stop-details")).hasCount(1);
    }
  }
}
