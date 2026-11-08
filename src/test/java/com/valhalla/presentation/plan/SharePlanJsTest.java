package com.valhalla.presentation.plan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * [SHARE] El mensaje de compartir es SOLO el link: WhatsApp y el resto de los destinos
 * concatenan al texto del composer todo campo extra de navigator.share (title, text) y los
 * planes ya muestran su descripcion en la tarjeta OG del preview. Se usa el sheet del sistema
 * cuando el navegador lo expone (celular y los desktop que lo habilitan: ofrece WhatsApp,
 * Teams, Discord y demas destinos nativos); donde no existe, el boton cae al portapapeles. Se
 * asserta sobre el JS fuente porque el compartidor corre en el browser y esta suite no tiene
 * WebDriver.
 */
public class SharePlanJsTest {

  private static final Path SCRIPT = Path.of(
    "src",
    "main",
    "webapp",
    "resources",
    "core",
    "js",
    "share-plan.js"
  );

  @Test
  public void theSharePayloadCarriesOnlyTheUrl() throws Exception {
    String js = Files.readString(SCRIPT);

    // navigator.share cuando el navegador lo expone: nada de menus propios ni deteccion de UA.
    assertThat(js, containsString("if (navigator.share)"));
    assertThat(js, containsString("navigator.share({ url: absoluteUrl })"));
    assertThat(js, not(containsString("shareText")));
    assertThat(js, not(containsString("shareTitle")));
    assertThat(js, not(containsString("isMobileDevice")));
    assertThat(js, not(containsString("openShareMenu")));
    assertThat(js, not(containsString("web.whatsapp.com")));
    // El fallback a portapapeles tampoco agrega texto: copia la URL absoluta nomas.
    assertThat(js, containsString("navigator.clipboard.writeText(url)"));
  }
}
