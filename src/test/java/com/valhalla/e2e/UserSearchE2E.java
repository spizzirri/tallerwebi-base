package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * [USEARCH] T-USEARCH-004: el recorrido completo del buscador de personas.
 *
 * <p>Arrange: el admin de la seed publica un plan publico por UI. El visitante se registra y usa
 * el buscador del navbar (el mismo input con debounce que ve el usuario) para encontrar al autor
 * por nombre. La fila muestra la cantidad de planes publicos y el boton "Seguir"; el POST vuelve
 * al mismo listado, el boton se re-renderiza como "Siguiendo" y el plan aparece en el scope
 * Siguiendo del feed.
 */
public class UserSearchE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan que se descubre buscando";
  private static final String PLAN_DESCRIPTION = "Un plan publico para el buscador de usuarios";

  private static final String VIEWER_FIRST_NAME = "Rosa";
  private static final String VIEWER_LAST_NAME = "Buscadora";
  private static final String VIEWER_EMAIL = "rosa.buscadora@unlam.edu.ar";
  private static final String VIEWER_PASSWORD = "secret123";

  @Test
  void E01_buscarSeguirYVerElPlanDelAutorEnSiguiendo() {
    // --- arrange: el admin publica un plan publico y el visitante se registra ---
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, isoDateIn(30));

    newContext();
    WebPage web = new WebPage(page);
    registerViewer(web);

    // --- buscar desde el navbar, con debounce, sin pasar por /users a mano ---
    Locator search = page.locator("body > nav input[data-user-search]");
    assertEquals(1, search.count());
    search.fill("Admin");
    page.waitForURL(Pattern.compile(".*/users\\?q=Admin.*"));
    assertTrue(page.textContent("main h1").contains("Buscar usuarios"));

    // --- resultado: nombre, contador de planes publicos y boton para seguir ---
    Locator row = page
      .locator("[data-user-result]")
      .filter(new Locator.FilterOptions().setHasText("Admin Test"));
    assertEquals(1, row.count());
    assertTrue(row.textContent().replaceAll("\\s+", " ").contains("1 plan publico"));
    row.locator("button:text-is('Seguir')").click();

    // El POST redirige a la misma busqueda: la espera es el boton re-renderizado.
    page.waitForSelector("[data-user-result] button:text-is('Siguiendo')");
    assertTrue(page.url().contains("/users?q=Admin"));

    // --- el follow desde el resultado alimenta el scope Siguiendo del feed ---
    web.navigate(web.baseUrl() + "/for-you?scope=following");
    Locator cards = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertEquals(1, cards.count());
    assertEquals(0, page.locator("[data-testid='following-empty']").count());
  }

  private void registerViewer(WebPage web) {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName(VIEWER_FIRST_NAME);
    register.typeLastName(VIEWER_LAST_NAME);
    register.typeEmail(VIEWER_EMAIL);
    register.typePassword(VIEWER_PASSWORD);
    register.typeConfirmPassword(VIEWER_PASSWORD);
    register.clickRegister();
    web.waitForPath(LoginRedirects.USER_LANDING);
  }
}
