package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

/**
 * [NOTIF/NGRP] E-01/E-02 (docs/specs/25-NOTIF.md): badge, centro y apertura de grupo.
 *
 * <p>Arrange como en {@link FollowE2E}: el admin (unico usuario garantizado por la seed) publica
 * un plan, una follower registrada por UI lo sigue. La segunda sesion (newContext) entra como el
 * autor seguido, que es quien tiene la notificacion pendiente. El badge se mide por el atributo
 * {@code data-notifications-count} de la campana: el numero se pinta por CSS y no es texto del
 * DOM, asi que el pinneo de {@code getNavbarItems} no lo ve.
 */
public class NotificationE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan del autor notificado";
  private static final String PLAN_DESCRIPTION = "Un plan publico que genera la notificacion";
  private static final String PLAN_DATE = "2026-12-31";
  private static final String GROUP_PLAN_NAME = "Plan del autor con grupo";
  private static final String GROUP_PLAN_DESCRIPTION = "Un plan publico que genera tres seguidores";

  private static final String FOLLOWER_EMAIL = "rosa.notif@unlam.edu.ar";
  private static final String FOLLOWER_PASSWORD = "secret123";

  @Test
  void E01_seguirAAlguienLePintaElBadgeYElCentroMarcaYBorraLeidas() {
    // --- arrange: el admin publica un plan publico ---
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, PLAN_DATE);

    // --- una usuaria nueva sigue al admin ---
    newContext();
    WebPage web = new WebPage(page);
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Rosa");
    register.typeLastName("Seguidora");
    register.typeEmail(FOLLOWER_EMAIL);
    register.typePassword(FOLLOWER_PASSWORD);
    register.typeConfirmPassword(FOLLOWER_PASSWORD);
    register.clickRegister();
    web.waitForPath(LoginRedirects.USER_LANDING);

    String authorPath = authorPathFromFeed();
    web.navigate(web.baseUrl() + authorPath);
    page.waitForSelector("#btn-follow:text-is('Seguir')");
    page.locator("#btn-follow").click();
    page.waitForSelector("#btn-follow:text-is('Dejar de seguir')");

    // --- el autor, en sesion nueva, ve el badge con 1 no leida ---
    // `web` apuntaba a la pagina del contexto anterior: habita de reconstruirlo contra la pagina
    // nueva, si no waitForPath esperaria sobre una pagina que este contexto nunca ve.
    newContext();
    web = new WebPage(page);
    LoginPage login = new LoginPage(page);
    login.typeEmail("test@unlam.edu.ar");
    login.typePassword("password");
    login.clickSignIn();
    login.waitForPath("/admin/home");
    web.navigate(web.baseUrl() + "/for-you");
    Locator bell = page.locator("#navbar-notifications");
    assertEquals("1", bell.getAttribute("data-notifications-count"));

    // --- AC-04/AC-05: la campana navega al centro y la actividad esta listada ---
    bell.click();
    web.waitForPath("/notifications");
    assertTrue(page.textContent("#notifications-list").contains("Rosa Seguidora"));
    assertTrue(page.textContent("#notifications-list").contains("te empezó a seguir"));

    // --- AC-06: marcar leida descuenta el badge en el render siguiente ---
    page.locator(".btn-mark-read").first().click();
    web.waitForPath("/notifications");
    assertEquals(
      "0",
      page.locator("#navbar-notifications").getAttribute("data-notifications-count")
    );
    assertEquals(0, page.locator(".btn-mark-read").count());

    // --- la limpieza de leidas tira la fila y la bandeja queda vacia ---
    page.onDialog(dialog -> dialog.accept());
    page.locator("[data-testid=\"notifications-delete-read\"]").click();
    page.waitForSelector("#notifications-empty");
    assertEquals(0, page.locator("[data-testid=\"notification\"]").count());
  }

  @Test
  void E02_tresSeguidoresSeVenComoUnGrupoDesplegableYSeMarcanPorFila() {
    // --- arrange: el admin publica un plan y tres usuarios lo siguen ---
    signInAsAdmin();
    new PlansPage(page)
      .createPublicPlanViaExplore(GROUP_PLAN_NAME, GROUP_PLAN_DESCRIPTION, PLAN_DATE);

    String[][] followers = {
      { "Ana", "A", "ana.ngrp@unlam.edu.ar" },
      { "Luis", "B", "luis.ngrp@unlam.edu.ar" },
      { "Marta", "C", "marta.ngrp@unlam.edu.ar" },
    };
    for (String[] follower : followers) {
      newContext();
      WebPage web = new WebPage(page);
      RegisterPage register = new RegisterPage(page);
      register.typeFirstName(follower[0]);
      register.typeLastName(follower[1]);
      register.typeEmail(follower[2]);
      register.typePassword(FOLLOWER_PASSWORD);
      register.typeConfirmPassword(FOLLOWER_PASSWORD);
      register.clickRegister();
      web.waitForPath(LoginRedirects.USER_LANDING);

      String authorPath = authorPathFromFeed(GROUP_PLAN_NAME);
      web.navigate(web.baseUrl() + authorPath);
      page.waitForSelector("#btn-follow:text-is('Seguir')");
      page.locator("#btn-follow").click();
      page.waitForSelector("#btn-follow:text-is('Dejar de seguir')");
    }

    // --- el autor ve un solo item con los tres nombres resumidos ---
    newContext();
    WebPage web = new WebPage(page);
    signInAsAdmin();
    web.navigate(web.baseUrl() + "/notifications");
    Locator group = page.locator("[data-testid=\"notification-group\"]");
    Locator members = page.locator("[data-testid=\"notification-group-member\"]");
    assertEquals(1, group.count());
    assertTrue(group.textContent().contains("Ana y 2 más"));
    assertEquals(3, members.count());
    assertFalse(members.first().isVisible());
    assertEquals(
      "3",
      page.locator("#navbar-notifications").getAttribute("data-notifications-count")
    );

    // --- abrir el grupo marca todas sus filas como leidas y lo deja abierto ---
    page.locator("[data-testid=\"notification-group-toggle\"]").click();
    page.waitForSelector("#navbar-notifications[data-notifications-count=\"0\"]");
    assertTrue(members.first().isVisible());
    assertEquals(
      3,
      page.locator("[data-testid=\"notification-group-member\"][data-read=\"true\"]").count()
    );
    assertEquals(3, page.locator("[data-testid=\"notification-group-member-delete\"]").count());
    assertEquals(1, page.locator("[data-testid=\"notification-group\"]").count());
    assertEquals(0, page.locator("[data-testid=\"notification\"]").count());
    assertEquals(3, page.locator("[data-testid=\"notification-group-member\"]").count());

    // --- el encabezado del grupo borra todas sus filas ---
    page.onDialog(dialog -> dialog.accept());
    page.locator("[data-testid=\"notification-group-delete\"]").click();
    page.waitForSelector("#notifications-empty");
    assertEquals(
      "0",
      page.locator("#navbar-notifications").getAttribute("data-notifications-count")
    );
    assertEquals(0, page.locator("[data-testid=\"notification-group\"]").count());
    assertEquals(0, page.locator("[data-testid=\"notification-group-member\"]").count());
  }

  /** Ruta del perfil del autor, leida de la tarjeta del plan en el feed (como FollowE2E). */
  private String authorPathFromFeed() {
    return authorPathFromFeed(PLAN_NAME);
  }

  private String authorPathFromFeed(String planName) {
    Locator card = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(planName));
    assertEquals(1, card.count());
    return card.locator("a[href*='/profile/']").first().getAttribute("href");
  }
}
