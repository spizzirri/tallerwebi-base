package com.valhalla.presentation.brand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Modo oscuro, por archivo. El comportamiento en browser —repintado, persistencia en
 * localStorage, ícono que cambia— lo cubre ThemeModeE2E; esto clava las piezas estructurales
 * que ese E2E solo ve funcionando: el script temprano del head, el botón en las dos ramas del
 * navbar y la capa oscura en el stylesheet de tokens. Lee los templates porque la garantía es
 * sobre lo que el markup declara, igual que en BrandTokensTest.
 */
public class ThemeModeTest {

  private static final Path TEMPLATES = Paths.get("src", "main", "webapp", "WEB-INF", "templates");

  private static final Path BASE = TEMPLATES.resolve("layouts/base.html");
  private static final Path NAVBAR = TEMPLATES.resolve("components/navbar.html");
  private static final Path LANDING = TEMPLATES.resolve("pages/landing.html");
  private static final Path DETAIL = TEMPLATES.resolve("pages/plans/detail.html");
  private static final Path TOKENS = Paths.get(
    "src",
    "main",
    "webapp",
    "resources",
    "css",
    "brand.css"
  );

  // El tema se resuelve en el head, antes del primer stylesheet: si el script bajara al final
  // del body, la primera pintura saldría clara y habría que ocultar el destello a mano.
  @Test
  void theThemeIsResolvedInTheHeadBeforeAnyStylesheet() throws IOException {
    String base = read(BASE);

    int themeBlock = base.indexOf("planit-theme");
    int stylesheets = base.indexOf("@{/css/brand.css}");

    assertThat("el script lee la decision guardada", themeBlock, greaterThan(0));
    assertThat("hay un stylesheet que lo sigue", stylesheets, greaterThan(0));
    assertThat("y el tema se resuelve antes de él", themeBlock, lessThan(stylesheets));

    assertThat(
      "lee la clave de localStorage",
      base,
      containsString("localStorage.getItem(\"planit-theme\")")
    );
    assertThat(
      "escribe los dos atributos con el mismo valor",
      base,
      containsString("setAttribute(\"data-theme\", mode)")
    );
    assertThat(
      "data-bs-theme es la llave que lee Bootstrap",
      base,
      containsString("setAttribute(\"data-bs-theme\", mode)")
    );
  }

  // El toggle se duplica en las dos ramas del menú como todo el resto del navbar (el nav es
  // flex de dos hijos). El E2E pinnea el conjunto visible de items; esto clava que ninguna
  // rama se quede sin botón ni sin su label accesible.
  @Test
  void theNavbarCarriesTheToggleInBothBranches() throws IOException {
    String navbar = read(NAVBAR);

    assertThat(
      "las dos ramas declaran el botón",
      occurrences(navbar, "type=\"button\" data-theme-toggle"),
      is(2)
    );
    assertThat(
      "con el label accesible en cada rama",
      occurrences(navbar, "<span class=\"sr-only\">Cambiar tema</span>"),
      is(2)
    );
    assertThat("los dos íconos viajan siempre", occurrences(navbar, "theme-icon-moon"), is(2));
    assertThat(occurrences(navbar, "theme-icon-sun"), is(2));
  }

  // La capa oscura se declara una sola vez y en el stylesheet de tokens: una página que
  // repita estos overrides se desincroniza del resto la próxima que cambie un gris.
  @Test
  void theDarkOverridesLiveInTheTokenStylesheet() throws IOException {
    String tokens = read(TOKENS);

    assertThat(
      "los tokens oscuros se declaran una sola vez",
      occurrences(tokens, "html[data-theme=\"dark\"] {"),
      is(1)
    );
    assertThat(
      tokens,
      containsString("html[data-theme=\"dark\"] .bg-white { background-color: var(--dark-card); }")
    );
    assertThat(
      tokens,
      containsString(
        "html[data-theme=\"dark\"] .bg-gray-100 { background-color: var(--dark-page); }"
      )
    );
    assertThat(
      tokens,
      containsString("html[data-theme=\"dark\"] .text-gray-900 { color: var(--dark-text); }")
    );
    assertThat(
      "el texto de contenido principal también se invierte",
      tokens,
      containsString("html[data-theme=\"dark\"] main .text-black")
    );
    // El toggle no sincroniza íconos desde JS: los dos viajan en el botón y los muestra el modo.
    assertThat(tokens, containsString(".theme-icon-sun { display: none; }"));
    // El color por defecto del texto: el preflight de Tailwind no declara ninguno, así que lo
    // sin clase text-* hereda el negro del navegador. Tiene que vivir en el bloque de tokens
    // oscuros (no en una utilidad) para cubrir exactamente ese caso.
    assertThat(
      "el texto sin clase de color hereda claro en oscuro",
      tokens,
      matchesPattern(
        "(?s).+html\\[data-theme=\"dark\"\\]\\s*\\{[^}]*color: var\\(--dark-text\\);.+"
      )
    );
  }

  // Las dos páginas con CSS propio pintan hex a mano y no usan utilidades de Tailwind para
  // esas superficies: llevan su propio override al lado de la regla original.
  @Test
  void thePagesThatOwnTheirCssCarryTheirDarkOverrides() throws IOException {
    String landing = read(LANDING);
    String detail = read(DETAIL);

    assertThat(
      "el degradado del hero invierte la escala",
      landing,
      containsString("html[data-theme=\"dark\"] .hero-title")
    );
    assertThat(
      "y también el revelado del tagline",
      landing,
      containsString("html[data-theme=\"dark\"].reveal-ready .tagline-word")
    );
    assertThat(
      "Bootstrap no redefine --bs-light en dark: las superficies que lo usan sí cambian",
      detail,
      containsString("[data-bs-theme=\"dark\"] .stop-category")
    );
    assertThat(
      "las paradas leen su fondo a la variable que Bootstrap sí re-colorea",
      detail,
      matchesPattern("(?s).*\\.itinerary-stop\\s*\\{[^}]*var\\(--bs-body-bg\\).*")
    );
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
}
