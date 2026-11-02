package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

/**
 * [PLN] AC-14/AC-15 (docs/specs/03-PLN.md): el dueño es el único que reasigna roles existentes y
 * el email de los participantes nunca se muestra como texto.
 *
 * <p>El editor se registra en un contexto aparte, acepta la invitación y comprueba que ve el rol
 * como insignia, sin formulario y sin correo. Después el dueño lo degrada a Participante y el
 * editor ve el cambio sin recuperar el control.
 */
public class PlanParticipantsE2E extends E2eBase {

  private static final String PLAN_NAME = "Roles E2E";
  private static final String EDITOR_EMAIL = "editor.participantes@unlam.edu.ar";
  private static final String PARTICIPANTS = "section[aria-labelledby='participants-heading']";
  private static final String ROLE_FORM = PARTICIPANTS + " button:has-text('Guardar rol')";

  @Test
  void ownerChangesRoleWhileEditorOnlySeesTheBadgeAndNoEmail() {
    try (BrowserContext editorContext = browser.newContext()) {
      Page editorPage = editorContext.newPage();
      RegisterPage register = new RegisterPage(editorPage);
      register.typeFirstName("Editora");
      register.typeLastName("E2E");
      register.typeEmail(EDITOR_EMAIL);
      register.typePassword("secret123");
      register.typeConfirmPassword("secret123");
      register.clickRegister();
      new WebPage(editorPage).waitForPath(LoginRedirects.USER_LANDING);

      // El dueño crea el plan e invita a la editora con el rol Editor.
      signInAsAdmin();
      PlansPage plans = new PlansPage(page);
      plans.createPlanViaExplore(PLAN_NAME, "Prueba de roles y privacidad", isoDateIn(30));
      String detailUrl = page.url();
      page.locator("#invitation-email").fill(EDITOR_EMAIL);
      page.locator("#invitation-role").selectOption("editor");
      page
        .locator(PARTICIPANTS + " form[action*='/participants/invite'] button[type='submit']")
        .click();
      page.waitForLoadState();

      // La editora acepta y entra al detalle.
      editorPage.navigate(plans.baseUrl() + "/plans");
      editorPage.locator("form[action*='/invitations/'] button[value='true']").click();
      editorPage.waitForLoadState();
      editorPage.navigate(detailUrl);
      new PlansPage(editorPage).waitForDetailPath();

      // AC-14/AC-15: insignia visible, sin formulario de rol y sin email como texto.
      Locator editorSection = editorPage.locator(PARTICIPANTS);
      assertThat(editorSection.locator("button:has-text('Guardar rol')")).hasCount(0);
      assertThat(editorSection.locator(".badge:has-text('Editor')")).hasCount(1);
      assertFalse(editorSection.innerText().contains(EDITOR_EMAIL));

      // El dueño sí ve el formulario; su texto visible tampoco expone el correo.
      page.reload();
      assertThat(page.locator(ROLE_FORM)).hasCount(1);
      assertFalse(page.locator(PARTICIPANTS).innerText().contains(EDITOR_EMAIL));
      page
        .locator(PARTICIPANTS + " form[action*='/participants/role'] select[name='role']")
        .selectOption("guest");
      page.locator(ROLE_FORM).click();
      page.waitForLoadState();
      assertThat(
        page.locator(PARTICIPANTS + " form[action*='/participants/role'] select[name='role']")
      )
        .hasValue("guest");

      // La degradación se ve del lado de la editora, que sigue sin control de roles.
      editorPage.reload();
      assertThat(editorPage.locator(PARTICIPANTS + " .badge:has-text('Participante')")).hasCount(1);
      assertThat(editorPage.locator(ROLE_FORM)).hasCount(0);
    }
  }
}
