package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.valhalla.e2e.views.PlansPage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * E-01 de la spec de programación automática de itinerarios: el horario se deriva solo.
 *
 * <p>El wizard de creación pide fecha, hora de inicio y modo de traslado —y ya no pide hora por
 * parada ni fin de día, que es el default fijo 23:00—, así que al llegar al detalle cada parada
 * tiene que mostrar su rango (<code>HH:mm – HH:mm</code>) sin tocar nada. Y como el horario es
 * una función del orden, reordenar lo vuelve a calcular: la hora de llegada de la parada movida
 * cambia.
 *
 * <p>{@link E2eBase} limpia la base antes de cada test, así que el plan de este test es el único
 * alrededor y sus paradas salen del catálogo sembrado (lugares distintos, con duraciones y
 * coordenadas distintas: con el mismo lugar repetido el reorden no movería ningún horario).
 */
public class PlanScheduleE2E extends E2eBase {

  private static final String PLAN_DATE = "2026-12-05";
  private static final String START_TIME = "09:00";
  private static final String TRANSPORT_MODE = "BIKE";
  private static final int STOP_COUNT = 3;

  /** Rango que dibuja el chip: llegada = segundo valor, la hora que cambia al reordenar. */
  private static final Pattern RANGE = Pattern.compile("(\\d{2}:\\d{2}) – (\\d{2}:\\d{2})");

  private PlansPage plans;
  private String planName;

  @BeforeEach
  void signInAndPreparePlanPage() {
    signInAsAdmin();
    this.plans = new PlansPage(page);
    this.planName = "Horario derivado E2E " + System.nanoTime();
  }

  @Test
  void E01_elHorarioDerivadoSeVeYSeRecalculaAlReordenar() {
    createPlanWithThreeStops();

    // El itinerario ya muestra el horario derivado, sin tocar nada: al menos dos paradas tienen
    // su chip con rango. El hasCount va primero porque es el que reintenta hasta que Vue monta
    // el itinerario; el count() de abajo no reintenta y lea 0 si llega antes.
    assertThat(page.locator("li.place-item")).hasCount(STOP_COUNT);
    Locator ranges = page
      .locator("li.place-item .stop-schedule")
      .filter(new Locator.FilterOptions().setHasText("–"));
    assertTrue(
      ranges.count() >= 2,
      "al menos dos paradas muestran un rango derivado, tenían " + ranges.count()
    );
    assertThat(ranges.nth(0)).isVisible();
    assertThat(ranges.nth(1)).isVisible();

    // La hora de inicio y el modo del paso 1 llegaron al plan: son los que usa el planificador
    // para derivar el horario que se acaba de mostrar. El fin de día no tiene input: es el
    // default fijo 23:00.
    assertThat(page.locator("input[name='eventTime']")).hasValue(START_TIME);
    assertThat(page.locator("input[name='dayEndTime']")).hasCount(0);
    assertThat(page.locator("select[name='transportMode']")).hasValue(TRANSPORT_MODE);

    // Reordenar: la primera parada pasa a la segunda, por el mismo drag & drop que usa
    // PlanPlaceDetailE2E. Se lee antes el texto de esa parada (título y rango) para comparar
    // después el mismo caso, no la posición.
    Locator titles = page.locator(".stop-title");
    Locator schedules = page.locator("li.place-item .stop-schedule");
    String movedTitle = titles.nth(0).innerText();
    String movedScheduleBefore = schedules.nth(0).innerText();
    String arrivalBefore = arrivalOf(movedScheduleBefore);

    Response reorder = page.waitForResponse(
      "**/reorder",
      () -> page.locator("li.place-item").nth(0).dragTo(page.locator("li.place-item").nth(1))
    );
    assertEquals(200, reorder.status(), "el reorden tiene que guardarlo el servidor");

    // El drop pinta el orden optimista y recién después pide el horario nuevo al servidor: sin
    // esta espera la comparación de abajo leería el texto viejo y el assert sería una carrera.
    page.waitForFunction(
      "before => { const s = document.querySelectorAll('.stop-schedule');" +
      " return s.length > 1 && s[1].innerText.trim() !== before; }",
      movedScheduleBefore
    );

    assertEquals(movedTitle, titles.nth(1).innerText(), "la primera parada pasa a la segunda");
    assertNotEquals(
      arrivalBefore,
      arrivalOf(schedules.nth(1).innerText()),
      "reordenar tiene que recalcular la hora de llegada de la parada movida"
    );
    // La cadena nueva arranca de nuevo en la hora de inicio del plan.
    assertThat(schedules.nth(0)).containsText(START_TIME);
  }

  /** Wizard completo: paso 1 con la ventana del día, paso 2 con tres paradas distintas. */
  private void createPlanWithThreeStops() {
    plans.navigateToCreatePlanModal();
    plans.typeName(planName);
    plans.typeDescription("Tres paradas con horario derivado");
    plans.typeEventDate(PLAN_DATE);
    plans.typeStartTime(START_TIME);
    plans.selectTransportMode(TRANSPORT_MODE);
    plans.clickNextStep();
    for (int index = 0; index < STOP_COUNT; index++) {
      plans.addDraftPlace(index);
    }

    // [ITI] Con las tres paradas en el borrador, el mapa de /explore dibuja el overlay:
    // un marcador numerado por parada y una polyline punteada que las une. Sin fitBounds,
    // y sin romper el conteo de pines de lugar (los del borrador no llevan data-category).
    assertThat(page.locator("#map .draft-marker")).hasCount(STOP_COUNT);
    assertThat(page.locator("#map path.leaflet-interactive[stroke-dasharray]")).hasCount(1);

    plans.clickNextStep();
    plans.clickCreate();
    plans.waitForDetailPath();
  }

  /** La hora de llegada (segundo valor) del rango `HH:mm – HH:mm` que dibuja el chip. */
  private String arrivalOf(String scheduleText) {
    Matcher range = RANGE.matcher(scheduleText);
    assertTrue(range.find(), "el chip tiene que mostrar un rango, texto: " + scheduleText);
    return range.group(2);
  }
}
