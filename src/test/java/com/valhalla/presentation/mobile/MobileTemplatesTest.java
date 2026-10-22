package com.valhalla.presentation.mobile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * [MOBILE] T-MOB-001: guard de plantillas responsive. Cada vista de {@code pages/**} debe declarar
 * intencion mobile en su markup (un breakpoint de Tailwind, un contenedor con scroll horizontal
 * propio, un wrap de fila o un ancho maximo/vertical de columna). Es el tripwire que impide
 * sumar vistas nuevas de escritorio puro; el comportamiento real (que nada se desborde a 390px)
 * lo verifican los E2E de {@code MobileLayoutE2E}.
 *
 * <p>Lee los archivos como hace BrandTokensTest: la garantia es sobre lo que el markup declara,
 * no sobre lo que un controller devuelve.
 */
public class MobileTemplatesTest {

  private static final Path TEMPLATES = Paths.get("src", "main", "webapp", "WEB-INF", "templates");
  private static final Path PAGES = TEMPLATES.resolve("pages");
  private static final Path BASE = TEMPLATES.resolve("layouts/base.html");

  private static final Pattern RESPONSIVE_INTENT = Pattern.compile(
    "sm:|md:|lg:|xl:|overflow-x-auto|flex-wrap|max-w-|flex-col"
  );

  @Test
  public void T_MOB_001_cadaVistaDeclaraIntencionResponsive() throws IOException {
    List<String> missing = new ArrayList<>();
    long total;
    try (Stream<Path> walk = Files.walk(PAGES)) {
      List<Path> pages = walk.filter(p -> p.toString().endsWith(".html")).sorted().toList();
      total = pages.size();
      for (Path file : pages) {
        String html = Files.readString(file, StandardCharsets.UTF_8);
        if (!RESPONSIVE_INTENT.matcher(html).find()) {
          missing.add(PAGES.relativize(file).toString());
        }
      }
    }

    assertThat("el recorrido encontro vistas para auditar", total, greaterThan(10L));
    assertThat(
      "vistas sin ninguna marca responsive (agrega breakpoint/overflow/wrap/max-w)",
      missing,
      is(empty())
    );
  }

  @Test
  public void T_MOB_001_baseDeclaraViewportMeta() throws IOException {
    String base = Files.readString(BASE, StandardCharsets.UTF_8);
    assertThat(base, containsString("width=device-width"));
  }
}
