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
  void banAndUnbanFromTheParticipantsDialogPreservesPrivatePlanApproval() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore("Plan BAN", "Recorrido privado", isoDateIn(30));
    String detailUrl = page.url();
    try (BrowserContext visitor = browser.newContext()) {
      Page guest = visitor.newPage();
      registerGuest(guest);
      guest.navigate(detailUrl + "/public");
      guest.locator("button:has-text('Solicitar participación')").click();
      page.navigate(detailUrl);
      // [STD] Las solicitudes se responden en su card centrada.
      plans.openRequests();
      page.locator("form[action*='/join-requests/'] button[value='accept']").click();
      guest.navigate(detailUrl);
      assertThat(guest.locator("main h2")).containsText("Plan BAN");
      // El invitado no tiene el botón de baneados (vive en Participantes con th:if de edición).
      assertThat(guest.locator("[aria-controls='banned-participants']")).hasCount(0);
      // [STD-009/AC-09] "Más opciones" salió de la cabecera: ni menú, ni su trigger.
      assertThat(page.locator("#plan-options-menu")).hasCount(0);
      assertThat(page.locator("[aria-controls='plan-options-menu']")).hasCount(0);
      // [STD] Banear vive en la card centrada de Participantes.
      plans.openParticipants();
      page.locator("form[action$='/participants/ban'] button").click();
      guest.reload();
      assertThat(guest.locator("main")).containsText("Fuiste baneado");
      // El banear es un POST: la página recarga y la card de Participantes vuelve a su estado.
      plans.openParticipants();
      page.locator("[aria-controls='banned-participants']").click();
      assertThat(page.locator("#banned-participants")).isVisible();
      assertThat(page.locator("#banned-participants")).containsText("Visitante BAN");
      assertThat(page.locator("#banned-participants"))
        .not()
        .containsText("visitante.ban@unlam.edu.ar");
      // [STD-010] Escape cierra el diálogo y devuelve el foco al botón de Participantes.
      page.locator("#banned-participants").press("Escape");
      assertThat(page.locator("#banned-participants")).isHidden();
      assertThat(page.locator("[aria-controls='banned-participants']")).isFocused();
      page.locator("[aria-controls='banned-participants']").click();
      page.locator("form[action$='/participants/unban'] button").click();
      guest.navigate(detailUrl + "/public");
      assertThat(guest.locator("button:has-text('Solicitar participación')")).isVisible();
      assertThat(guest.locator(".stop-details")).hasCount(0);
      guest.locator("button:has-text('Solicitar participación')").click();
      page.reload();
      plans.openRequests();
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
