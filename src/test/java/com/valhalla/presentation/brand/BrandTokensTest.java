package com.valhalla.presentation.brand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Branding tokens (AC-01 a AC-08). These assertions read the templates as files, because the
 * guarantees are about what the markup declares, not about what a controller returns: a token that
 * nobody renders still has to exist in one place.
 */
public class BrandTokensTest {

  private static final Path TEMPLATES = Paths.get("src", "main", "webapp", "WEB-INF", "templates");

  private static final Path BASE = TEMPLATES.resolve("layouts/base.html");
  private static final Path TOKENS = Paths.get(
    "src",
    "main",
    "webapp",
    "resources",
    "css",
    "brand.css"
  );
  private static final Path NAVBAR = TEMPLATES.resolve("components/navbar.html");
  private static final Path PLACES = TEMPLATES.resolve("pages/places/list.html");
  /** The same view's JavaScript, extracted in phase 3 of docs/guide-js.md. */
  private static final Path PLACES_JS = Paths.get(
    "src",
    "main",
    "webapp",
    "resources",
    "core",
    "js",
    "pages",
    "places-list.js"
  );
  /** The shared map helpers, extracted in phase 5: the category table lives here now. */
  private static final Path PLACES_MAP_JS = Paths.get(
    "src",
    "main",
    "webapp",
    "resources",
    "core",
    "js",
    "lib",
    "places-map.js"
  );

  private static final List<String> CATEGORY_TOKENS = List.of(
    "--category-restaurant",
    "--category-bar",
    "--category-cafe",
    "--category-museum",
    "--category-park",
    "--category-shopping",
    "--category-nightlife",
    "--category-culture",
    "--category-sport",
    "--category-other"
  );

  /** Paleta y tipografia crudas, tal como viven en brand.css. */
  private static final List<String> BASE_TOKENS = List.of(
    "--white",
    "--black",
    "--gray",
    "--primary",
    "--secondary",
    "--typeface-sans",
    "--typeface-serif"
  );

  /** Nombre del icono de Lucide que se espera para cada categoría. */
  private static final List<String> LUCIDE_ICONS = List.of(
    "Utensils",
    "Wine",
    "Coffee",
    "Landmark",
    "Trees",
    "ShoppingBag",
    "Music",
    "BookOpen",
    "Trophy",
    "MapPin"
  );

  /** Las mismas bajo el namespace con el que Tailwind genera utilidades. */
  private static final List<String> BASE_THEME_ENTRIES = List.of(
    "--color-white",
    "--color-black",
    "--color-gray",
    "--color-primary",
    "--color-secondary",
    "--font-sans",
    "--font-serif"
  );

  // U-01: el navbar no dice UNLAM ni Valhalla.
  @Test
  void navbarCarriesNoInstitutionOrRepoName() throws IOException {
    String navbar = read(NAVBAR);

    assertThat(navbar, not(containsString("UNLAM")));
    assertThat(navbar, not(containsString("Valhalla")));
    assertThat(navbar, containsString("PlanIt"));
    // La clase es el hook que leen los tests del navbar; el nombre de la clase no se toca.
    assertThat(navbar, containsString("navbar-brand"));
  }

  // U-02: el head compartido declara el favicon.
  @Test
  void baseHeadDeclaresTheFavicon() throws IOException {
    assertThat(read(BASE), containsString("<link rel=\"icon\""));
  }

  // U-03: existe tipografia declarada y aplicada a body.
  @Test
  void baseHeadDeclaresAFontAppliedToBody() throws IOException {
    String base = read(BASE);
    String tokens = read(TOKENS);

    // En el template la ruta sigue siendo una expresión de Thymeleaf; que el link
    // renderizado apunte a /css/brand.css lo asserta BrandingPagesTest.
    assertThat(
      "el head declara la hoja de tokens",
      base,
      containsString("th:href=\"@{/css/brand.css}\"")
    );
    assertThat(tokens, containsString("--typeface-sans"));
    assertThat(tokens, containsString("--typeface-serif"));
    assertThat(tokens, containsString("font-family: var(--typeface-sans)"));
  }

  // Las webfonts entran por <link> en el head, con display=swap. El parámetro no es
  // cosmético: sin él el texto queda invisible hasta que la fuente llega, y encima
  // el reveal shield de base.html ya espera hasta 3 segundos.
  @Test
  void theWebfontsLoadWithSwapAndPreconnect() throws IOException {
    String base = read(BASE);

    assertThat(
      "las webfonts se piden desde el head compartido",
      base,
      containsString("fonts.googleapis.com/css2")
    );
    assertThat("Josefin Sans y Bree Serif", base, containsString("family=Josefin+Sans"));
    assertThat("Josefin Sans y Bree Serif", base, containsString("family=Bree+Serif"));
    assertThat("display=swap evita el texto invisible", base, containsString("display=swap"));
    assertThat(
      "preconnect a fonts.googleapis.com",
      base,
      containsString("rel=\"preconnect\" href=\"https://fonts.googleapis.com\"")
    );
    assertThat(
      "preconnect a fonts.gstatic.com",
      base,
      containsString("rel=\"preconnect\" href=\"https://fonts.gstatic.com\"")
    );
  }

  // El nombre de la webfont va primero en el token y la pila del sistema queda atrás.
  // Si el CDN no responde, la página se ve igual: ese fallback es el punto del stack.
  @Test
  void theTypefacesKeepASystemFallbackBehindTheWebfont() throws IOException {
    String tokens = read(TOKENS);

    assertThat(tokens, matchesPattern("(?s).*--typeface-sans: \"Josefin Sans\", system-ui.*"));
    assertThat(tokens, matchesPattern("(?s).*--typeface-serif: \"Bree Serif\", Georgia.*"));
  }

  // U-06: la paleta base y las dos tipografías existen una sola vez, cada una.
  @Test
  void basePaletteAndTypefacesAreDeclaredOnce() throws IOException {
    String tokens = read(TOKENS);

    for (String token : BASE_TOKENS) {
      assertThat(
        "declarado exactamente una vez: " + token,
        occurrences(tokens, "\n  " + token + ":"),
        is(1)
      );
    }
  }

  // U-04 (AC-03, AC-04): un token por categoria, declarado una sola vez, y ningun hex
  // suelto en el JS que arma el mapa.
  @Test
  void categoryColorsLiveInOneTokenPerCategory() throws IOException {
    String tokens = read(TOKENS);
    String places = read(PLACES);
    String placesJs = read(PLACES_JS);

    for (String token : CATEGORY_TOKENS) {
      assertThat(
        "token declarado exactamente una vez: " + token,
        occurrences(tokens, token),
        is(1)
      );
    }

    // El mapa del marker y el del panel eran dos copias del mismo objeto. Una sola ahora.
    assertThat(placesJs, not(containsString("categoryColors")));

    // Ningun hex de categoria literal en el JS: se lee del :root computado.
    assertThat(placesJs, containsString("getPropertyValue"));
    for (String hex : categoryHexes()) {
      assertThat(
        "ningun hex de categoria literal en places-list.js: " + hex,
        placesJs,
        not(containsString(hex))
      );
    }
  }

  // Los iconos de categoría salen de la librería vendorizada, no de SVG escrito a mano.
  // Antes eran diez blobs con el mismo wrapper de siete atributos y un stroke="white"
  // dentro de cada uno: el mismo problema de fuente duplicada que brand.css resolvió para
  // los colores, del lado de los iconos.
  @Test
  void categoryIconsComeFromTheVendoredLibrary() throws IOException {
    String places = read(PLACES);
    String placesJs = read(PLACES_JS);
    String placesMapJs = read(PLACES_MAP_JS);
    Path library = Paths.get("src", "main", "webapp", "resources", "core", "js", "lucide.min.js");

    org.junit.jupiter.api.Assertions.assertTrue(
      Files.exists(library),
      "la librería está vendorizada, junto a Vue y Tailwind: falta " + library
    );
    assertThat(
      "la vista la carga desde el recurso local, no de un CDN",
      places,
      containsString("th:src=\"@{/js/lucide.min.js}\"")
    );
    assertThat(places, not(containsString("unpkg.com/lucide")));

    // El SVG lo arma la librería. Ningún string de la vista empieza con '<svg', que es
    // como arrancaba cada entrada del mapa viejo. El único <svg> que queda es el botón
    // de cerrar del panel, que es markup y además ya usaba currentColor.
    assertThat(places, not(containsString("'<svg")));
    assertThat(places, not(containsString("stroke=\"white\"")));
    assertThat(placesJs, not(containsString("'<svg")));
    assertThat(placesJs, not(containsString("stroke=\"white\"")));
    assertThat(placesJs, containsString("lucide.createElement(lucide.icons[name]"));
    // La tabla de categorias es una sola desde la fase 5: vive en lib/places-map.js y las
    // tres vistas la comparten, en vez de tener una copia con nombres de Font Awesome y otra
    // con nombres de Lucide.
    assertThat(
      "places-list.js consume la tabla compartida",
      placesJs,
      containsString("from '../lib/places-map.js'")
    );
    assertThat(placesMapJs, containsString("lucide.createElement(lucide.icons[categoryIconName"));

    for (String icon : LUCIDE_ICONS) {
      assertThat("el mapa nombra el icono: " + icon, placesMapJs, containsString("'" + icon + "'"));
    }
  }

  // U-05 (AC-08): el titulo por defecto del head compartido es PlanIt.
  @Test
  void baseHeadDefaultsTheTitleToPlanIt() throws IOException {
    String base = read(BASE);

    assertThat(base, containsString(": 'PlanIt'"));
    // Ningun nombre viejo sobrevive como titulo por defecto.
    assertThat(base, not(containsString("'Valhalla'")));
    assertThat(base, not(containsString("'UNLAM'")));
  }

  // AC-02: ningun <title> de una vista dice Valhalla ni UNLAM.
  @Test
  void noViewTitleCarriesTheOldNames() throws IOException {
    List<Path> pages;
    try (var walk = Files.walk(TEMPLATES)) {
      pages = walk.filter(path -> path.toString().endsWith(".html")).toList();
    }

    for (Path page : pages) {
      String content = read(page);
      Matcher title = Pattern.compile("base :: head\\('([^']*)'\\)").matcher(content);
      if (title.find()) {
        assertThat(
          TEMPLATES.relativize(page).toString() + ": " + title.group(1),
          title.group(1),
          not(containsString("Valhalla"))
        );
        assertThat(
          TEMPLATES.relativize(page).toString() + ": " + title.group(1),
          title.group(1),
          not(containsString("UNLAM"))
        );
      }
    }
  }

  // AC-07: el navbar usa los tokens de marca en vez de las clases sueltas de color.
  @Test
  void navbarUsesTheBrandTokens() throws IOException {
    String navbar = read(NAVBAR);

    assertThat(navbar, containsString("text-secondary"));
    assertThat(navbar, containsString("bg-primary"));
    assertThat(navbar, containsString("bg-secondary"));
    assertThat(navbar, containsString("bg-black"));
    // Ningun color de marca hardcodeado como clase suelta de Tailwind.
    assertThat(navbar, not(containsString("bg-gray-700")));
    assertThat(navbar, not(containsString("bg-gray-900")));
    assertThat(navbar, not(containsString("bg-blue-600")));
  }

  // AC-07: el navbar usa las utilidades que genera @theme, no la sintaxis arbitraria.
  // La diferencia es visible en el atributo class del HTML renderizado, y por eso
  // esto se asserta sobre el markup final.
  @Test
  void navbarUsesRealUtilitiesInsteadOfArbitraryValues() throws IOException {
    String navbar = read(NAVBAR);

    assertThat(navbar, not(containsString("bg-[var(")));
    assertThat(navbar, not(containsString("text-[var(")));
  }

  // La capa @theme no puede declarar un color propio: si lo hiciera, brand.css dejaría
  // de ser la única fuente y el token volvería a vivir en dos lugares.
  @Test
  void theThemeLayerOnlyPointsAtTheStylesheetTokens() throws IOException {
    String base = read(BASE);
    Matcher theme = Pattern.compile("@theme\\s*\\{(.*?)\\}", Pattern.DOTALL).matcher(base);

    org.junit.jupiter.api.Assertions.assertTrue(theme.find(), "no hay bloque @theme en base.html");
    String block = theme.group(1);

    assertThat(
      "cada entrada de @theme apunta a un token de brand.css",
      block,
      not(containsString("#"))
    );
    for (String entry : BASE_THEME_ENTRIES) {
      assertThat(
        "la paleta y las tipografías entran a @theme: " + entry,
        block,
        containsString(entry)
      );
    }
    for (String token : CATEGORY_TOKENS) {
      assertThat("la categoría entra a @theme: " + token, block, containsString(token));
    }
    // Una variable que se referencia a sí misma es inválida en CSS: por eso los nombres
    // crudos de brand.css y los del namespace de Tailwind no pueden coincidir.
    for (String[] pair : new String[][] {
      { "--primary", "--color-primary" },
      { "--white", "--color-white" },
      { "--secondary", "--color-secondary" },
      { "--typeface-sans", "--font-sans" },
      { "--typeface-serif", "--font-serif" },
    }) {
      assertThat(
        "el token crudo se expone con otro nombre: " + pair[0],
        block,
        containsString(pair[1] + ": var(" + pair[0] + ")")
      );
    }
    // Y el bloque tiene que ser del tipo que el browser build realmente compila.
    assertThat(base, containsString("<style type=\"text/tailwindcss\">"));
  }

  // El token de cada categoria tiene que resolver al hex que el E2E asserta. Si esto cambia,
  // el valor del color pasa a ser un trabajo aparte con su propio E2E.
  @Test
  void categoryTokensKeepTheHexTheE2EAsserts() throws IOException {
    String tokens = read(TOKENS);

    for (String[] pair : new String[][] {
      { "--category-restaurant", "#e74c3c" },
      { "--category-bar", "#9b59b6" },
      { "--category-cafe", "#e67e22" },
      { "--category-museum", "#3498db" },
      { "--category-park", "#2ecc71" },
      { "--category-shopping", "#f39c12" },
      { "--category-nightlife", "#1abc9c" },
      { "--category-culture", "#34495e" },
      { "--category-sport", "#e91e63" },
      { "--category-other", "#95a5a6" },
    }) {
      assertThat(tokens, containsString(pair[0] + ": " + pair[1] + ";"));
    }
  }

  // Vista previa al compartir. Open Graph y tarjeta de Twitter son las dos rutas que usan
  // los scrapers reales: los OG sin og:image salen sin miniatura, y con una URL relativa
  // no se descargan.
  @Test
  void theSharedHeadDeclaresTheSharePreview() throws IOException {
    String base = read(BASE);

    for (String[] tag : new String[][] {
      { "og:type", "content=\"website\"" },
      { "og:site_name", "content=\"PlanIt\"" },
      { "og:title", null },
      { "og:description", null },
      { "og:image", "th:content=\"|${@siteOrigin.base()}@{/images/og-image.png}|\"" },
      { "og:image:width", "content=\"1200\"" },
      { "og:image:height", "content=\"630\"" },
      { "twitter:card", "content=\"summary_large_image\"" },
      { "twitter:image", "th:content=\"|${@siteOrigin.base()}@{/images/og-image.png}|\"" },
    }) {
      String element = metaElement(base, tag[0]);
      if (tag[1] != null) {
        assertThat(tag[0] + ": " + element, element, containsString(tag[1]));
      }
    }

    // El prefijo sale de un bean de scope de request, no del @{}: Thymeleaf devuelve una
    // URL relativa en un GET directo y una og:image relativa no la baja ningún scraper. Que
    // el bean exista y resuelva lo asserta BrandingPagesTest sobre el HTML ya renderizado.
    assertThat(base, containsString("@siteOrigin.base()"));

    // Un og:image pelado no lo baja ningún scraper: la URL tiene que salir absoluta, y
    // el @{} de Thymeleaf devuelve una relativa, así que no alcanza con linkearlo.
    assertThat(
      "la URL de la imagen se arma con la request, no con un @{} solo",
      base,
      not(containsString("th:content=\"@{/images/og-image.png}\""))
    );
  }

  // El título de la vista también es el título de la tarjeta: si el og:title quedara fijo
  // en PlanIt, cada link compartido perdería el nombre de la página.
  @Test
  void theSharePreviewTakesThePageTitle() throws IOException {
    String base = read(BASE);

    for (String property : new String[] { "og:title", "twitter:title" }) {
      assertThat(
        property + " sigue el título de la página",
        metaElement(base, property),
        containsString("${pageTitle != null}")
      );
    }
  }

  /**
   * Open Graph se declara con property= y Twitter con name=. Buscar siempre el <meta> por el
   * nombre del atributo evita el falso positivo de un assert que solo reconoce property=.
   */
  private static String metaElement(String head, String name) {
    Matcher matcher = Pattern
      .compile("<meta\\s+[^>]*\\b(?:property|name)=\"" + Pattern.quote(name) + "\"[^>]*>")
      .matcher(head);
    org.junit.jupiter.api.Assertions.assertTrue(matcher.find(), "falta el meta " + name);
    return matcher.group();
  }

  // Barra del navegador, manifest e ícono de iOS. El último va aparte porque iOS no
  // acepta SVG para apple-touch-icon y aplica su propia máscara encima.
  @Test
  void theSharedHeadDeclaresTheInstallIcons() throws IOException {
    String base = read(BASE);

    assertThat(base, containsString("<meta name=\"theme-color\" content=\"#111827\">"));
    assertThat(
      "el manifest se enlaza desde el recurso local",
      base,
      containsString("th:href=\"@{/manifest.json}\"")
    );
    assertThat(
      "el ícono de iOS es un PNG, no el SVG del favicon",
      base,
      containsString("th:href=\"@{/images/apple-touch-icon.png}\"")
    );
  }

  // La serif no puede quedar como token muerto: brand.css la declara y @theme la expone,
  // pero si ninguna plantilla la usa no hay tipografía de marca en pantalla. Va en la
  // marca — el wordmark del navbar y el h1 de la landing — y no en los h1 de formulario,
  // donde una display serif se lee como un error de estilo y no como identidad.
  @Test
  void theSerifIsActuallyUsedForTheBrandLockup() throws IOException {
    String navbar = read(NAVBAR);
    String landing = read(TEMPLATES.resolve("pages/landing.html"));

    assertThat("el wordmark del navbar", navbar, containsString("font-serif"));
    assertThat("el h1 de la landing", landing, containsString("font-serif"));
    assertThat("el wordmark", landing, containsString("PlanIt"));
  }

  // Decisión de alcance: los neutros siguen siendo la escala por defecto de Tailwind.
  // El riesgo de esa decisión es que alguien lea brand.css y asuma que hay una rampa
  // propia, así que la decisión queda escrita en el archivo y no solo en un chat.
  @Test
  void theNeutralScaleStaysOnTailwindAndSaysSo() throws IOException {
    String tokens = read(TOKENS);
    String base = read(BASE);
    Matcher theme = Pattern.compile("@theme\\s*\\{(.*?)\\}", Pattern.DOTALL).matcher(base);
    org.junit.jupiter.api.Assertions.assertTrue(theme.find(), "no hay bloque @theme en base.html");

    // --gray sí existe: es el color de texto secundario que define la marca.
    assertThat(tokens, containsString("--gray:"));

    // El invariante estructural: no hay rampa neutra propia. Si alguien declara
    // --gray-500 o --neutral-900, la decisión de alcance cambió y este test lo frena.
    org.junit.jupiter.api.Assertions.assertFalse(
      Pattern.compile("--(?:gray|slate|zinc|neutral)-\\d+\\s*:").matcher(tokens).find(),
      "brand.css no debe declarar una rampa neutra propia: los neutros son los de Tailwind"
    );

    // Pero @theme no remapea ningún gris de la rampa: si los remapeara, la escala dejaría
    // de ser la de Tailwind y aparecería una segunda fuente de gris.
    for (String neutral : new String[] {
      "--color-gray-50",
      "--color-gray-100",
      "--color-gray-500",
      "--color-gray-900",
    }) {
      assertThat(
        "la rampa neutra no se remapea: " + neutral,
        theme.group(1),
        not(containsString(neutral))
      );
    }

    // Y la decisión queda escrita en el archivo, no solo en una conversación: alguien que
    // abra brand.css tiene que poder ver por qué el gris no está.
    assertThat(
      "la decisión sobre los neutros está escrita, no implícita",
      tokens,
      containsString("por defecto de Tailwind")
    );
  }

  private static Set<String> categoryHexes() {
    Set<String> hexes = new HashSet<>();
    Matcher matcher = Pattern
      .compile("(--category-[a-z]+):\\s*(#[0-9a-fA-F]{6})")
      .matcher(readQuietly(TOKENS));
    while (matcher.find()) {
      hexes.add(matcher.group(2));
    }
    if (hexes.isEmpty()) {
      fail("no se encontro ningun token --category-* en " + TOKENS);
    }
    return hexes;
  }

  private static int occurrences(String haystack, String needle) {
    int count = 0;
    for (
      int at = haystack.indexOf(needle);
      at >= 0;
      at = haystack.indexOf(needle, at + needle.length())
    ) {
      count++;
    }
    return count;
  }

  private static String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("no se pudo leer " + path, e);
    }
  }

  private static String readQuietly(Path path) {
    try {
      return read(path);
    } catch (RuntimeException e) {
      return "";
    }
  }

  // Un token por cada valor del enum PlaceCategory, sin sobras ni faltantes.
  @Test
  void everyCategoryTokenIsDeclaredOnce() throws IOException {
    String tokens = read(TOKENS);

    assertThat(tokens, matchesPattern("(?s).*--category-restaurant: #[0-9a-fA-F]{6};.*"));
    assertThat(CATEGORY_TOKENS.size(), is(10));
    assertThat(
      Pattern.compile("--category-[a-z]+\\s*:").matcher(tokens).results().count(),
      is((long) CATEGORY_TOKENS.size())
    );
  }
}
