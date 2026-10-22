package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * [PROF] Browser coverage del perfil (docs/specs/11-PROF.md, E-01..E-04): el menú del navbar abre
 * el modal, "Ver perfil" llega a la URL con id, el editor persiste visible con el aviso, la vista
 * pública funciona con y sin sesión. Extra de esta ronda: la guardia de z-order del modal en
 * /explore, cuya barra sticky tapaba el panel.
 *
 * <p>[MARK] "Guardados" ya no es un "Pronto": es un link activo al ancla del perfil propio.
 *
 * <p>[FOLLOW] E-02 (docs/specs/19-FOLLOW.md): "Siguiendo" ya es un link a `/profile#follow`, y la
 * vista pública muestra los contadores reales con las dos listas de `#follow`.
 *
 * <p>El admin seed ("Admin Test", inicial A) es la única cuenta garantizada: ResetDatabase corre
 * en cada test y la seed se recarga.
 */
public class ProfileViewE2E extends E2eBase {

  private static final String FOLLOWED_FIRST_NAME = "Rosa";
  private static final String FOLLOWED_LAST_NAME = "Seguidora";
  private static final String FOLLOWED_EMAIL = "rosa.seguida@unlam.edu.ar";
  private static final String FOLLOWED_PASSWORD = "secret123";

  @BeforeEach
  void signInAsSeedAdmin() {
    signInAsAdmin();
  }

  // --- E-01: el avatar abre el menú y "Ver perfil" llega al perfil con id en la URL ---

  @Test
  void openingTheMenuAndClickingViewProfileLandsOnTheOwnProfileEditor() {
    WebPage landing = new WebPage(page);
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    page.locator("#menu-view-profile").click();

    // La URL canónica siempre lleva el id (AC-12): /profile es solo el atajo.
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}.*"));
    page.waitForSelector("main h1");
    assertTrue(page.textContent("main h1").contains("Mi perfil"));
  }

  // --- E-03: no quedan entradas "Pronto": cada ancla del modal es un link real ---

  @Test
  void noSoonEntriesRemainInTheMenuAndTheAnchorsAreLinks() {
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    // [MARK] activo "Guardados" y [FOLLOW] activo "Siguiendo": el modal ya no tiene placeholders.
    var disabledEntries = page.locator("#navbar-menu [role='menuitem'][aria-disabled='true']");
    assertEquals(0, disabledEntries.count());

    // [MARK] AC-17: Guardados es un link al ancla de la seccion en el perfil propio.
    var bookmarksLink = page.locator("#navbar-menu a#menu-bookmarks");
    assertEquals(1, bookmarksLink.count());
    assertTrue(bookmarksLink.getAttribute("href").endsWith("/profile#bookmarks"));

    // El menú sigue abierto y la URL intacta: nada navegó.
    assertTrue(page.locator("#navbar-menu:not([hidden])").count() == 1);
    assertFalse(page.url().contains("/profile"));
  }

  // --- E-02 ([FOLLOW] AC-06): "Siguiendo" es navegable y lleva al anchor del perfil ---

  @Test
  void followingEntryInTheMenuNavigatesToTheFollowSection() {
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    var followingEntry = page.locator("#navbar-menu a#menu-following");
    assertEquals(1, followingEntry.count());
    assertTrue(followingEntry.getAttribute("href").endsWith("/profile#follow"));
    // Dejo de ser un span "Pronto": el badge ya no esta dentro de la entrada.
    assertFalse(followingEntry.innerText().contains("Pronto"));

    followingEntry.click();

    // /profile resuelve el id propio con un 302 y el fragmento sobrevive al redirect.
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}#follow"));
    page.waitForSelector("#follow");
    assertTrue(page.locator("#follow").isVisible());
    assertEquals("Seguidores y seguidos", page.textContent("#follow h2").trim());
  }

  // --- E-02 ([FOLLOW] AC-04/AC-05): contadores reales y las dos listas en la vista publica ---

  @Test
  void publicProfileShowsRealFollowCountersAndLists() {
    // Segundo usuario real: ResetDatabase deja solo al admin, asi que el seguido se registra por
    // UI (el mismo patron que el resto del suite) y su URL canonica sale del atajo /profile.
    newContext();
    WebPage web = new WebPage(page);
    registerSecondUser(web);
    String followedProfileUrl = currentOwnProfileUrl();

    // Vuelvo a la sesion del admin para seguir a ese usuario desde su perfil publico.
    new LoginPage(page).clickLogout();
    page.waitForSelector("#btn-login");
    signInAsAdmin();
    page.navigate(followedProfileUrl);
    page.waitForSelector("#btn-follow:text-is('Seguir')");
    page.locator("#btn-follow").click();
    page.waitForSelector("#btn-follow:text-is('Dejar de seguir')");

    // Vista publica del seguido: contadores reales (AC-04) y las dos listas (AC-05).
    assertTrue(page.textContent("#follow").contains("1 seguidores"));
    assertTrue(page.textContent("#follow").contains("0 seguidos"));
    var followersList = page.locator("[data-testid='followers-list']");
    assertEquals(1, followersList.count());
    assertTrue(followersList.innerText().contains("Admin Test"));
    // El seguido no sigue a nadie: su lista vacia muestra su propio mensaje y no se renderiza.
    assertEquals(0, page.locator("[data-testid='following-list']").count());
    assertTrue(page.textContent("#follow").contains("Todavía no sigue a nadie."));
    // AC-12 de [FOLLOW]: la seccion expone nombre, avatar y link, nunca el email del usuario.
    assertFalse(page.textContent("main").contains(FOLLOWED_EMAIL));

    // La otra cara de la misma relacion: el perfil propio (edit.html) lista al seguido.
    page.navigate(web.baseUrl() + "/profile");
    page.waitForSelector("[data-testid='following-list']");
    assertTrue(
      page.locator("[data-testid='following-list']").innerText().contains("Rosa Seguidora")
    );
  }

  // --- z del modal: /explore deja el <nav> sticky con z-index 1000 y el panel queda ENCIMA ---

  @Test
  void theMenuOpensAboveTheStickyNavbarOnExplore() {
    WebPage explore = new WebPage(page);
    page.navigate(explore.baseUrl() + "/explore");
    page.waitForSelector("#navbar-avatar");
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    // Regresion: places/list.html pisa body > nav con position:sticky z-index 1000 (para
    // ganarle a las capas del mapa Leaflet). El modal del menu debe quedarse encima: su
    // z-index computado tiene que superar al del navbar EN ESTA pagina (en las demas el nav
    // no esta posicionado; el bug solo lo reventaba en /explore).
    int navZ = Integer.parseInt(
      (String) page.evaluate("() => getComputedStyle(document.querySelector('nav')).zIndex")
    );
    int menuZ = Integer.parseInt(
      (String) page.evaluate(
        "() => getComputedStyle(document.getElementById('navbar-menu')).zIndex"
      )
    );
    assertTrue(
      menuZ > navZ,
      "el menu (" + menuZ + ") queda tapado por el navbar sticky (" + navZ + ")"
    );
  }

  // --- E-02: la vista pública funciona con sesión ajena y con anonimo (link compartible) ---

  @Test
  void theSharedProfileLinkWorksLoggedOut() {
    String profileUrl = currentOwnProfileUrl();

    // Contexto nuevo = sin sesión: el mismo /profile/{id} sigue mostrando el escaparate.
    newContext();
    page.navigate(profileUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
    page.waitForSelector("main h1");

    assertTrue(page.textContent("main h1").contains("Admin Test"));
    assertTrue(page.textContent("main").contains("seguidores"));
    assertFalse(page.textContent("main").contains("Guardar perfil"));
  }

  // --- E-04: la edición persiste visible con el aviso de éxito ---

  @Test
  void profileEditRoundsTripWithTheSuccessNotice() {
    page.navigate(currentOwnProfileUrl());
    page.waitForSelector("#firstName");

    // Nombres con acentos a proposito: esta es la guardia del fix de charset en
    // BaseWebConfig. La pagina antes salia como iso-8859-1, el browser armaba
    // el body del form en Latin-1 y Jetty respondia 400 "Unable to parse form
    // content: Invalid UTF-8" con cualquier acento (tambien al enviar el form
    // sin tocar nada, porque el prefill ya traia acentos de la DB).
    page.fill("#firstName", "Simón");
    page.fill("#lastName", "Beltrán");
    // La galeria es la via del usuario: el input VISIBLE de URL propia queda vacio (no filtra
    // el path interno) y el valor persistido viaja por el campo oculto #avatarUrl.
    page.locator("main button[data-avatar-value='/images/avatars/avatar-1.svg']").click();
    assertEquals("", page.inputValue("#avatarUrlText"));
    assertEquals("/images/avatars/avatar-1.svg", page.inputValue("#avatarUrl"));
    // El preview no es una burbuja aparte junto a la galeria: el avatar del header es el que
    // refleja la seleccion en vivo (el JS del picker le cambia el src y esconde las iniciales).
    assertEquals(0, page.locator("#avatar-preview").count());
    assertTrue(page.locator("#avatar-preview-img").isVisible());
    assertEquals(
      "/images/avatars/avatar-1.svg",
      page.locator("#avatar-preview-img").getAttribute("src")
    );
    assertFalse(page.locator("#avatar-preview-initial").isVisible());
    page.locator("main button[type='submit']").first().click();

    page.waitForSelector("p[role='status']");
    assertEquals("Perfil actualizado", page.textContent("p[role='status']").trim());
    assertEquals("Simón", page.inputValue("#firstName"));
    assertEquals("Beltrán", page.inputValue("#lastName"));
    // Tras el redirect el avatar de galeria sigue sin mostrarse como path interno.
    assertEquals("", page.inputValue("#avatarUrlText"));
    // El navbar server-rendered del redirect ya refleja el avatar nuevo (sin JS ni cache).
    var navbarAvatar = page.locator("#navbar-avatar img");
    assertEquals(1, navbarAvatar.count());
    assertEquals("/images/avatars/avatar-1.svg", navbarAvatar.getAttribute("src"));
    // Server-rendered con avatar ya guardado: manda la imagen del header y las iniciales quedan
    // ocultas (el mismo par que alterna el JS del picker, pero resuelto en Thymeleaf).
    assertTrue(page.locator("#avatar-preview-img").isVisible());
    assertEquals(
      "/images/avatars/avatar-1.svg",
      page.locator("#avatar-preview-img").getAttribute("src")
    );
    assertFalse(page.locator("#avatar-preview-initial").isVisible());
  }

  /** URL del perfil propio resolviendo el atajo /profile que redirige a /profile/{id}. */
  private String currentOwnProfileUrl() {
    WebPage nav = new WebPage(page);
    page.navigate(nav.baseUrl() + "/profile");
    page.waitForURL(Pattern.compile(".*/profile/[0-9a-f-]{36}.*"));
    return page.url();
  }

  /** Registra al segundo usuario por UI: el alta abre la sesion y aterriza en el feed ([PPV]). */
  private void registerSecondUser(WebPage web) {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName(FOLLOWED_FIRST_NAME);
    register.typeLastName(FOLLOWED_LAST_NAME);
    register.typeEmail(FOLLOWED_EMAIL);
    register.typePassword(FOLLOWED_PASSWORD);
    register.typeConfirmPassword(FOLLOWED_PASSWORD);
    register.clickRegister();
    web.waitForPath(LoginRedirects.USER_LANDING);
  }
}
