package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * [MYRAT] Browser coverage de "Mis calificaciones" (docs/specs/26-MYRAT.md, T-MYRAT-005/006) contra
 * el Jetty real:
 *
 * <ol>
 *   <li>Un usuario normal califica un plan publico desde su vista publica y la calificacion
 *       aparece en {@code #my-ratings} de su perfil, con sus estrellas, el agregado global y el
 *       link a esa misma vista publica.
 *   <li>La entrada "Mis calificaciones" del modal del navbar lleva al ancla de la seccion en el
 *       perfil propio.
 * </ol>
 *
 * <p>El arrange es el de [MARK]: el admin crea el plan publico por UI y despues un contexto nuevo
 * registra al usuario normal, porque el caso que importa es el de un tercero que califica un plan
 * ajeno.
 */
public class MyRatingsE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan calificado E2E";
  private static final String PLAN_DESCRIPTION = "Un plan para recordar que opine";
  private static final String EMAIL = "calificador@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void T_MYRAT_005_aRatingGivenInThePublicViewShowsUpInTheOwnProfile() {
    String planId = givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);

    // --- califica 4 estrellas desde la vista publica del plan (el usuario no es miembro)
    page.navigate(plans.baseUrl() + "/plans/" + planId + "/public");
    rateFromThisPage(4);

    // --- el perfil propio lista la calificacion recien dada
    page.navigate(plans.baseUrl() + "/profile");
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}.*"));
    page.waitForSelector("#my-ratings");

    assertThat(page.locator("#my-ratings [data-my-ratings-count]")).hasText("1");
    Locator row = page.locator("#my-ratings [data-my-rating-row]");
    assertThat(row).hasCount(1);
    assertThat(row).containsText(PLAN_NAME);

    // No es miembro: el link cae en la vista publica, no en el detalle privado.
    assertThat(row.locator("a")).hasAttribute("href", "/plans/" + planId + "/public");

    // Las estrellas propias son las que dio, y el agregado global es el de la unica calificacion.
    assertEquals("Tu calificación: 4 de 5", ownStarsLabel(row));
    String global = row.locator("[role='img']").nth(1).getAttribute("aria-label");
    assertTrue(global.startsWith("Puntuación 4"), global);
    assertTrue(global.contains("con 1 calificación"), global);
    assertThat(row).containsText("1 calificación");

    // Con historial, el estado vacio no se renderiza.
    assertThat(page.locator("#my-ratings [data-my-ratings-empty]")).hasCount(0);
  }

  @Test
  void T_MYRAT_006_theMenuEntryNavigatesToTheRatingsSection() {
    signInAsAdmin();

    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    Locator entry = page.locator("#navbar-menu a#menu-my-ratings");
    assertThat(entry).hasCount(1);
    assertTrue(entry.getAttribute("href").endsWith("/profile#my-ratings"));

    entry.click();

    // /profile resuelve el id propio con un 302 y el fragmento sobrevive al redirect.
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}#my-ratings"));
    page.waitForSelector("#my-ratings");
    assertThat(page.locator("#my-ratings")).isVisible();
    assertTrue(page.textContent("#my-ratings h2").contains("Mis calificaciones"));
  }

  /**
   * Vota con un solo click: el DOM del radio-group esta invertido (5..1, el combinator ~ rellena
   * hasta la elegida) y el change del radio ya envia el form por el auto-submit del widget. La
   * espera es el radio re-renderizado marcado en la respuesta del POST (mismo helper que [CAL]).
   */
  private void rateFromThisPage(int stars) {
    page.locator(".rating-star-option").nth(5 - stars).click();
    page.waitForSelector("input[name='score'][value='" + stars + "']:checked");
  }

  /** El aria-label del indicador estatico de las estrellas propias dentro de la fila. */
  private String ownStarsLabel(Locator row) {
    return row.locator("[role='img']").nth(0).getAttribute("aria-label");
  }

  /** El admin crea el plan publico con un lugar y devuelve el id del original. */
  private String givenAnAdminOwnsAPublicPlan() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    // Fecha dinámica: el #plan-date del wizard lleva min=${today}, un literal duro vence.
    plans.createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, isoDateIn(30));
    return page.url().substring(page.url().lastIndexOf('/') + 1);
  }

  /**
   * Contexto nuevo y registro por la UI: la sesion de la feature es un usuario normal, no el admin
   * (el admin califica sus propios planes, pero el caso que importa es el de un tercero).
   */
  private void signUpANormalUser() {
    newContext();
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Carla");
    register.typeLastName("Calificadora");
    register.typeEmail(EMAIL);
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    new WebPage(page).waitForPath("/for-you");
  }
}
