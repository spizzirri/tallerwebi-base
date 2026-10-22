package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * [MARK] Happy path de "Guardados" (docs/specs/20-MARK.md, E-01) contra el Jetty real:
 *
 * <ol>
 *   <li>Un usuario normal guarda un plan publico desde su card de {@code /for-you}: el icono pasa
 *       de contorno a relleno sin recargar nada raro (AC-01, AC-02).
 *   <li>La entrada "Guardados" del modal del navbar lo lleva al ancla del perfil propio (AC-17).
 *   <li>La card aparece en {@code #bookmarks} y "Clonar plan" cae en el detalle de la copia
 *       (AC-07, AC-08, AC-13).
 *   <li>"Quitar" pide confirmacion nativa y deja el estado vacio (AC-06).
 * </ol>
 */
public class BookmarkE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan guardable E2E";
  private static final String PLAN_DESCRIPTION = "Un plan para guardar desde el feed";
  private static final String PLAN_DATE = "2026-12-31";
  private static final String EMAIL = "guardador@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void E01_saveCloneAndRemoveABookmarkFromTheFeedAndTheOwnProfile() {
    String originalId = givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);

    // --- guardar desde la card del feed: el icono de contorno pasa a relleno
    page.navigate(plans.baseUrl() + "/for-you");
    Locator card = page
      .locator("article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat(card).hasCount(1);
    card.locator("[data-bookmarked='false']").click();
    page.waitForSelector("article [data-bookmarked='true']");

    // --- el modal del navbar lleva al ancla del perfil propio
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");
    page.locator("#menu-bookmarks").click();
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}.*"));
    page.waitForSelector("#bookmarks");
    assertTrue(
      page.url().endsWith("#bookmarks"),
      "el link del navbar tiene que conservar el ancla, url=" + page.url()
    );
    assertThat(page.locator("#bookmarks")).containsText(PLAN_NAME);

    // --- clonar desde Guardados: cae en el detalle de la copia, no en el original
    page.locator("#bookmarks button:has-text('Clonar plan')").click();
    plans.waitForDetailPath();
    assertNotEquals(
      originalId,
      currentPlanId(),
      "el clon tiene que aterrizar en la copia, no en el plan de origen"
    );

    // --- quitar con la confirmacion nativa: la seccion vuelve al estado vacio
    page.navigate(plans.baseUrl() + "/profile");
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}.*"));
    page.waitForSelector("#bookmarks button:has-text('Quitar')");
    page.onDialog(dialog -> dialog.accept());
    page.locator("#bookmarks button:has-text('Quitar')").click();
    page.waitForSelector("[data-bookmarks-empty]");
  }

  /** El admin crea el plan publico con un lugar y devuelve el id del original. */
  private String givenAnAdminOwnsAPublicPlan() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, PLAN_DATE);
    return currentPlanId();
  }

  /**
   * Contexto nuevo y registro por la UI: la sesion de la feature es un usuario normal, no el admin
   * (el admin tambien es usuario, pero el feed no ofrece el toggle de sus propios planes).
   */
  private void signUpANormalUser() {
    newContext();
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Gloria");
    register.typeLastName("Guardados");
    register.typeEmail(EMAIL);
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    new WebPage(page).waitForPath("/for-you");
  }

  private String currentPlanId() {
    return page.url().substring(page.url().lastIndexOf('/') + 1);
  }
}
