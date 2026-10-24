package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.RegisterPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import org.junit.jupiter.api.Test;

/**
 * [PPV] E-01 / T-PPV-005: el feed "Para vos" ({@code GET /for-you}) es visible con y sin sesion.
 *
 * <p>Cubro las tres caras del contrato de producto. Sin sesion: el navbar ofrece "Para vos", entrar
 * directo a /for-you rinde el feed (no rebota en el login) y el CTA "Clonar Plan" de la tarjeta es
 * un link al login con la vuelta. Con sesion: el navbar lo ofrece, la tarjeta rinde sus datos con
 * el href a la vista publica del plan (AC-04) y "Clonar Plan" copia el plan como propia. El plan
 * propio del logueado nunca aparece: el feed es para descubrir planes de otros.
 *
 * <p>E2eBase limpia la base antes de cada test, asi que el unico plan publico lo crea la sesion
 * del arrange, que es el admin: por eso el observador del feed es un usuario recien registrado en
 * un contexto aparte, para quien el plan del admin es ajeno.
 *
 * <p>El nombre del plan es ASCII a proposito, por estilo del suite: la app ahora responde las
 * paginas como {@code text/html;charset=UTF-8} (charset fijado en BaseWebConfig; hasta ese fix
 * las paginas salian iso-8859-1, el browser armaba el body del form en Latin-1 y Jetty lo
 * rechazaba con 400 "Unable to parse form content: Invalid UTF-8" al primer acento).
 */
public class PublicPlansListE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan publico E2E";
  private static final String PLAN_DESCRIPTION = "Un plan para compartir con todos";
  private static final String EMAIL = "visitante.ppv@unlam.edu.ar";
  private static final String PASSWORD = "secret123";

  @Test
  void T_PPV_005_visitanteVeElFeedSinSesionYUnNuevoUsuarioLoClona() {
    WebPage web = new WebPage(page);

    // --- Sin sesion: el navbar ofrece el feed como "Para vos" ---
    web.navigate(web.baseUrl() + "/");
    assertThat(
      "el navbar sin sesion ofrece el feed",
      web.getNavbarItems(),
      contains("PlanIt", "Para vos", "Register", "Login", "Cambiar tema")
    );
    assertThat(
      "el link del navbar sin sesion apunta a /for-you",
      page.locator("body > nav a:has-text('Para vos')").getAttribute("href"),
      containsString("/for-you")
    );
    assertThat(
      "el navbar sin sesion no ofrece el menu hamburguesa",
      page.locator("#navbar-menu-btn").count(),
      is(0)
    );

    // --- El plan publico lo arma el admin, y EL NO LO VE en su propio feed ---
    givenAPublicPlanExists(); // deja la sesion admin sobre el detalle del plan creado
    web.navigate(web.baseUrl() + LoginRedirects.USER_LANDING);
    assertThat(
      "el feed no lista planes propios (publicos o privados)",
      page.locator("main article").count(),
      is(0)
    );
    assertThat(page.locator("main").innerText(), containsString("Todavía no hay planes públicos"));

    // --- Sin sesion, directo a la ruta: el feed rinde el listado, sin redirect a login ---
    newContext();
    web = new WebPage(page);
    web.navigate(web.baseUrl() + "/for-you");
    web.waitForPath("/for-you");

    Locator anonCard = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat("el visitante ve el plan publico en el feed", anonCard.count(), is(1));
    // El CTA anónimo es un link al login con la vuelta, no un form POST que se pierde.
    assertThat(anonCard.locator("a:has-text('Clonar Plan')").count(), is(1));
    assertThat(anonCard.locator("button:has-text('Clonar Plan')").count(), is(0));
    assertThat(
      anonCard.locator("a:has-text('Clonar Plan')").getAttribute("href"),
      containsString("/auth/login?redirect=")
    );

    // --- Con sesion de OTRO usuario: el feed es el home y sus tarjetas funcionan ---
    newContext();
    givenARegisteredUserOpensFeed();
    web = new WebPage(page);

    // El navbar con sesion ofrece el feed como "Para vos", una unica vez.
    Locator navLink = page.locator("body > nav a:has-text('Para vos')");
    assertThat("el navbar con sesion ofrece el feed", navLink.count(), is(1));
    navLink.click();
    web.waitForPath(LoginRedirects.USER_LANDING);

    // La tarjeta del plan con todos sus datos (AC-03) y el boton "Clonar Plan".
    Locator card = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat(card.count(), is(1));
    String cardText = card.innerText();
    assertThat(cardText, containsString(PLAN_DESCRIPTION));
    assertThat(cardText, containsString("lugar(es) en el itinerario"));
    // AC-09: el seed del E2E reinserta al admin sin nombre, y aun asi la tarjeta no filtra su email.
    assertThat(cardText, not(containsString("test@unlam.edu.ar")));
    assertThat(card.locator("button:text-is('Clonar Plan')").count(), is(1));
    // El feed no gestiona planes propios: ningun CTA de edicion en la tarjeta.
    assertThat(card.locator("a:has-text('Editar')").count(), is(0));

    // El cover visual de la tarjeta: imagen generica del plan, alt accesible y que SI carga.
    Locator cover = card.locator("img").first();
    assertThat(cover.getAttribute("src"), matchesPattern(".*/images/plans/cover-\\d\\.svg"));
    assertThat(cover.getAttribute("alt"), is(PLAN_NAME));
    page.waitForFunction(
      "() => { const image = document.querySelector(\"main article img\"); " +
      "return image !== null && image.complete && image.naturalWidth > 0; }"
    );

    // AC-04: la tarjeta linkea a la vista publica del plan, nunca al detalle protegido.
    String href = card.locator("a").first().getAttribute("href");
    assertThat(href, matchesPattern(".*/plans/[0-9a-f-]{36}/public"));
    String sourceId = href.replaceAll(".*/plans/([0-9a-f-]{36})/public", "$1");

    // "Clonar Plan" clona el plan: caigo en MI copia, con id distinto al original.
    card.locator("button:text-is('Clonar Plan')").click();
    new PlansPage(page).waitForDetailPath();
    String copyId = page.url().replaceAll(".*/plans/([0-9a-f-]{36})(?:[?#].*)?$", "$1");
    assertThat("la copia tiene un id propio", copyId, not(is(sourceId)));
    assertThat(new PlansPage(page).getDetailHeading(), containsString(PLAN_NAME));
  }

  /**
   * [DIS] T-DIS-04 (23-DIS): "Por {autor}" del feed en una sola linea. La autora del plan tiene
   * nombre (el seed del E2E reinserta al admin sin nombre, por eso el arrange registra una
   * usuaria), la linea renderiza, y "Por" y el link del autor comparten linea: el link trunca
   * con ellipsis computado y el title lleva el nombre completo (accesibilidad del truncado).
   */
  @Test
  void T_DIS_04_porYAutorCompartenLineaEnElFeed() {
    WebPage web = new WebPage(page);

    // --- Arrange: una usuaria registrada (con nombre y apellido) publica un plan publico ---
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Veronica");
    register.typeLastName("Visitante");
    register.typeEmail("autora.dis@unlam.edu.ar");
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    web.waitForPath(LoginRedirects.USER_LANDING);
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, "2026-12-31");

    // --- Act: OTRA sesion mira el feed y encuentra la tarjeta con su autora ---
    newContext();
    givenARegisteredUserOpensFeed();
    web = new WebPage(page);
    Locator card = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat("el feed muestra el plan de la autora", card.count(), is(1));

    // --- Assert: una sola linea, truncado accesible ---
    Locator authorLine = card.locator("[data-testid='plan-card-author']");
    assertThat("la linea del autor renderiza (la autora tiene nombre)", authorLine.count(), is(1));
    Locator por = authorLine.locator("span").first();
    Locator authorLink = authorLine.locator("a");
    assertThat(
      "el nombre linkea al perfil publico de la autora",
      authorLink.getAttribute("href"),
      matchesPattern(".*/profile/[0-9a-f-]{36}")
    );
    assertThat(
      "el title del link lleva el nombre completo (hover y lectura de pantalla)",
      authorLink.getAttribute("title"),
      containsString("Veronica")
    );
    double porTop = por.boundingBox().y;
    double linkTop = authorLink.boundingBox().y;
    assertThat(
      "'Por' y el nombre comparten linea (no se parten en dos renglones)",
      Math.abs(porTop - linkTop) < 1.0,
      is(true)
    );
    Object textOverflow = authorLink.evaluate("el => getComputedStyle(el).textOverflow");
    assertThat("el nombre largo trunca con elipsis", String.valueOf(textOverflow), is("ellipsis"));
  }

  /** Crea el plan publico con la sesion admin y la deja abierta sobre su detalle. */
  private void givenAPublicPlanExists() {
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, "2026-12-31");
  }

  /** Registra un usuario limpio en el contexto nuevo: la registration abre la sesion y aterriza en el feed. */
  private void givenARegisteredUserOpensFeed() {
    RegisterPage register = new RegisterPage(page);
    register.typeFirstName("Veronica");
    register.typeLastName("Visitante");
    register.typeEmail(EMAIL);
    register.typePassword(PASSWORD);
    register.typeConfirmPassword(PASSWORD);
    register.clickRegister();
    // Contra la constante de landing, no un literal: tolera el cambio de landing sin reescribir.
    new WebPage(page).waitForPath(LoginRedirects.USER_LANDING);
  }
}
