package com.valhalla.presentation.brand;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * [DIS] 23-DIS: el set de iconos de acción y el autor del feed en una línea. Lee los templates
 * como texto, igual que {@link BrandTokensTest}: las garantías son sobre lo que el markup
 * declara —el set, el aria, la una línea—, no sobre lo que un controller devuelve.
 */
public class ActionIconsTest {

  private static final Path TEMPLATES = Paths.get("src", "main", "webapp", "WEB-INF", "templates");

  private static final Path FEED = TEMPLATES.resolve("pages/plans/public-list.html");
  private static final Path PUBLIC_PLAN = TEMPLATES.resolve("pages/plans/public.html");
  private static final Path DETAIL = TEMPLATES.resolve("pages/plans/detail.html");
  private static final Path MY_PLANS = TEMPLATES.resolve("pages/plans/list.html");
  private static final Path PROFILE_EDIT = TEMPLATES.resolve("pages/profile/edit.html");
  private static final Path CHANGE_PASSWORD = TEMPLATES.resolve(
    "pages/profile/change-password.html"
  );
  private static final Path EXPLORER = TEMPLATES.resolve("pages/places/list.html");
  private static final Path BOOKMARK = TEMPLATES.resolve("components/bookmark-button.html");
  private static final Path NAVBAR_MENU = TEMPLATES.resolve("components/navbar-menu.html");

  /** Marcador del icono del set: 16px, la medida de toda acción con texto. */
  private static final String SET_SVG = "<svg class=\"h-4 w-4\"";

  /** Fragmentos de los paths de Lucide (mismo set vendorizado), uno por acción del inventario. */
  private static final String PLUS = "M5 12h14";
  private static final String SEARCH = "m21 21-4.3-4.3";
  private static final String USER_PLUS = "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2";
  private static final String COPY = "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2";
  private static final String EYE = "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0";
  private static final String TRASH = "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6";
  private static final String LOG_OUT = "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4";
  private static final String SETTINGS_GLYPH = "M12.22 2h-.44a2 2 0 0 0-2 2v.18";
  private static final String CHECK = "M20 6 9 17l-5-5";
  private static final String CLOSE = "M18 6 6 18";
  private static final String SAVE = "M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8";

  /** Cada vista del inventario declara su cantidad mínima de iconos del set (U-01/U-02). */
  private static final Map<Path, Integer> SET_ICONS_BY_VIEW = Map.of(
    FEED,
    5, // Crear plan, Buscar, Clonar (form + link), Ver todos los planes
    PUBLIC_PLAN,
    5, // Unirme (form + link), Clonar (form + link), Ver mi plan
    DETAIL,
    6, // Configuración, Eliminar, Salir, Clonar, Guardar cambios, Guardar permisos
    MY_PLANS,
    8, // Crear plan, Unirme, Ver detalle x3, Eliminar, Aceptar, Rechazar
    PROFILE_EDIT,
    4, // Guardar perfil, Abrir, Clonar, Quitar
    CHANGE_PASSWORD,
    1,
    EXPLORER,
    2 // Create plan (sidebar) y Crear plan (submit del modal)
  );

  private static final List<Path> TEMPLATES_OF_THE_INVENTORY = List.copyOf(
    SET_ICONS_BY_VIEW.keySet()
  );

  /** U-01 (AC-01): cada acción del inventario lleva su glyph del set Lucide. */
  @Test
  void everyActionCarriesItsIconFromTheSameSet() {
    assertThat(read(FEED), containsString(PLUS));
    assertThat(read(FEED), containsString(SEARCH));
    assertThat(read(FEED), containsString(COPY));
    assertThat(read(FEED), containsString(EYE));

    assertThat(read(PUBLIC_PLAN), containsString(USER_PLUS));
    assertThat(read(PUBLIC_PLAN), containsString(COPY));
    assertThat(read(PUBLIC_PLAN), containsString(EYE));

    assertThat(read(DETAIL), containsString(SETTINGS_GLYPH));
    assertThat(read(DETAIL), containsString(TRASH));
    assertThat(read(DETAIL), containsString(LOG_OUT));
    assertThat(read(DETAIL), containsString(COPY));
    assertThat(read(DETAIL), containsString(SAVE));

    assertThat(read(MY_PLANS), containsString(PLUS));
    assertThat(read(MY_PLANS), containsString(USER_PLUS));
    assertThat(read(MY_PLANS), containsString(EYE));
    assertThat(read(MY_PLANS), containsString(TRASH));
    assertThat(read(MY_PLANS), containsString(CHECK));
    assertThat(read(MY_PLANS), containsString(CLOSE));

    assertThat(read(PROFILE_EDIT), containsString(SAVE));
    assertThat(read(PROFILE_EDIT), containsString(EYE));
    assertThat(read(PROFILE_EDIT), containsString(COPY));
    assertThat(read(PROFILE_EDIT), containsString(TRASH));
    assertThat(read(CHANGE_PASSWORD), containsString(SAVE));
    assertThat(read(EXPLORER), containsString(PLUS));
  }

  /**
   * U-02/U-03 (AC-01, AC-02, AC-03): el set pinta con currentColor a tamaño fijo, cada icono va
   * aria-hidden (el texto del botón es el nombre accesible) y ningún color propio entra al trazo.
   */
  @Test
  void theSetIsOutlinedWithCurrentColorAndAFixedSize() {
    for (Path file : TEMPLATES_OF_THE_INVENTORY) {
      assertThat(
        file + " no declara colores hardcoded en sus iconos",
        read(file),
        not(containsString("stroke=\"#"))
      );
      assertEverySetIconIsHiddenFromAssistiveTech(file);
    }
    SET_ICONS_BY_VIEW.forEach((file, expected) -> {
      long iconsInFile = countOccurrences(read(file), SET_SVG);
      assertThat(
        file + " declara al menos " + expected + " iconos del set",
        iconsInFile,
        greaterThanOrEqualTo(expected.longValue())
      );
    });
  }

  /** U-03 (AC-03): los controles de solo icono conservan su nombre accesible. */
  @Test
  void iconOnlyControlsKeepTheirAccessibleName() {
    assertThat(read(BOOKMARK), containsString("aria-label"));
    assertThat(read(BOOKMARK), containsString("title="));
    assertThat(read(BOOKMARK), containsString("sr-only"));
    assertThat(read(BOOKMARK), containsString("aria-pressed"));
    assertThat(read(BOOKMARK), containsString("data-bookmark-icon=\"filled\""));
    assertThat(read(BOOKMARK), containsString("data-bookmark-icon=\"outline\""));
    assertThat(read(NAVBAR_MENU), containsString("aria-hidden=\"true\""));
    assertThat(read(EXPLORER), containsString(":aria-label="));
    assertThat(read(EXPLORER), containsString("data-bookmark-icon=\"filled\""));
    assertThat(read(EXPLORER), containsString("data-bookmark-icon=\"outline\""));
    assertThat(read(EXPLORER), containsString(":disabled=\"plan.bookmarking === true\""));
  }

  /** U-04 (AC-04): "Por {autor}" en una sola línea, con elipsis visual y nombre completo en title. */
  @Test
  void theFeedAuthorLineStaysOnOneLineWithAccessibleTruncation() {
    String feed = read(FEED);
    assertThat(feed, containsString("data-testid=\"plan-card-author\""));
    assertThat(feed, containsString("class=\"flex min-w-0 items-baseline gap-1\""));
    assertThat(
      feed,
      containsString("class=\"relative z-10 truncate text-blue-600 hover:underline\"")
    );
    assertThat(feed, containsString("th:title=\"${plan.administratorName}\""));
    // El link de la card es un overlay: el link del autor no queda anidado dentro de otro <a>
    // (HTML invalido que el parser partia en fragmentos).
    assertThat(
      feed,
      containsString(
        "<a th:href=\"@{/plans/{id}/public(id=${plan.id})}\" class=\"absolute inset-0 z-0\">"
      )
    );
    assertThat(feed, containsString("<span class=\"sr-only\" th:text=\"${plan.name}\">"));
    // La fila de acciones tambien va por encima del overlay: si no, el <a> absoluto le roba
    // el click al boton Clonar (lo cubrio el E2E de clonar del feed).
    assertThat(
      feed,
      containsString("<div class=\"relative z-10 flex gap-2 border-t border-gray-100 p-3\">")
    );

    String profile = read(PROFILE_EDIT);
    assertThat(profile, containsString("data-testid=\"bookmark-card-author\""));
    assertThat(profile, containsString("class=\"truncate text-primary hover:underline\""));
    assertThat(profile, containsString("th:title=\"${bookmark.administratorName}\""));
  }

  /**
   * U-05 (AC-05): los iconos del set viajan como SVG inline, no vía runtime. Ninguna vista del
   * inventario usa {@code data-lucide} para ellos. (Algunas vistas ya cargaban el vendorizado de
   * Lucide de antes de esta card —detalle y explorador, para sus módulos JS—; la card no agrega
   * ni una invocación nueva.)
   */
  @Test
  void theNewIconsShipAsInlineSvgWithoutTheLucideRuntime() {
    for (Path file : TEMPLATES_OF_THE_INVENTORY) {
      assertThat(
        file + " no dibuja sus iconos de acción vía data-lucide (son SVG inline)",
        read(file),
        not(containsString("data-lucide"))
      );
    }
  }

  /** Recorre cada icono del set del archivo y fija su contrato de dibujo y de accesibilidad. */
  private static void assertEverySetIconIsHiddenFromAssistiveTech(Path file) {
    String html = read(file);
    int index = html.indexOf(SET_SVG);
    while (index != -1) {
      int end = html.indexOf("</svg>", index);
      String svg = html.substring(index, end);
      assertThat(
        file + ": el icono del set va aria-hidden (el texto nombra el control)",
        svg,
        containsString("aria-hidden=\"true\"")
      );
      assertThat(
        file + ": el icono pinta con currentColor, no con un color propio",
        svg,
        containsString("stroke=\"currentColor\"")
      );
      assertThat(
        file + ": el trazo del set es stroke-width 2",
        svg,
        containsString("stroke-width=\"2\"")
      );
      index = html.indexOf(SET_SVG, end);
    }
  }

  private static int countOccurrences(String text, String needle) {
    int count = 0;
    int index = text.indexOf(needle);
    while (index != -1) {
      count++;
      index = text.indexOf(needle, index + needle.length());
    }
    return count;
  }

  private static String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
