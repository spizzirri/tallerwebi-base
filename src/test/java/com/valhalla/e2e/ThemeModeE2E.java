package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.valhalla.e2e.views.WebPage;
import org.junit.jupiter.api.Test;

/**
 * El toggle de modo oscuro del navbar, que existe solo con sesión iniciada. Cubre el contrato
 * completo en browser: el botón cambia los dos atributos que encienden la capa oscura
 * (data-theme, que lee brand.css, y data-bs-theme, que lee Bootstrap en detail.html), la página
 * se repinta de verdad —el body pasa del gris-100 claro a la superficie oscura— y la elección
 * sobrevive al reload y a una sesión nueva porque el toggle POSTea a /profile/theme y la
 * preferencia queda en el usuario. Un toggle que solo escribiera localStorage se revertiría en
 * la carga siguiente: el meta del servidor manda sobre el storage.
 *
 * <p>El contexto de Playwright arranca con colorScheme light: sin decisión guardada el sistema
 * dice "claro", así que el resultado no depende del sistema de la máquina que corre el test.
 */
public class ThemeModeE2E extends E2eBase {

  private static final String TOGGLE = "body > nav button[data-theme-toggle]";
  private static final String DARK_BODY = "rgb(3, 7, 18)";
  private static final String LIGHT_TEXT = "rgb(249, 250, 251)";

  @Test
  void theThemeToggleRepaintsThePageAndTheChoiceSurvivesAReload() {
    // El toggle existe solo con sesion: el anonimo no tiene control de tema. La sesion del seed
    // es la unica garantizada (openContext borra la base y recarga la seed).
    signInAsAdmin();
    WebPage web = new WebPage(page);
    web.navigate(web.baseUrl() + "/");
    waitUntilTheBodyIsPainted();

    assertThat("arranca en claro", page.locator("html").getAttribute("data-theme"), is("light"));
    assertThat(
      "y Bootstrap recibe el mismo valor",
      page.locator("html").getAttribute("data-bs-theme"),
      is("light")
    );
    // El gris-100 claro es de Tailwind (v4 lo declara en oklch, y su valor es asunto suyo):
    // lo que nosotros garantizamos es que el claro original no sea el oscuro y que el toggle
    // lo devuelva tal cual.
    String lightSurface = bodyBackground();
    String lightText = bodyTextColor();
    assertThat("arranca en la superficie clara de Tailwind", lightSurface, not(is(DARK_BODY)));

    // El click dispara el submit nativo del form y el POST a /profile/theme: Playwright espera
    // la navegacion de vuelta al origen antes de que sigan las aserciones.
    page.click(TOGGLE);
    waitUntilTheBodyIsPainted();

    assertThat(
      "el click enciende la capa oscura",
      page.locator("html").getAttribute("data-theme"),
      is("dark")
    );
    assertThat(page.locator("html").getAttribute("data-bs-theme"), is("dark"));
    assertThat("los overrides de brand.css repintan el body", bodyBackground(), is(DARK_BODY));
    // El preflight de Tailwind no declara color de texto: lo sin clase text-* hereda el
    // negro del navegador. El dark fija el default en html y este body lo hereda.
    assertThat("el texto por defecto pasa a claro", bodyTextColor(), is(LIGHT_TEXT));

    page.reload();
    waitUntilTheBodyIsPainted();

    assertThat(
      "la eleccion viaja server-side y sobrevive al reload",
      page.locator("html").getAttribute("data-theme"),
      is("dark")
    );
    assertThat(bodyBackground(), is(DARK_BODY));

    // Contexto nuevo = otro dispositivo: sin localStorage ni cookies. Al loguearse, el meta que
    // emite el servidor devuelve el modo oscuro sin depender del storage local. Esa es la razon
    // por la que el toggle tiene que POSTear y no escribir solo localStorage.
    newContext();
    signInAsAdmin();
    web = new WebPage(page);
    web.navigate(web.baseUrl() + "/");
    waitUntilTheBodyIsPainted();
    assertThat(
      "la preferencia del usuario sobrevive en una sesion nueva",
      page.locator("html").getAttribute("data-theme"),
      is("dark")
    );

    page.click(TOGGLE);
    waitUntilTheBodyIsPainted();

    assertThat(
      "el segundo click vuelve al claro",
      page.locator("html").getAttribute("data-theme"),
      is("light")
    );
    assertThat("y al valor exacto de antes", bodyBackground(), is(lightSurface));
    assertThat("y al texto del navegador de antes", bodyTextColor(), is(lightText));
  }

  /**
   * El body arranca transparente hasta que Tailwind inyecta las utilidades compiladas (el mismo
   * criterio que usa el reveal de layouts/base). Medir el color antes de ese estilo sería medir
   * la página sin compilar.
   *
   * <p>El toggle es un submit: el click navega a /profile/theme y vuelve, y el predicate puede
   * caer en el documento nuevo antes de que exista su body. Sin el guard, getComputedStyle(null)
   * lanza TypeError en vez de devolver false y reintentar.
   */
  private void waitUntilTheBodyIsPainted() {
    page.waitForFunction(
      "() => document.body !== null && getComputedStyle(document.body).backgroundColor !== 'rgba(0, 0, 0, 0)'"
    );
  }

  private String bodyBackground() {
    return (String) page.evaluate("() => getComputedStyle(document.body).backgroundColor");
  }

  private String bodyTextColor() {
    return (String) page.evaluate("() => getComputedStyle(document.body).color");
  }
}
