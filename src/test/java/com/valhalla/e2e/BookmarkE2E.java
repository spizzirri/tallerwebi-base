package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import java.util.concurrent.atomic.AtomicInteger;
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
  private static final String EMAIL = "guardador@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void E01_saveCloneAndRemoveABookmarkFromTheFeedAndTheOwnProfile() {
    String originalId = givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);

    // --- guardar desde la card del feed: el icono de contorno pasa a relleno
    page.navigate(plans.baseUrl() + "/for-you");
    Locator card = feedCard();
    Locator feedToggle = card.locator("[data-bookmark-toggle]");
    clickAndAwaitBookmark(feedToggle);
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

  /**
   * [MARK] El toggle del feed es optimista y no recarga: guardar y quitar cambian el icono en el
   * lugar. La vista publica del plan usa el mismo modulo y el token del input oculto, porque no
   * hereda los metas de CSRF.
   */
  @Test
  void E02_saveAndRemoveFromTheFeedAndThePublicViewWithoutReloading() {
    String planId = givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);
    page.navigate(plans.baseUrl() + "/for-you");
    Locator card = feedCard();
    Locator toggle = card.locator("[data-bookmark-toggle]");

    page.evaluate("window.__markNoReload = true");
    clickAndAwaitBookmark(toggle);
    assertThat(toggle).hasAttribute("data-bookmarked", "true");
    assertThat(toggle).hasAttribute("aria-pressed", "true");
    assertThat(toggle.locator("[data-bookmark-icon='filled']")).isVisible();
    assertThat(toggle.locator("[data-bookmark-icon='outline']")).isHidden();
    assertTrue(
      Boolean.TRUE.equals(page.evaluate("window.__markNoReload === true")),
      "guardar no debe recargar la pagina"
    );

    // El estado optimista coincide con el que persiste el servidor.
    page.reload();
    toggle = feedCard().locator("[data-bookmark-toggle]");
    assertThat(toggle).hasAttribute("data-bookmarked", "true");

    page.onDialog(dialog -> dialog.accept());
    page.evaluate("window.__markNoReload = true");
    clickAndAwaitBookmark(toggle);
    assertThat(toggle).hasAttribute("data-bookmarked", "false");
    assertThat(toggle).hasAttribute("aria-pressed", "false");
    assertThat(toggle.locator("[data-bookmark-icon='outline']")).isVisible();
    assertThat(toggle.locator("[data-bookmark-icon='filled']")).isHidden();
    assertTrue(
      Boolean.TRUE.equals(page.evaluate("window.__markNoReload === true")),
      "quitar no debe recargar la pagina"
    );

    // La vista publica no tiene metas de CSRF: el token sale del input oculto del form.
    page.navigate(plans.baseUrl() + "/plans/" + planId + "/public");
    Locator publicToggle = page.locator("[data-bookmark-toggle]");
    assertThat(publicToggle).isVisible();
    page.evaluate("window.__markNoReload = true");
    clickAndAwaitBookmark(publicToggle);
    assertThat(publicToggle).hasAttribute("data-bookmarked", "true");
    assertTrue(
      Boolean.TRUE.equals(page.evaluate("window.__markNoReload === true")),
      "guardar desde la vista publica no debe recargar"
    );

    // Deja la base como la encontro: el reset tambien limpia, pero el flujo cierra completo.
    clickAndAwaitBookmark(publicToggle);
    assertThat(publicToggle).hasAttribute("data-bookmarked", "false");
  }

  /** [MARK] Si la API falla, el estado optimista vuelve atras y el toast explica el motivo. */
  @Test
  void E03_aFailedToggleRevertsTheIconAndShowsTheToast() {
    givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);
    page.navigate(plans.baseUrl() + "/for-you");
    Locator toggle = feedCard().locator("[data-bookmark-toggle]");
    AtomicInteger attempts = new AtomicInteger();
    page.route(
      "**/api/plans/*/bookmarks",
      route -> {
        attempts.incrementAndGet();
        route.fulfill(
          new Route.FulfillOptions()
            .setStatus(500)
            .setContentType("application/json")
            .setBody("{\"message\":\"fallo simulado\"}")
        );
      }
    );

    clickAndAwaitBookmark(toggle);

    assertThat(page.locator("#bookmark-alert")).isVisible();
    assertThat(page.locator("#bookmark-alert")).containsText("No se pudo actualizar Guardados.");
    assertThat(toggle).hasAttribute("data-bookmarked", "false");
    assertThat(toggle).hasAttribute("aria-pressed", "false");
    assertThat(toggle.locator("[data-bookmark-icon='outline']")).isVisible();
    assertThat(toggle.locator("[data-bookmark-icon='filled']")).isHidden();
    assertThat(toggle).isEnabled();
    assertTrue(attempts.get() > 0, "el toggle tiene que pegarle a la API");
  }

  /** [MARK] El sidebar de /explore comparte el toggle optimista y su propio banner de error. */
  @Test
  void E04_theExploreSidebarSavesRemovesAndRollsBackWithoutReloading() {
    givenAnAdminOwnsAPublicPlan();
    signUpANormalUser();

    PlansPage plans = new PlansPage(page);
    plans.navigateToExplore();
    Locator toggle = exploreCard().locator("[data-bookmark-toggle]");

    page.evaluate("window.__markNoReload = true");
    clickAndAwaitBookmark(toggle);
    assertThat(toggle).hasAttribute("data-bookmarked", "true");
    assertThat(toggle).hasAttribute("aria-pressed", "true");
    assertThat(toggle.locator("[data-bookmark-icon='filled']")).isVisible();
    assertTrue(
      Boolean.TRUE.equals(page.evaluate("window.__markNoReload === true")),
      "guardar desde /explore no debe recargar"
    );

    page.reload();
    toggle = exploreCard().locator("[data-bookmark-toggle]");
    assertThat(toggle).hasAttribute("data-bookmarked", "true");

    page.onDialog(dialog -> dialog.accept());
    page.evaluate("window.__markNoReload = true");
    clickAndAwaitBookmark(toggle);
    assertThat(toggle).hasAttribute("data-bookmarked", "false");
    assertTrue(
      Boolean.TRUE.equals(page.evaluate("window.__markNoReload === true")),
      "quitar desde /explore no debe recargar"
    );

    AtomicInteger attempts = new AtomicInteger();
    page.route(
      "**/api/plans/*/bookmarks",
      route -> {
        attempts.incrementAndGet();
        route.fulfill(
          new Route.FulfillOptions()
            .setStatus(500)
            .setContentType("application/json")
            .setBody("{\"message\":\"fallo simulado\"}")
        );
      }
    );

    clickAndAwaitBookmark(toggle);

    Locator alert = page.locator("#explore-app [role='alert']");
    assertThat(alert).isVisible();
    assertThat(alert).containsText("No se pudo actualizar Guardados.");
    assertThat(toggle).hasAttribute("data-bookmarked", "false");
    assertThat(toggle.locator("[data-bookmark-icon='outline']")).isVisible();
    assertThat(toggle).isEnabled();
    assertTrue(attempts.get() > 0, "el sidebar tiene que pegarle a la API");
  }

  /**
   * Dispara el click y espera la respuesta de la API. Sin esta espera, el estado optimista deja
   * pasar la asercion y una recarga o navegacion inmediata aborta el request en vuelo.
   */
  private void clickAndAwaitBookmark(Locator toggle) {
    page.waitForResponse("**/api/plans/*/bookmarks", toggle::click);
  }

  /** El admin crea el plan publico con un lugar y devuelve el id del original. */
  private Locator exploreCard() {
    Locator card = page
      .locator("#plans-aside li")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat(card).hasCount(1);
    return card;
  }

  /** La card del plan sembrado en el feed. */
  private Locator feedCard() {
    Locator card = page
      .locator("article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat(card).hasCount(1);
    return card;
  }

  /** El admin crea el plan publico con un lugar y devuelve el id del original. */
  private String givenAnAdminOwnsAPublicPlan() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    // Fecha dinámica: el #plan-date del wizard lleva min=${today}, un literal duro vence.
    plans.createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, isoDateIn(30));
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
