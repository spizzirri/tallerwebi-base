package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.PlansPage;
import com.valhalla.e2e.views.UserFormPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of the public plan view: anyone can open it and see the plan data, and a
 * signed-in visitor who is not participating gets the join and clone CTAs.
 *
 * <p>E2eBase wipes the database before every test, so each test creates its own public plan from
 * the UI (create modal on /explore + the public checkbox on the detail page) and then either
 * visits it anonymously or signs in as a fresh non-participant user.
 */
public class PublicPlanViewE2E extends E2eBase {

  private static final String PLAN_NAME = "Viaje publico E2E";
  private static final String PLAN_DESCRIPTION = "Un plan para compartir por link";
  private static final String PLAN_DATE = "2026-12-31";

  @Test
  void T_PVP_007_sinSesionMuestraLosDatosDelPlanPublico() {
    String planUrl = givenAPublicPlanUrl();
    // La polyline del mapa une 2+ paradas (mismo criterio que el detalle), y el plan de explore
    // trae una sola: se agrega otra en la sesion admin que sigue abierta (newContext la cierra
    // recien despues). El id sale de la pagina de detalle abierta, no de la URL publica (que
    // termina en /public y dejaria "public" como id).
    givenASecondStopOn(planIdFrom(page.url()));
    newContext();

    page.navigate(planUrl);

    String body = page.locator("body").innerText();
    assertThat(body, containsString(PLAN_NAME));
    assertThat(body, containsString(PLAN_DESCRIPTION));
    assertThat(body, containsString("31/12/2026"));
    assertThat(page.locator("ol li").count() > 0, is(true));

    // El mapa del itinerario se renderiza con los tiles de OpenStreetMap y marcadores con el
    // estilo del detalle (13-PVP): circulo por categoria, numerado por parada y con la clase
    // in-itinerary (borde amarillo). Con las dos paradas del plan hay linea de itinerario.
    Locator map = page.locator("#plan-map");
    assertThat(map.isVisible(), is(true));
    page.waitForSelector("#plan-map .leaflet-marker-icon");
    assertThat(page.locator("#plan-map .leaflet-tile").count() > 0, is(true));
    assertThat(page.locator("#plan-map .custom-map-icon.in-itinerary").count(), is(2));
    assertThat(
      page.locator("#plan-map .custom-map-icon.in-itinerary").first().innerText(),
      is("1")
    );
    assertThat(page.locator("#plan-map path.leaflet-interactive").count() > 0, is(true));

    // La cabecera firma el plan y dice cuanta gente lo acompana.
    assertThat(body, containsString("Creado por"));
    assertThat(body, containsString("participantes"));

    // El anonimo ve los MISMOS CTAs que el logueado, pero como links al login con la
    // vuelta al plan: no ejecutan join/clone directamente.
    assertThat(page.locator("a:has-text('Unirme al plan')").count(), is(1));
    assertThat(page.locator("a:has-text('Clonar plan')").count(), is(1));
    assertThat(page.locator("button:has-text('Unirme al plan')").count(), is(0));
    assertThat(page.locator("button:has-text('Clonar plan')").count(), is(0));
    assertThat(
      page.locator("a:has-text('Unirme al plan')").getAttribute("href"),
      containsString("/auth/login?redirect=")
    );
  }

  @Test
  void T_PVP_007_elCtaAnonimoLlevaAlLoginYVuelveAlPlan() {
    String planUrl = givenAPublicPlanUrl();
    String password = givenANormalUser("carla@unlam.edu.ar");
    newContext();

    page.navigate(planUrl);
    page.locator("a:has-text('Clonar plan')").click();

    // Aterriza en el login conservando a donde volver.
    page.waitForSelector("input[type='email']");
    assertThat(page.url(), containsString("/auth/login?redirect="));

    // Se rellena el form YA CARGADO (el que abrio el CTA): new LoginPage(page) navega a
    // /admin/login y perderia el redirect, que es justo lo que este test verifica.
    page.fill("#email", "carla@unlam.edu.ar");
    page.fill("#password", password);
    page.click("#btn-login");

    // Termina en la vista publica del mismo plan, ahora con los CTAs de logueado.
    page.waitForSelector("button:has-text('Clonar plan')");
    assertThat(page.locator("button:has-text('Unirme al plan')").count(), is(1));
  }

  @Test
  void T_PVP_007_conSesionDeNoParticipanteMuestraUnirseYClonar() {
    String planUrl = givenAPublicPlanUrl();
    String password = givenANormalUser("juan@unlam.edu.ar");
    newContext();

    LoginPage login = new LoginPage(page);
    login.typeEmail("juan@unlam.edu.ar");
    login.typePassword(password);
    login.clickSignIn();
    // Contra la constante de landing, no un literal: tolera el cambio de landing (p. ej. /for-you)
    login.waitForPath(LoginRedirects.USER_LANDING);

    page.navigate(planUrl);

    String body = page.locator("body").innerText();
    assertThat(body, containsString("Unirme al plan"));
    assertThat(body, containsString("Clonar plan"));
    // Con sesion son forms que ejecutan join/clone, no links al login.
    assertThat(page.locator("button:has-text('Unirme al plan')").count(), is(1));
    assertThat(page.locator("a:has-text('Clonar plan')").count(), is(0));
  }

  private String givenAPublicPlanUrl() {
    signInAsAdmin();
    PlansPage plans = new PlansPage(page);
    plans.createPlanViaExplore(PLAN_NAME, PLAN_DESCRIPTION, PLAN_DATE);
    plans.waitForDetailPath();

    String planId = planIdFrom(page.url());
    makePlanPublic(plans);

    return plans.baseUrl() + "/plans/" + planId + "/public";
  }

  private static String planIdFrom(String url) {
    return URI.create(url).getPath().replaceAll(".*/", "");
  }

  /**
   * Adds a second stop through the API while the admin session is still open (05-APL-FE owns the
   * add-to-plan UI, so the backend is exercised via fetch — same pattern as AddPlaceToPlanE2E).
   * Picks a place the itinerary does not have yet, since the create modal already added the first
   * catalog place. The whole seeded catalog has coordinates, so the polyline test gets two stops
   * that qualify.
   */
  private void givenASecondStopOn(String planId) {
    Object stopCount = page.evaluate(
      """
      async planId => {
        const tokenEl = document.querySelector('meta[name="csrf-token"]') || document.querySelector('input[name="_csrf"]');
        const headerEl = document.querySelector('meta[name="csrf-header"]');
        const token = tokenEl?.content || tokenEl?.value || '';
        const header = headerEl?.content || 'X-CSRF-TOKEN';
        if (!token) throw new Error('Missing CSRF token');
        const url = '/api/plans/' + planId + '/places';
        const present = new Set((await (await fetch(url)).json()).map(e => e.placeId));
        const catalog = (await (await fetch('/api/places')).json()).filter(p => !present.has(p.id));
        if (catalog.length === 0) throw new Error('No extra place in catalog');
        const added = await fetch(url + '?placeId=' + catalog[0].id, {
          method: 'POST',
          headers: { [header]: token }
        });
        if (!added.ok) {
          const body = await added.text().catch(() => '');
          throw new Error('Add failed: ' + added.status + ' ' + body);
        }
        return (await (await fetch(url)).json()).length;
      }
      """,
      planId
    );
    assertEquals(2, ((Number) stopCount).intValue());
  }

  private void makePlanPublic(PlansPage plans) {
    // The editor submits the itinerary rows from Vue, which mount after the page loads.
    // The row inputs are type=hidden, so wait for attached rather than for visible.
    page
      .locator("input[name='places[0].placeId']")
      .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator("#isPublic").check();
    page.locator("button:has-text('Guardar cambios')").click();
    plans.waitForDetailPath();
  }

  private String givenANormalUser(String email) {
    // The arrange that made the plan public left the admin session open, and /admin/login
    // bounces an already signed-in user to its landing instead of rendering the form.
    newContext();
    LoginPage adminLogin = signInAsAdmin();
    adminLogin.navigate(adminLogin.baseUrl() + "/admin/users/new");
    UserFormPage form = new UserFormPage(page);
    form.typeFirstName("Juan");
    form.typeLastName("Perez");
    form.typeEmail(email);
    form.selectRole("USER");
    form.clickCreate();
    form.waitForPath("/admin/users");
    return page.locator("code").textContent();
  }
}
