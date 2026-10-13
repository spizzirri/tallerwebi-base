package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.UserFormPage;
import com.valhalla.e2e.views.WebPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of the branding (AC-01, AC-02, AC-05). Both navbar states are exercised
 * because the brand anchor has two mutually exclusive branches: the admin one and the one every
 * other visitor sees.
 *
 * <p>The token guarantees themselves are pinned by
 * {@code com.valhalla.presentation.brand.BrandTokensTest}; this only proves the browser got
 * what the server sent.
 */
public class BrandingViewE2E extends E2eBase {

  private static final String BRAND = "PlanIt";

  // E-01: brand y favicon como ADMIN.
  @Test
  void shouldShowTheBrandAndTheFaviconToAnAdmin() {
    givenAdminIsSignedIn();

    thenShouldSeeTheBrandInTheNavbar();
    thenShouldSeeTheNavbarPaintedInBlack();
    thenShouldSeeTheBodyInTheSansToken();
    thenShouldSeeBothTypefacesResolve();
    thenShouldSeeTheFavicon();
  }

  // E-01: brand y favicon como USER. El alta la hace el admin, igual que en LoginViewE2E: el
  // registro público ya no existe, así que el camino real hacia un USER pasa por /admin/users.
  @Test
  void shouldShowTheBrandAndTheFaviconToAUser() {
    String generatedPassword = givenAdminCreatesUser("branding.user@unlam.edu.ar");

    LoginPage login = new LoginPage(page, "/auth/login");
    login.typeEmail("branding.user@unlam.edu.ar");
    // El alta genera la clave; el panel la muestra una sola vez en /admin/users.
    login.typePassword(generatedPassword);
    login.clickSignIn();
    // USER landing: LoginRedirects sends a signed-in USER to USER_LANDING, never to "/".
    new WebPage(page).waitForPath(LoginRedirects.USER_LANDING);

    WebPage landing = new WebPage(page);
    assertThat("the user sees the brand too", landing.getNavbarBrand(), equalToIgnoringCase(BRAND));
    assertThat(
      "the brand is the product name, not the institution",
      landing.getNavbarBrand(),
      not(containsString("UNLAM"))
    );
    thenShouldSeeTheBrandPaintedInTheTokenColor();
    thenShouldSeeTheNavbarPaintedInBlack();
    thenShouldSeeTheBodyInTheSansToken();
    thenShouldSeeBothTypefacesResolve();
    thenShouldSeeTheFavicon();
  }

  /**
   * La serif de marca tiene que verse en pantalla, no quedar declarada y sin usar. Se mide el
   * nombre de la webfont en el stack computado, que prueba la declaración: si la descarga
   * fallara, el stack seguiría diciendo Bree Serif con el Georgia detrás y este assert
   * no lo detectaría.
   */
  @Test
  void shouldWearTheSerifOnTheBrandLockup() {
    WebPage anonymous = new WebPage(page);
    anonymous.navigate(anonymous.baseUrl() + "/");

    String wordmark = (String) page
      .locator("nav a.navbar-brand")
      .first()
      .evaluate("el => getComputedStyle(el).fontFamily");
    assertThat("el wordmark del navbar lleva la serif", wordmark, containsString("Bree Serif"));

    String hero = (String) page
      .locator("main h1")
      .first()
      .evaluate("el => getComputedStyle(el).fontFamily");
    assertThat("el h1 de la landing lleva la serif", hero, containsString("Bree Serif"));
  }

  /**
   * Josefin Sans es la tipografía de todo el cuerpo de texto, y llega sin que ninguna plantilla la
   * pida: el preflight de Tailwind resuelve {@code --default-font-family} a {@code
   * --font-sans} y lo aplica al documento. Ese camino tiene dos formas de romperse en
   * silencio —que el mapeo de {@code @theme} deje de emitir la variable, o que el preflight
   * deje de consumirla— y en las dos la app se ve con la system font sin que ningún otro
   * test se entere. Se mide el cuerpo, no la declaración.
   *
   * <p>Como en el caso de la serif, el stack computado prueba que la webfont está declarada,
   * no que se haya descargado: si Google Fonts no responde, el nombre sigue ahí con el
   * respaldo del sistema detrás.
   */
  @Test
  void shouldSetTheBodyInTheBrandSansSerif() {
    WebPage anonymous = new WebPage(page);
    anonymous.navigate(anonymous.baseUrl() + "/");

    String body = (String) page.evaluate("() => getComputedStyle(document.body).fontFamily");
    assertThat("el cuerpo de texto usa Josefin Sans", body, containsString("Josefin Sans"));
  }

  /**
   * El wordmark es texto secondary (#6681fc) sobre el navbar negro: 5.16:1 con los tokens
   * actuales. Eso alcanza solo si el texto cuenta como grande, y a 18px/600 no cuenta. El
   * assert mide las dos mitades por separado —tamaño y peso, que deciden el umbral, y el
   * ratio real— para que volver a achicar el wordmark no pueda hacer pasar el test con el
   * umbral viejo.
   */
  @Test
  void shouldKeepTheNavbarWordmarkReadableOnBlack() {
    WebPage anonymous = new WebPage(page);
    anonymous.navigate(anonymous.baseUrl() + "/");

    @SuppressWarnings("unchecked")
    Map<String, Object> measured = (Map<String, Object>) page.evaluate(
      "() => {" +
      "  const nav = document.querySelector('nav');" +
      "  const brand = nav.querySelector('a.navbar-brand');" +
      "  const style = getComputedStyle(brand);" +
      "  const channels = rgb => rgb.match(/[0-9.]+/g).slice(0, 3).map(Number);" +
      "  const linear = c => { c /= 255;" +
      "    return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4); };" +
      "  const luminance = rgb => {" +
      "    const [r, g, b] = channels(rgb).map(linear);" +
      "    return 0.2126 * r + 0.7152 * g + 0.0722 * b; };" +
      "  const foreground = luminance(style.color);" +
      "  const background = luminance(getComputedStyle(nav).backgroundColor);" +
      "  const [high, low] = foreground > background" +
      "    ? [foreground, background] : [background, foreground];" +
      "  return {" +
      "    ratio: (high + 0.05) / (low + 0.05)," +
      "    fontSize: parseFloat(style.fontSize)," +
      "    fontWeight: parseInt(style.fontWeight, 10)" +
      "  };" +
      "}"
    );

    double ratio = ((Number) measured.get("ratio")).doubleValue();
    double fontSize = ((Number) measured.get("fontSize")).doubleValue();
    int fontWeight = ((Integer) measured.get("fontWeight"));

    // WCAG 1.4.3: 14pt bold (18.66px a peso 700) ya es texto grande, umbral 3:1 en vez de 4.5:1.
    org.junit.jupiter.api.Assertions.assertTrue(
      fontSize >= 18.66,
      "el wordmark debe medir al menos 18.66px para el umbral de texto grande, mide " + fontSize
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      700,
      fontWeight,
      "el peso tiene que ser bold; a menor peso el umbral sube a 4.5:1"
    );
    org.junit.jupiter.api.Assertions.assertTrue(
      ratio >= 3.0,
      "contraste del wordmark " + ratio + ":1, por debajo del 3:1 que le corresponde"
    );
  }

  /**
   * Los scrapers de Open Graph no resuelven rutas relativas: sin host, la imagen no se
   * descarga y el link se comparte sin miniatura. Solo un servidor real tiene host y puerto,
   * así que el prefijo sale del bean siteOrigin de scope de request.
   */
  @Test
  void shouldPublishTheSharePreviewWithAbsoluteUrls() {
    WebPage anonymous = new WebPage(page);
    anonymous.navigate(anonymous.baseUrl() + "/");

    // Open Graph se declara con property= y Twitter con name=.
    for (String[] tag : new String[][] {
      { "property", "og:image" },
      { "name", "twitter:image" },
    }) {
      String url = page
        .locator("meta[" + tag[0] + "='" + tag[1] + "']")
        .first()
        .getAttribute("content");
      org.junit.jupiter.api.Assertions.assertTrue(
        url != null && Pattern.matches("https?://[^/]+:\\d+/images/og-image\\.png", url),
        tag[1] + " tiene que ser una URL absoluta, llegó: " + url
      );
    }

    String card = page.locator("meta[name='twitter:card']").first().getAttribute("content");
    assertThat("la tarjeta pide la imagen grande", card, equalTo("summary_large_image"));
  }

  // AC-02: ningún título renderizado dice el nombre del repo.
  @Test
  void shouldNotTitleAnyPageWithTheRepoName() {
    WebPage anonymous = new WebPage(page);
    anonymous.navigate(anonymous.baseUrl() + "/auth/login");

    String title = anonymous.getTitle();
    assertThat(title, containsString(BRAND));
    assertThat(title, not(containsString("Valhalla")));
  }

  private void thenShouldSeeTheBrandInTheNavbar() {
    WebPage current = new WebPage(page);

    assertThat(
      "the navbar brand is the product name",
      current.getNavbarBrand(),
      equalToIgnoringCase(BRAND)
    );
    thenShouldSeeTheBrandPaintedInTheTokenColor();
  }

  /**
   * The markup assertion above passes even if the utility generates nothing, because the text
   * is there either way. This is the one that proves the @theme block reached the browser
   * build: it resolves through brand.css to --brand-primary, and the browser reports the
   * value it actually painted.
   */
  private void thenShouldSeeTheBrandPaintedInTheTokenColor() {
    String color = (String) page
      .locator("nav a.navbar-brand")
      .first()
      .evaluate("el => getComputedStyle(el).color");

    assertThat(
      "the brand text wears the --secondary token (#6681fc)",
      color,
      equalTo("rgb(102, 129, 252)")
    );
  }

  /** El navbar pide el fondo con la utilidad bg-black, así que --black tiene que pintar. */
  private void thenShouldSeeTheNavbarPaintedInBlack() {
    String background = (String) page
      .locator("nav")
      .first()
      .evaluate("el => getComputedStyle(el).backgroundColor");

    assertThat(
      "the navbar background wears the --black token (#111827)",
      background,
      equalTo("rgb(17, 24, 39)")
    );
  }

  /**
   * body tiene que usar la sans del token y no la serif. El assert mira el nombre de la
   * webfont, no el fallback: con la pila del sistema atrás, un assert sobre system-ui
   * pasaría aunque la webfont nunca cargara.
   */
  private void thenShouldSeeTheBodyInTheSansToken() {
    String fontFamily = (String) page
      .locator("body")
      .first()
      .evaluate("el => getComputedStyle(el).fontFamily");

    assertThat("body wears --typeface-sans", fontFamily, containsString("Josefin Sans"));
    assertThat(
      "body does not fall back to the serif",
      fontFamily,
      not(containsString("Bree Serif"))
    );
  }

  /**
   * La serif tiene que quedar disponible como utilidad de Tailwind y no solo como token
   * muerto. Si el browser build no procesa el bloque @theme, font-serif no genera nada
   * y el assert falla aunque la token exista en la hoja.
   */
  private void thenShouldSeeBothTypefacesResolve() {
    // Se comprueba el token, no la utilidad. @theme solo emite una variable en :root si
    // algo generado la referencia: --font-sans entra porque el preflight la usa
    // (tailwind-browser.js:933, --default-font-family), mientras que --font-serif no la
    // referencia nadie hasta que una plantilla escriba class="font-serif". Medir la
    // utilidad hoy mide un token que Tailwind todavia no emitio, y falla sin explicar por
    // que. Cuando alguien use font-serif de verdad, la regla se genera y el token aparece.
    String resolved = (String) page.evaluate(
      "() => {" +
      "  const root = getComputedStyle(document.documentElement);" +
      "  return root.getPropertyValue('--typeface-sans') + ' | ' +" +
      "         root.getPropertyValue('--typeface-serif');" +
      "}"
    );

    assertThat("la sans resuelve a la webfont", resolved, containsString("Josefin Sans"));
    assertThat("la serif resuelve a la webfont", resolved, containsString("Bree Serif"));
  }

  // AC-05: el navegador resuelve el favicon de verdad, no solo lo encuentra en el markup.
  private void thenShouldSeeTheFavicon() {
    String href = page.locator("link[rel='icon']").first().getAttribute("href");

    assertThat("the page declares a favicon", href, not(equalTo(null)));
    org.junit.jupiter.api.Assertions.assertTrue(
      Pattern.matches(".*/images/favicon\\.svg", href),
      "unexpected favicon href: " + href
    );
  }

  private void givenAdminIsSignedIn() {
    signInAsAdmin();
  }

  /** Crea un USER desde el panel de admin, devuelve la clave generada y abre un contexto nuevo. */
  private String givenAdminCreatesUser(String email) {
    LoginPage adminLogin = signInAsAdmin();
    adminLogin.navigate(adminLogin.baseUrl() + "/admin/users/new");

    UserFormPage form = new UserFormPage(page);
    form.typeFirstName("Brand");
    form.typeLastName("User");
    form.typeEmail(email);
    form.selectRole("USER");
    form.clickCreate();
    form.waitForPath("/admin/users");

    // El panel imprime la clave generada en un <code> y no la vuelve a mostrar.
    String generatedPassword = page.locator("code").textContent();

    newContext();
    return generatedPassword;
  }
}
