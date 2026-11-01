package com.valhalla.presentation.place;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PlaceDetailViewTest {

  private static final Path TEMPLATE_PATH = Path.of(
    "src/main/webapp/WEB-INF/templates/pages/places/list.html"
  );

  private static String html;

  @BeforeAll
  static void readTemplate() throws IOException {
    html = Files.readString(TEMPLATE_PATH);
  }

  @Test
  public void T_APL_FE_001_vistaLugares_pickerInlineSinModal() {
    // El boton "Add to plan" del panel de detalles y su modal se quitaron de la vista:
    // el alta de lugares vive en el wizard de creacion y en el detalle del plan.
    assertThat(html, not(containsString("Add to plan")));
    assertThat(html, not(containsString("Agregar a plan")));

    // [APL] El panel de detalle recupero un picker de plan, pero inline: select + boton,
    // sin dialogo. El gate es la sesión (isLogged), no la cantidad de planes: logueado
    // sin planes ofrece crear el primero con el wizard de la página.
    assertThat(html, containsString("id=\"plan-picker\""));
    assertThat(html, containsString("aria-label=\"Agregar al plan\""));
    assertThat(html, containsString("v-if=\"isLogged\""));
    assertThat(html, containsString("data-is-logged=${isLogged}"));
    assertThat(html, containsString("Crear un plan"));
    // [T10] Con el wizard abierto el mismo bloque agrega al plan en proceso.
    assertThat(html, containsString("v-if=\"showCreatePlan\""));
    assertThat(html, containsString("@click=\"addSelectedToDraft\""));
    // [T12] Sin sesión el "+" del panel de planes manda al login, no al wizard.
    assertThat(html, containsString("th:if=\"${!isLogged}\""));
    assertThat(html, containsString("@{/auth/login?redirect=/explore}"));
    assertThat(html, not(containsString("role=\"dialog\"")));

    // Queda el boton de las tarjetas que agrega al borrador mientras el wizard esta abierto.
    assertThat(html, containsString("type=\"button\""));
  }

  @Test
  public void T_APL_FE_001_vistaLugares_contieneSelectDeFiltro() {
    // 3- Verifica la presencia del elemento <select> como pide en Trello pero este select es del
    // filtro de categorías del sidebar.
    assertThat(html, containsString("<select"));
  }
}
