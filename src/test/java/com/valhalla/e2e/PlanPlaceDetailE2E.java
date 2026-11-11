package com.valhalla.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.valhalla.e2e.views.PlansPage;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Browser coverage of a plan's itinerary: a stop created through the wizard shows the schedule
 * the planner derives from the plan's own date and start time, keeps it across reloads, and
 * stays reorderable and removable afterwards. The "Ver más" dialog reads the itinerary over REST
 * and shows the range and the day as text, and pinning a stop goes through the endpoint the app
 * itself uses.
 *
 * <p>{@link E2eBase} wipes the database before the test, so the seeded admin owns the only plan
 * around and every assertion below is about the plan this test creates. Screenshots land in
 * {@code target/itinerary-*.png}; failures keep their own screenshot and trace in {@code
 * target/e2e-artifacts/}.
 */
public class PlanPlaceDetailE2E extends E2eBase {

  // [SCH] El tile FECHA del resumen se unificó a dd/mm/yyyy con el resto de la feature; el
  // input del form de edición sigue value ISO. Ambas fechas se computan en el @BeforeEach:
  // el #plan-date del wizard lleva min=${today}, así que un literal duro vence contra el reloj.
  private static final String PLAN_TIME = "20:00";
  private static final String START_TIME = "18:30";
  private static final String PLAN_DESCRIPTION = "Itinerario con horarios de visita";
  private static final String PINNED_TIME = "10:30";
  private String eventDate;
  private String pinnedDate;
  // Playwright manda el Pattern de Java tal cual a una regex de JavaScript, donde no existen
  // (?s) ni \Q...\E: por eso el "any" explícito es [\s\S]* en lugar de (?s).*.
  private static final Pattern RANGE = Pattern.compile(
    "[\\s\\S]*\\d{2}:\\d{2} – \\d{2}:\\d{2}[\\s\\S]*"
  );

  /**
   * El chip del itinerario junta rango y duración en un solo nodo, así que hasText (que compara el
   * texto completo) necesita el rango completo que arranca en la hora dada.
   */
  private static Pattern rangeFrom(String start) {
    return Pattern.compile("[\\s\\S]*" + start + " – \\d{2}:\\d{2}[\\s\\S]*");
  }

  private PlansPage plans;
  private String planName;

  @BeforeEach
  void signInAndPreparePlanPage() {
    signInAsAdmin();
    this.plans = new PlansPage(page);
    this.planName = "Detalle E2E " + System.nanoTime();
    // Relative order matters: the pinned day (+2) must differ from the plan's own day (+1).
    this.eventDate = isoDateIn(1);
    this.pinnedDate = isoDateIn(2);
  }

  @Test
  void addsPersistsAndDisplaysVisitDetails() {
    createPlanWithAVisit();

    // Creation: the stop and the schedule derived from the create form's date and start time are
    // on screen right away — the chip carries the full range, the summary carries the same start
    // time, and nothing is pinned by hand yet (the pin only shows for a hand-picked time).
    assertEquals(planName, plans.getDetailHeading());
    assertThat(page.locator(".stop-details")).hasCount(1);
    assertThat(page.locator(".stop-schedule").first()).hasText(rangeFrom(START_TIME));
    assertThat(page.locator(".stop-pinned")).hasCount(0);
    assertThat(page.locator("#plan-summary")).containsText(dateLabel(eventDate));
    assertThat(page.locator("#plan-summary")).containsText(START_TIME);
    assertThat(page.locator("input[name='eventDate']")).hasValue(eventDate);
    assertThat(page.locator("input[name='eventTime']")).hasValue(START_TIME);

    // The start time belongs to the edit form: changing it re-derives the whole chain, so the
    // summary and the stop's chip have to move together. La configuración vive en una card
    // centrada: se abre antes de tocar el form.
    plans.openPlanConfig();
    page.locator("input[name='eventTime']").fill(PLAN_TIME);
    page.getByText("Guardar cambios", new Page.GetByTextOptions().setExact(true)).click();
    plans.waitForDetailPath();
    assertThat(page.locator("#plan-summary")).containsText(PLAN_TIME);
    assertThat(page.locator(".stop-schedule").first()).hasText(rangeFrom(PLAN_TIME));

    // The details dialog fetches the itinerary instead of reading the rendered snapshot: it
    // shows the range as text and the stop's position in the itinerary.
    Locator dialog = openStopDetails(0);
    assertThat(dialog).containsText("Horario de visita");
    assertThat(dialog).containsText(PLAN_TIME);
    assertThat(dialog.locator("dd").last()).hasText("1");
    closeStopDetails();

    // Nothing above is client state: a reload shows the same stop with the same schedule.
    page.reload();
    assertThat(page.locator(".stop-details")).hasCount(1);
    assertThat(page.locator(".stop-schedule").first()).containsText(PLAN_TIME);

    // The detail page has no date/time editor, so the change goes through the very endpoint the
    // app calls. Pinning the stop onto another day moves it out of the plan's own day: the
    // dialog and the itinerary both pick up the new time and the dd/mm/yyyy day, and the stop is
    // now marked as pinned instead of derived.
    updateVisitDetails(pinnedDate, PINNED_TIME);
    dialog = openStopDetails(0);
    assertThat(dialog).containsText(PINNED_TIME);
    assertThat(dialog).containsText(dateLabel(pinnedDate));
    closeStopDetails();
    page.reload();
    assertThat(page.locator(".stop-schedule").first()).containsText(PINNED_TIME);
    assertThat(page.locator(".stop-schedule").first()).containsText(dateLabel(pinnedDate));
    assertThat(page.locator(".stop-pinned")).hasCount(1);

    // A second stop added from the page's own form carries no schedule of its own: the server
    // derives one, and the dialog shows the range, the day it landed on and position 2.
    addPlaceToPlan();
    assertThat(page.locator(".stop-details")).hasCount(2);
    dialog = openStopDetails(1);
    assertThat(dialog).containsText("Horario de visita");
    assertThat(dialog).containsText(RANGE);
    assertThat(dialog).containsText(dateLabel(pinnedDate));
    assertThat(dialog.locator("dd").last()).hasText("2");
    closeStopDetails();

    // Reordering is drag and drop, and the new order survives a reload. The drop handler swaps
    // the list optimistically and saves afterwards, so the reload has to wait for that save:
    // reloading while the POST is still in flight renders the old order from the database.
    Locator titles = page.locator(".stop-title");
    String firstName = titles.nth(0).innerText();
    String secondName = titles.nth(1).innerText();
    assertThat(page.locator(".place-item").first()).hasAttribute("draggable", "true");
    Response reorder = reorderFirstTwoStops();
    assertEquals(200, reorder.status(), "the reorder has to be saved by the server");
    assertThat(titles.nth(0)).hasText(secondName);
    assertThat(titles.nth(1)).hasText(firstName);
    page.reload();
    assertThat(titles.nth(0)).hasText(secondName);

    screenshotItinerary();

    // Removing both stops empties the itinerary, and it stays empty after a reload.
    page.locator(".stop-remove").nth(0).click();
    assertThat(page.locator(".stop-details")).hasCount(1);
    page.locator(".stop-remove").nth(0).click();
    assertThat(page.locator(".stop-details")).hasCount(0);
    assertThat(page.locator(".itinerary-empty")).isVisible();
    page.reload();
    assertThat(page.locator(".stop-details")).hasCount(0);

    // The plan built above can still be deleted from this same page.
    page.getByText("Eliminar plan", new Page.GetByTextOptions().setExact(true)).click();
    plans.waitForPath("/plans");
    assertEquals(0, plans.getPlanCount(), "the plan this test created has to be gone");
  }

  /**
   * [MAPS] The stop details dialog links out to Google Maps directions: the link is only rendered
   * when the stop has an address, and its href carries that address (URL-encoded), not raw
   * coordinates — the dataset stores address and coordinates out of sync, so the link follows the
   * address the dialog shows. The itinerary list offers the same link below the stop, so a stop can
   * be routed without opening the dialog.
   */
  @Test
  void showsTheMapsDirectionsLinkInTheStopDetails() {
    createPlanWithAVisit();

    Locator dialog = openStopDetails(0);
    Locator mapsLink = dialog.locator("a[href*='maps/dir']");
    assertThat(mapsLink).isVisible();
    // El destino es la direccion del lugar, url-encoded: al menos un caracter no vacio.
    assertThat(mapsLink)
      .hasAttribute("href", Pattern.compile(".*destination=([^&\\s]|%[0-9A-F]{2})+.*"));
    closeStopDetails();

    // El itinerario de abajo tambien ofrece el "Como llegar" del lugar.
    Locator itineraryLink = page.locator(".place-item").first().locator("a.stop-directions");
    assertThat(itineraryLink).isVisible();
    assertThat(itineraryLink).hasText("Cómo llegar");
    assertThat(itineraryLink)
      .hasAttribute("href", Pattern.compile(".*google\\.com/maps/dir/\\?api=1&destination=.+"));
  }

  /**
   * [DIS-B01] /plans/{id} carga Bootstrap en su propio &lt;head&gt; y las utilidades de Tailwind
   * del navbar compartido quedan por debajo. El navbar tiene que lucir igual que en el resto del
   * sitio: avatar redondo con la serif de la marca, fondo de token, gap real de gap-4 y wordmark
   * en text-xl.
   */
  @Test
  void keepsTheSharedNavbarOnThePlanDetailPage() {
    createPlanWithAVisit();

    Map<?, ?> nav = (Map<?, ?>) page.evaluate(
      """
      () => {
        const avatarStyle = getComputedStyle(document.querySelector('#navbar-avatar'));
        return {
          avatarRadius: parseFloat(avatarStyle.borderRadius),
          avatarFont: avatarStyle.fontFamily,
          navBg: getComputedStyle(document.querySelector('nav')).backgroundColor,
          menuGap: getComputedStyle(document.querySelector('nav .md\\\\:flex')).gap,
          brandSize: getComputedStyle(document.querySelector('nav .navbar-brand')).fontSize,
        };
      }
      """
    );

    assertTrue(
      ((Number) nav.get("avatarRadius")).doubleValue() > 1000,
      "el avatar del navbar tiene que seguir redondo (rounded-full), no un rectángulo de 6px"
    );
    assertTrue(
      nav.get("avatarFont").toString().contains("Bree Serif"),
      "el avatar hereda la serif de la marca, no la sans del body de Bootstrap"
    );
    assertEquals("rgb(17, 24, 39)", nav.get("navBg"), "fondo de token del nav, como en el resto");
    assertEquals(
      "16px",
      nav.get("menuGap"),
      "gap-4 es 1rem en Tailwind, no el 1.5rem de Bootstrap"
    );
    assertEquals("20px", nav.get("brandSize"), "wordmark en text-xl, como en el resto del sitio");
  }

  /**
   * [DIS-B02/B03/B04] El modal "Ver más" es una ficha con jerarquía: título + chip de categoría,
   * bloques con icono para dirección, descripciones, horario/duración/día y posición; el editor
   * de horario sigue ahí para quien puede editar el plan (en este arrange, el administrador).
   * Del lado del a11y el diálogo declara aria-labelledby, recibe el foco al abrir y se cierra
   * con Escape.
   */
  @Test
  void showsTheStopDetailsAsAStructuredFichaAndClosesWithEscape() {
    createPlanWithAVisit();

    Locator dialog = openStopDetails(0);

    // Título + chip de categoría con la clase cat-{categoría} que pinta el mapa.
    assertThat(dialog.locator("#place-detail-title")).isVisible();
    Locator chip = dialog.locator(".place-chip");
    assertThat(chip).isVisible();
    assertThat(chip).containsText(Pattern.compile("\\S"));
    assertThat(chip).hasAttribute("class", Pattern.compile("cat-\\S+"));

    // [MERGE] Un icono por bloque del dl: dirección, cómo llegar, 2 descripciones, horario,
    // duración, día y posición. La ficha de main y el horario legible de la feature se
    // conservan juntos, así que los 8 bloques del lugar del arrange (que tiene dirección)
    // llevan icono. El valor de fecha/hora viaja en el rango, arrancando en la hora de la parada.
    assertThat(dialog.locator(".place-facts dt svg")).hasCount(8);

    // Bloques de horario y descripciones con sus valores, posición al final del dl.
    assertThat(dialog).containsText("Horario de visita");
    assertThat(dialog).containsText("Duración");
    assertThat(dialog).containsText("Día");
    assertThat(dialog).containsText("Descripción del lugar");
    assertThat(dialog).containsText("Descripción de la visita");
    assertThat(dialog).containsText(START_TIME);
    assertThat(dialog.locator(".place-facts dd").last()).hasText("1");

    // [DIS-B03] owner/editor: el admin del arrange sigue viendo el editor de fecha/hora.
    assertThat(dialog.locator(".visit-editor input[type='date']")).isVisible();
    assertThat(dialog.locator(".visit-editor input[type='time']")).isVisible();

    // [DIS-B04] aria-labelledby y foco dentro del diálogo al abrir. El selector se ancla al
    // diálogo de este detalle para no depender del orden de los <dialog> del DOM.
    assertThat(dialog).hasAttribute("aria-labelledby", "place-detail-title");
    boolean focusInside = Boolean.TRUE.equals(
      page.evaluate(
        "() => document.querySelector('dialog[aria-labelledby=place-detail-title]').contains(document.activeElement)"
      )
    );
    assertTrue(focusInside, "abrir el diálogo tiene que dejar el foco adentro");

    // Escape cierra (comportamiento nativo del <dialog>).
    page.keyboard().press("Escape");
    page.waitForTimeout(150);
    boolean stillOpen = Boolean.TRUE.equals(
      page.evaluate(
        "() => document.querySelector('dialog[aria-labelledby=place-detail-title]').open"
      )
    );
    assertFalse(stillOpen, "Escape tiene que cerrar el diálogo");
  }

  /**
   * [STD-014] "Ver más" abre la ficha del lugar como una card más del tablero: centrada en el
   * viewport, con el mismo radio de 16 px, la misma superficie y sombra que las cards, y con
   * cada bloque del {@code dl} como chip de lectura (mismo fondo que los chips de la ficha del
   * plan). Antes el reset de márgenes de Tailwind dejaba el {@code <dialog>} nativo pegado al
   * borde superior izquierdo, con radio de 6 px y bloques separados por líneas.
   */
  @Test
  void opensThePlaceCardAsACenteredModalLikeTheRestOfTheCards() {
    createPlanWithAVisit();

    Locator dialog = openStopDetails(0);

    Map<String, Object> chrome = (Map<String, Object>) page.evaluate(
      """
      () => {
        const d = document.querySelector('dialog[aria-labelledby=place-detail-title]');
        const r = d.getBoundingClientRect();
        const s = getComputedStyle(d);
        const chip = getComputedStyle(document.querySelector('#plan-summary .plan-chip'));
        const fact = getComputedStyle(d.querySelector('.place-fact'));
        return {
          leftGap: r.left,
          rightGap: window.innerWidth - r.right,
          topGap: r.top,
          bottomGap: window.innerHeight - r.bottom,
          radius: s.borderTopLeftRadius,
          padding: s.paddingTop,
          shadow: s.boxShadow,
          background: s.backgroundColor,
          factBackground: fact.backgroundColor,
          factRadius: fact.borderTopLeftRadius,
          factBorderTop: fact.borderTopWidth,
          chipBackground: chip.backgroundColor
        };
      }
      """
    );

    double leftGap = ((Number) chrome.get("leftGap")).doubleValue();
    double rightGap = ((Number) chrome.get("rightGap")).doubleValue();
    assertTrue(
      Math.abs(leftGap - rightGap) <= 2,
      "la card tiene que quedar centrada en horizontal, no pegada a un borde: " + chrome
    );
    assertTrue(
      ((Number) chrome.get("topGap")).doubleValue() > 0 &&
      ((Number) chrome.get("bottomGap")).doubleValue() > 0,
      "la card no puede tocar el borde superior ni inferior del viewport: " + chrome
    );
    assertEquals("16px", chrome.get("radius"), "mismo radio que las cards del tablero");
    assertEquals("24px", chrome.get("padding"), "mismo aire interno que el resto de las cards");
    assertFalse(
      "none".equals(chrome.get("shadow")),
      "la card flota con sombra, como las demás: " + chrome
    );
    assertEquals(
      chrome.get("chipBackground"),
      chrome.get("factBackground"),
      "cada bloque de la ficha del lugar se lee como un chip de la ficha del plan"
    );
    assertEquals("10px", chrome.get("factRadius"), "los chips comparten radio con los del plan");
    assertEquals("0px", chrome.get("factBorderTop"), "los bloques ya no se separan con líneas");

    // El chip de categoría se lee igual que en la card del recorrido: label en español + icono,
    // no el enum crudo del catálogo.
    String cardCategory = page.locator(".place-item .stop-category").first().innerText().trim();
    String dialogCategory = dialog.locator(".place-chip").innerText().trim();
    assertEquals(
      cardCategory,
      dialogCategory,
      "el chip de la ficha del lugar repite el label de la card del recorrido"
    );
    assertThat(dialog.locator(".place-chip svg")).hasCount(1);
  }

  /**
   * [STD-001/003/004/005] La vista de dueño queda compacta: cada dato del plan aparece una sola
   * vez en el tablero (la ficha manda y el form de edición vive en la card de configuración), la
   * card de solicitudes avisa cuando no hay pendientes, "Invitar a alguien" está plegado dentro de
   * Participantes y "Agregar lugar al plan" es botón de la cabecera de "Tu recorrido".
   */
  @Test
  void keepsTheOwnerViewCompactAndWithoutRepeatedData() {
    createPlanWithAVisit();

    // AC-01: los values del form de edición no están en la vista base. La card de configuración
    // cerrada no cuenta: display:none la saca del texto visible y del filtro de inputs visibles.
    String visibleValues = (String) page.evaluate(
      """
      () => [...document.querySelectorAll('input, textarea, select')]
        .filter(el => el.type !== 'hidden' && el.offsetParent !== null)
        .map(el => el.value)
        .filter(Boolean)
        .join('|')
      """
    );
    assertFalse(
      visibleValues.contains(planName),
      "el nombre no vive en un input visible: " + visibleValues
    );
    assertFalse(visibleValues.contains(eventDate), "la fecha solo en la ficha: " + visibleValues);
    assertFalse(visibleValues.contains(START_TIME), "la hora solo en la ficha: " + visibleValues);

    // AC-01: la ficha es la fuente de lectura — 4 chips (fecha, hora, visibilidad, código) y una
    // descripción con clamp de 3 líneas; el tile ADMINISTRADOR ya no existe.
    Locator summary = page.locator("#plan-summary");
    assertThat(summary.locator(".plan-chip")).hasCount(4);
    assertThat(summary).containsText(dateLabel(eventDate));
    assertThat(summary).containsText(START_TIME);
    assertThat(summary).containsText("Privado");
    assertThat(summary).containsText("CÓDIGO");
    String lineClamp = (String) page.evaluate(
      "() => getComputedStyle(document.querySelector('#plan-description')).webkitLineClamp"
    );
    assertEquals("3", lineClamp, "la descripción de la ficha se clampa a 3 líneas");

    // AC-01: cada dato visible ocurre una sola vez. La hora del chip se cuenta dentro de la
    // ficha, no en la página: el recorrido también muestra una hora —la derivada de la parada,
    // que es otro dato—.
    String text = page.locator("body").innerText();
    assertEquals(1, occurrences(text, planName), "el nombre se ve una sola vez");
    assertEquals(1, occurrences(text, PLAN_DESCRIPTION), "la descripción se ve una sola vez");
    assertEquals(1, occurrences(text, dateLabel(eventDate)), "la fecha se ve una sola vez");
    assertEquals(
      1,
      occurrences(summary.innerText(), START_TIME),
      "la hora se ve una sola vez en la ficha"
    );
    assertFalse(summary.innerText().contains("ADMINISTRADOR"), "el admin sale de la ficha");
    assertThat(page.locator("input[name='eventDate']:visible")).hasCount(0);

    // AC-01: Participantes es la única fuente del administrador (badge "Dueño").
    assertThat(page.locator("section:has(#participants-heading)")).containsText("Dueño");

    // AC-03: sin solicitudes pendientes la card muestra el estado vacío (PLN #58) y el badge de
    // la cabecera no se pinta.
    assertTrue(
      text.contains("No hay solicitudes pendientes."),
      "la card avisa que no hay solicitudes"
    );
    assertThat(page.locator("#config-solicitudes")).isVisible();
    assertThat(page.locator(".plan-toolbar a[href='#config-solicitudes']")).hasCount(0);

    // AC-04: "Invitar a alguien" es un accordion dentro de la card centrada de Participantes:
    // colapsado, con aria-expanded, que despliega el form y lo vuelve a ocultar.
    plans.openParticipants();
    Locator inviteToggle = page.locator("button[aria-controls='invite-panel']");
    assertThat(inviteToggle).hasText("Invitar a alguien");
    assertThat(inviteToggle).hasAttribute("aria-expanded", "false");
    assertThat(page.locator("#invite-panel")).isHidden();
    inviteToggle.click();
    assertThat(inviteToggle).hasAttribute("aria-expanded", "true");
    assertThat(page.locator("#invitation-email")).isVisible();
    inviteToggle.click();
    assertThat(page.locator("#invite-panel")).isHidden();
    page.keyboard().press("Escape");
    assertThat(page.locator("#participants-dialog")).isHidden();

    // AC-05: "Agregar lugar al plan" es un botón de la cabecera de "Tu recorrido" que despliega
    // el picker; la card propia ya no existe.
    assertThat(page.locator("h5:has-text('Agregar lugar al plan')")).hasCount(0);
    Locator addToggle = page.locator(".itinerary-panel button[aria-controls='add-place-panel']");
    assertThat(addToggle).hasText("Agregar lugar al plan");
    assertThat(page.locator("#add-place-panel")).isHidden();
    addToggle.click();
    assertThat(page.getByLabel("Lugar para agregar al plan")).isVisible();
  }

  /**
   * [STD-002] La edición de metadatos se abre desde el botón "Editar" de la ficha en una card
   * centrada con el a11y del AC-18 de 17-DIS: aria-labelledby hacia su título, foco al abrir y
   * Escape cierra devolviendo el foco. Guarda con el mismo POST y la vista base no muestra form.
   */
  @Test
  void editsPlanMetadataFromAModalWithDialogA11y() {
    createPlanWithAVisit();

    Locator dialog = page.locator("#plan-config-dialog");
    Locator trigger = page.locator("#plan-summary button:has-text('Editar')");
    assertThat(trigger).isVisible();

    // Cerrada hasta que se pide: el form no está en el layout.
    assertThat(dialog).isHidden();
    assertThat(page.locator("input[name='eventDate']:visible")).hasCount(0);

    trigger.click();
    assertThat(dialog).isVisible();
    assertThat(dialog).hasAttribute("aria-labelledby", "configuracion-heading");
    boolean focusInside = Boolean.TRUE.equals(
      page.evaluate(
        "() => document.getElementById('plan-config-dialog').contains(document.activeElement)"
      )
    );
    assertTrue(focusInside, "abrir la configuración tiene que dejar el foco adentro");
    assertThat(page.locator("#config-datos input[name='eventDate']")).hasValue(eventDate);
    assertThat(page.locator("#config-datos input[name='name']")).hasValue(planName);

    // Escape cierra (comportamiento nativo del <dialog>) y devuelve el foco al botón.
    page.keyboard().press("Escape");
    page.waitForTimeout(150);
    assertThat(dialog).isHidden();
    assertThat(trigger).isFocused();

    // Round trip: cambiar la hora desde la card y guardar rederige con el valor nuevo en la
    // ficha, y la vista base sigue sin form visible.
    trigger.click();
    page.locator("#config-datos input[name='eventTime']").fill(PLAN_TIME);
    page.locator("#config-datos button:has-text('Guardar cambios')").click();
    plans.waitForDetailPath();
    assertThat(page.locator("#plan-summary")).containsText(PLAN_TIME);
    assertThat(page.locator("input[name='eventTime']:visible")).hasCount(0);
  }

  /**
   * [STD-011/AC-10] Variante C (2026-11-03, intercambio del 2026-11-04): tablero de tres columnas
   * —ficha fija a la izquierda con Participantes debajo, mapa al centro y "Tu recorrido" a la
   * derecha—, sin pestañas: las tres cards se ven al cargar. La ficha no se mueve al scrollear el
   * recorrido y la página sigue bajo el presupuesto de scroll de la spec: ≤1.500 px en desktop y
   * ≤2.800 px en 390.
   */
  @Test
  void laysOutTheFichaFixedOnTheLeftWithParticipantsUnderItAndTheItineraryOnTheRight() {
    createPlanWithAVisit();
    page.waitForLoadState(LoadState.NETWORKIDLE);
    page.waitForTimeout(300);

    // Desktop: las tres cards visibles, cada una en su columna.
    page.setViewportSize(1440, 900);
    page.waitForTimeout(300);
    assertThat(page.locator("#plan-summary")).isVisible();
    assertThat(page.locator(".itinerary-panel")).isVisible();
    assertThat(page.locator("#participants-section")).isVisible();

    Locator ficha = page.locator("#plan-summary");
    Locator mapa = page.locator("#map");
    Locator recorrido = page.locator(".itinerary-panel");
    Locator participantes = page.locator("#participants-section");
    double fichaX = ficha.boundingBox().x;
    double mapaX = mapa.boundingBox().x;
    double participantesX = participantes.boundingBox().x;
    double recorridoX = recorrido.boundingBox().x;
    assertTrue(fichaX < mapaX, "la ficha va a la izquierda del mapa");
    assertTrue(mapaX < recorridoX, "el recorrido va a la derecha del mapa");
    assertTrue(participantesX < mapaX, "participantes va a la izquierda del mapa");
    assertEquals(fichaX, participantesX, 1.0, "participantes comparte la columna de la ficha");
    assertTrue(
      participantes.boundingBox().y > ficha.boundingBox().y,
      "participantes va debajo de la ficha"
    );
    assertTrue(
      recorrido.boundingBox().y <= ficha.boundingBox().y,
      "el recorrido arranca a la altura de la ficha, en su propia columna"
    );

    // [STD-015/AC-14] La banda del título termina donde termina el mapa: el encabezado usa las
    // mismas tres columnas del tablero y las acciones viven en la tercera. La configuración se
    // abre desde "Editar" en la ficha, así que la cabecera ya no la repite: quedan Compartir y
    // Eliminar plan, con el mismo alto.
    Locator banda = page.locator(".plan-header");
    double bandaRight = banda.boundingBox().x + banda.boundingBox().width;
    double mapaRight = mapa.boundingBox().x + mapa.boundingBox().width;
    assertEquals(mapaRight, bandaRight, 2.0, "la banda azul termina donde termina el mapa");

    assertThat(page.locator(".plan-toolbar button[aria-controls='plan-config-dialog']"))
      .hasCount(0);
    assertThat(page.locator("#plan-summary button[aria-controls='plan-config-dialog']"))
      .isVisible();
    double compartirH = page
      .getByRole(
        com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Compartir")
      )
      .boundingBox()
      .height;
    double eliminarH = page
      .getByRole(
        com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Eliminar plan")
      )
      .boundingBox()
      .height;
    assertEquals(compartirH, eliminarH, 1.0, "las acciones de la cabecera comparten alto");

    // La ficha queda quieta: scrollear el recorrido no la mueve.
    double fichaY = ficha.boundingBox().y;
    page.locator(".itinerary-panel").evaluate("el => el.scrollTop = el.scrollHeight");
    page.waitForTimeout(200);
    assertEquals(fichaY, ficha.boundingBox().y, "la ficha no se mueve al scrollear el recorrido");

    // Presupuesto desktop.
    long desktop = scrollHeight();
    assertTrue(desktop <= 1500, "desktop quedó en " + desktop + " px, techo 1500");

    // Mobile 390: apilado ficha -> mapa -> participantes -> recorrido, bajo el techo.
    page.setViewportSize(390, 844);
    page.waitForTimeout(300);
    double yFicha = ficha.boundingBox().y;
    double yMapa = mapa.boundingBox().y;
    double yParticipantes = participantes.boundingBox().y;
    double yRecorrido = recorrido.boundingBox().y;
    assertTrue(yFicha < yMapa, "390: la ficha va primero");
    assertTrue(yMapa < yParticipantes, "390: participantes va antes que el recorrido");
    assertTrue(yParticipantes < yRecorrido, "390: el recorrido va último");
    long mobile = scrollHeight();
    assertTrue(mobile <= 2800, "390 quedó en " + mobile + " px, techo 2800");
  }

  /**
   * The create panel of {@code /explore}. Step 1 owns the plan's date and start time — the stop
   * does not ask for an hour any more — so the schedule the detail shows comes from these two.
   */
  private void createPlanWithAVisit() {
    plans.navigateToCreatePlanModal();
    plans.typeName(planName);
    plans.typeDescription(PLAN_DESCRIPTION);
    plans.typeEventDate(eventDate);
    plans.typeStartTime(START_TIME);
    plans.clickNextStep();
    plans.addFirstDraftPlace();
    plans.clickNextStep();
    plans.clickCreate();
    plans.waitForDetailPath();
  }

  /**
   * Detail page form: picks the first place the itinerary does not hold yet and adds it.
   * [STD-005] El picker vive detrás de un botón en la cabecera del recorrido: si el toggle
   * existe y el panel está colapsado se expande primero (idempotente en los dos estados).
   */
  private void addPlaceToPlan() {
    Locator addToggle = page.locator(".itinerary-panel button[aria-controls='add-place-panel']");
    if (addToggle.count() > 0 && !page.locator("#add-place-panel").isVisible()) {
      addToggle.click();
    }
    page.getByLabel("Lugar para agregar al plan").selectOption(new SelectOption().setIndex(1));
    page.getByText("+ Agregar", new Page.GetByTextOptions().setExact(true)).click();
  }

  private Locator openStopDetails(int index) {
    Locator dialog = page.locator("dialog[aria-labelledby='place-detail-title']");
    page.locator(".stop-details").nth(index).click();
    assertThat(dialog).isVisible();
    return dialog;
  }

  private void closeStopDetails() {
    Locator dialog = page.locator("dialog[aria-labelledby='place-detail-title']");
    dialog.locator(".btn-close").click();
    // Do not let the next action race the native dialog backdrop.
    assertThat(dialog).isHidden();
  }

  /**
   * Playwright's pointer-driven {@code dragTo} can skip the HTML5 drag events on headless CI and
   * leave the response wait hanging. Dispatch the same events the Vue component listens to, so the
   * handlers and the reorder POST remain covered without depending on pointer hit-testing.
   */
  private Response reorderFirstTwoStops() {
    return page.waitForResponse(
      "**/reorder",
      () ->
        page.evaluate(
          """
          () => {
            const items = document.querySelectorAll('.place-item');
            const dataTransfer = new DataTransfer();
            const event = (type) => new DragEvent(type, {
              bubbles: true,
              cancelable: true,
              dataTransfer
            });
            items[0].dispatchEvent(event('dragstart'));
            items[1].dispatchEvent(event('dragover'));
            items[1].dispatchEvent(event('drop'));
            items[0].dispatchEvent(event('dragend'));
          }
          """
        )
    );
  }

  /**
   * The only way to pin a day or a time after the plan exists: the detail page renders the
   * schedule read-only, so the change goes to {@code PUT /api/plans/{id}/places}/{@code {entryId}}
   * with the CSRF token the page carries, the same way the Vue app itself does. Sending a date or
   * a time pins the stop (the server re-derives the rest of the chain from it).
   */
  private void updateVisitDetails(String isoDate, String time) {
    Object updated = page.evaluate(
      """
      async body => {
        const planId = location.pathname.split('/').pop();
        const url = '/api/plans/' + planId + '/places';
        const entries = await (await fetch(url)).json();
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        if (!headerMeta || !tokenMeta) {
          throw new Error('the plan detail page must carry the _csrf metas');
        }
        const header = headerMeta.content;
        const token = tokenMeta.content;
        const response = await fetch(url + '/' + entries[0].id, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', [header]: token },
          body
        });
        return response.ok;
      }
      """,
      String.format("{\"visitDate\":\"%s\",\"visitTime\":\"%s\"}", isoDate, time)
    );
    assertEquals(
      Boolean.TRUE,
      updated,
      "the itinerary endpoint has to accept a new visit date and time"
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

  private long scrollHeight() {
    return ((Number) page.evaluate("() => document.documentElement.scrollHeight")).longValue();
  }

  private void screenshotItinerary() {
    page.screenshot(
      new Page.ScreenshotOptions()
        .setPath(Path.of("target/itinerary-desktop.png"))
        .setFullPage(true)
    );
    page.setViewportSize(390, 844);
    page.locator(".itinerary-panel").scrollIntoViewIfNeeded();
    page.screenshot(
      new Page.ScreenshotOptions().setPath(Path.of("target/itinerary-mobile.png")).setFullPage(true)
    );
    Object fitsOnPhone = page.evaluate(
      "() => { const p = document.querySelector('.itinerary-panel');" +
      " return p.scrollWidth <= p.clientWidth; }"
    );
    assertEquals(Boolean.TRUE, fitsOnPhone, "the itinerary must not scroll sideways on a phone");
    page.setViewportSize(1280, 720);
  }
}
