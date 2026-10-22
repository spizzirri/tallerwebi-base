package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

/**
 * [FOLLOW] E-01 (docs/specs/19-FOLLOW.md): el recorrido completo de seguir y dejar de seguir.
 *
 * <p>Arrange: el unico autor garantizado por {@link ResetDatabase} (el admin de la seed) publica un
 * plan publico por UI. El seeder de fantasmas de [PPV] esta apagado en E2E, asi que no hay usuarios
 * demo disponibles: el autor seguido se arma en el arrange, igual que en {@link
 * PublicPlansListE2E}.
 *
 * <p>El seguidor se registra por UI. El registro abre la sesion y aterriza en el feed, donde el
 * plan del admin es ajeno: el mismo plan mide el scope Siguiendo con y sin la relacion. El autor se
 * descubre desde la tarjeta ("Por ..." linkea a /profile/{id}), nunca por un id cableado, asi que
 * el test no depende de la numeracion de la base.
 */
public class FollowE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan del autor seguido";
  private static final String PLAN_DESCRIPTION = "Un plan publico para el scope Siguiendo";
  private static final String PLAN_DATE = "2026-12-31";

  private static final String FOLLOWER_FIRST_NAME = "Rosa";
  private static final String FOLLOWER_LAST_NAME = "Seguidora";
  private static final String FOLLOWER_EMAIL = "rosa.seguidora@unlam.edu.ar";
  private static final String FOLLOWER_PASSWORD = "secret123";

  @Test
  void E01_seguirYDejarDeSeguirCambiaElScopeSiguiendo() {
    // --- arrange: el admin publica un plan publico y el seguidor se registra por UI ---
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, PLAN_DATE);

    newContext();
    WebPage web = new WebPage(page);
    registerFollower(web);
    String authorPath = authorPathFromFeed();

    // --- AC-01: seguir desde el perfil publico del autor ---
    web.navigate(web.baseUrl() + authorPath);
    page.waitForSelector("#btn-follow:text-is('Seguir')");
    page.locator("#btn-follow").click();
    // El POST redirige al mismo perfil ya como seguidor: la espera es el boton re-renderizado.
    page.waitForSelector("#btn-follow:text-is('Dejar de seguir')");
    assertTrue(page.textContent("#follow").contains("1 seguidores"));

    // --- AC-07/AC-08: el scope Siguiendo trae el plan publico del autor seguido ---
    web.navigate(web.baseUrl() + "/for-you?scope=following");
    assertEquals(1, cardsWithPlan().count());
    assertEquals(0, page.locator("[data-testid='following-empty']").count());

    // --- AC-02: dejar de seguir deja el plan fuera del scope ---
    web.navigate(web.baseUrl() + authorPath);
    page.waitForSelector("#btn-follow:text-is('Dejar de seguir')");
    page.locator("#btn-follow").click();
    page.waitForSelector("#btn-follow:text-is('Seguir')");

    web.navigate(web.baseUrl() + "/for-you?scope=following");
    assertEquals(0, cardsWithPlan().count());
    assertTrue(page.locator("[data-testid='following-empty']").isVisible());
  }

  /** Registra al seguidor por UI: el alta abre la sesion y aterriza en el feed (como [PPV]). */
  private void registerFollower(WebPage web) {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName(FOLLOWER_FIRST_NAME);
    register.typeLastName(FOLLOWER_LAST_NAME);
    register.typeEmail(FOLLOWER_EMAIL);
    register.typePassword(FOLLOWER_PASSWORD);
    register.typeConfirmPassword(FOLLOWER_PASSWORD);
    register.clickRegister();
    web.waitForPath(LoginRedirects.USER_LANDING);
  }

  /**
   * Ruta del perfil del autor del plan publico, leida del link "Por ..." de su tarjeta en el feed:
   * el id lo asigna la base y el test no lo adivina.
   */
  private String authorPathFromFeed() {
    Locator card = cardsWithPlan();
    assertEquals(1, card.count());
    return card.locator("a[href*='/profile/']").first().getAttribute("href");
  }

  /** Tarjeta del plan del autor, sin depender del orden del feed. */
  private Locator cardsWithPlan() {
    return page.locator("main article").filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
  }
}
