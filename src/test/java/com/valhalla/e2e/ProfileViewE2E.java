package com.valhalla.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.valhalla.e2e.views.WebPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * [PROF] Browser coverage del perfil (docs/specs/11-PROF.md, E-01..E-04): el menú del navbar abre
 * el modal, "Ver perfil" llega a la URL con id, el editor persiste visible con el aviso, la vista
 * pública funciona con y sin sesión, y las entradas "Pronto" no navegan a ningún lado. Extra de
 * esta ronda: la guardia de z-order del modal en /explore, cuya barra sticky tapaba el panel.
 *
 * <p>El admin seed ("Admin Test", inicial A) es la única cuenta garantizada: ResetDatabase corre
 * en cada test y la seed se recarga.
 */
public class ProfileViewE2E extends E2eBase {

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
    page.waitForURL(Pattern.compile(".*/profile/\\d+.*"));
    page.waitForSelector("main h1");
    assertTrue(page.textContent("main h1").contains("Mi perfil"));
  }

  // --- E-03: las entradas "Pronto" están deshabilitadas y NO navegan ---

  @Test
  void soonEntriesAreDisabledAndDoNotNavigate() {
    page.locator("#navbar-avatar").click();
    page.waitForSelector("#navbar-menu:not([hidden])");

    // AC-18: "Pronto" no es <a> ni <button> navegable: un span aria-disabled. El E2E no lo
    // clica (Playwright se niega sobre elementos deshabilitados y ahi esta el punto): fija en
    // el DOM que la entrada no linkea a nada y que las dos "Pronto" siguen inertes.
    var disabledEntries = page.locator("#navbar-menu [role='menuitem'][aria-disabled='true']");
    assertTrue(disabledEntries.count() >= 2);
    assertEquals(
      0,
      page.locator("#navbar-menu a", new Page.LocatorOptions().setHasText("Guardados")).count()
    );

    // El menú sigue abierto y la URL intacta: nada navegó.
    assertTrue(page.locator("#navbar-menu:not([hidden])").count() == 1);
    assertFalse(page.url().contains("/profile"));
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
    page.waitForURL(Pattern.compile(".*/profile/\\d+.*"));
    return page.url();
  }
}
