package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;

import com.microsoft.playwright.Locator;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.WebPage;
import org.junit.jupiter.api.Test;

/**
 * [PPV] E-01 / T-PPV-005: el feed de planes publicos es para usuarios con sesion.
 *
 * <p>Cubro las dos caras del contrato de producto. Sin sesion: el landing no ofrece el boton
 * "Planes públicos" en el navbar y entrar directo a /plans/public aterriza en el login. Con
 * sesion: el navbar lo ofrece, la tarjeta rinde sus datos con el href a la vista publica del plan
 * (AC-04), "Usar plan" clona el plan como copia propia y "Editar" aparece al lado del plan que es
 * tuyo.
 *
 * <p>E2eBase limpia la base antes de cada test, asi que el unico plan publico lo crea la sesion
 * del arrange. El destino {@code /plans/{id}/public} lo renderiza [PVP]: hasta que entre, el href
 * de la tarjeta es el contrato del destino (AC-04) y no se navega a el. Cuando PVP llegue, este
 * test puede empezar a clickearlo y esperar {@code /plans/\d+/public}.
 *
 * <p>El nombre del plan es ASCII a proposito, no por estetica: la app responde las paginas como
 * {@code text/html;charset=iso-8859-1}, asi que el browser arma el body del form en Latin-1 y
 * Jetty lo rechaza con 400 "Unable to parse form content: Invalid UTF-8" en cuanto un campo trae
 * un acento. Ese bug de codificacion es preexistente y le toca a la card de creacion de planes,
 * no a esta: agregarle un acento aca volveria rojo este E2E por un motivo que PPV no controla.
 */
public class PublicPlansListE2E extends E2eBase {

  private static final String PLAN_NAME = "Plan publico E2E";
  private static final String PLAN_DESCRIPTION = "Un plan para compartir con todos";

  @Test
  void T_PPV_005_visitanteNoVeElFeedYElUsuarioLogueadoLoUsaYLoEdita() {
    WebPage web = new WebPage(page);

    // --- Sin sesion: el boton no existe en el landing (decision de producto) ---
    web.navigate(web.baseUrl() + "/");
    assertThat(
      "el navbar sin sesion no ofrece el feed",
      web.getNavbarItems(),
      contains("PlanIt", "Register", "Login")
    );

    // ...y entrar directo a la ruta cae en login.
    web.navigate(web.baseUrl() + "/plans/public");
    web.waitForPath("/auth/login");

    // --- Con sesion: el feed es el home y sus tarjetas funcionan ---
    givenAPublicPlanExists(); // deja la sesion admin sobre el detalle del plan creado

    Locator navLink = page.locator("body > nav a:has-text('Planes públicos')");
    assertThat("el navbar con sesion ofrece el feed", navLink.count(), is(1));
    navLink.click();
    web.waitForPath("/plans/public");

    // La tarjeta del plan con todos sus datos (AC-03) y el boton "Usar plan".
    Locator card = page
      .locator("main article")
      .filter(new Locator.FilterOptions().setHasText(PLAN_NAME));
    assertThat(card.count(), is(1));
    String cardText = card.innerText();
    assertThat(cardText, containsString(PLAN_DESCRIPTION));
    assertThat(cardText, containsString("lugar(es) en el itinerario"));
    // AC-09: el seed del E2E reinserta al admin sin nombre, y aun asi la tarjeta no filtra su email.
    assertThat(cardText, not(containsString("test@unlam.edu.ar")));
    assertThat(card.locator("button:text-is('Usar plan')").count(), is(1));

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
    assertThat(href, matchesPattern(".*/plans/\\d+/public"));
    String sourceId = href.replaceAll(".*/plans/(\\d+)/public", "$1");

    // El plan es del usuario logueado: su tarjeta ofrece Editar al lado de Usar plan.
    assertThat(card.locator("a:has-text('Editar')").count(), is(1));

    // "Usar plan" clona el plan: caigo en MI copia, con id distinto al original.
    card.locator("button:text-is('Usar plan')").click();
    new PlansPage(page).waitForDetailPath();
    String copyId = page.url().replaceAll(".*/plans/(\\d+)(?:[?#].*)?$", "$1");
    assertThat("la copia tiene un id propio", copyId, not(is(sourceId)));
    assertThat(new PlansPage(page).getDetailHeading(), containsString(PLAN_NAME));
  }

  /** Crea el plan publico con la sesion admin y la deja abierta sobre su detalle. */
  private void givenAPublicPlanExists() {
    signInAsAdmin();
    new PlansPage(page).createPublicPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, "2026-12-31");
  }
}
