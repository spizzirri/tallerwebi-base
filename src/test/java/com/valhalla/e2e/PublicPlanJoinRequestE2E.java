package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

public class PublicPlanJoinRequestE2E extends E2eBase {

  @Test
  void E01_publicUrlRequestAndAcceptanceGrantPrivatePlanAccess() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Encuentro privado PET", "Resumen del encuentro", isoDateIn(30));
    String publicUrl = page.url() + "/public";

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
      visitorPage.navigate(publicUrl);
      assertThat(visitorPage.locator("main h1")).hasText("Encuentro privado PET");
      assertThat(visitorPage.locator(".stop-details")).hasCount(0);
      visitorPage.locator("button:has-text('Solicitar participación')").click();
      new WebPage(visitorPage).waitForPath("/plans");
      assertThat(visitorPage.locator(".planit-toast[role='status']"))
        .containsText("Solicitud enviada");

      visitorPage.navigate(publicUrl);
      assertThat(visitorPage.locator("main [role='status']"))
        .containsText("Ya enviaste tu solicitud");

      page.reload();
      // [STD] Aceptar vive en la card centrada de Solicitudes.
      plans.openRequests();
      page.locator("form[action*='/join-requests/'] button[value='accept']").click();
      assertThat(page.locator("form[action*='/join-requests/']")).hasCount(0);
      visitorPage.navigate(publicUrl);
      new PlansPage(visitorPage).waitForDetailPath();
      assertThat(visitorPage.locator("main h2")).containsText("Encuentro privado PET");
      assertThat(visitorPage.locator(".stop-details")).hasCount(1);
    }
  }
}
