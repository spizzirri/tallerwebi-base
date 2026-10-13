package com.valhalla.e2e.views;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.regex.Pattern;

/** Page object for the {@code /plans} views and the create-plan modal on {@code /explore}. */
public class PlansPage extends WebPage {

  private static final String LIST_PATH = "/plans";
  private static final String EXPLORE_PATH = "/explore";

  public PlansPage(Page page) {
    super(page);
  }

  public void navigateToPlans() {
    page.navigate(baseUrl() + LIST_PATH);
  }

  /**
   * La creación de planes vive en el modal de /explore (ya no existe /plans/new): navega a esa
   * vista y abre el modal con su botón "Create plan" del panel lateral.
   */
  public void navigateToCreatePlanModal() {
    navigateToExplore();
    clickElement("aside[aria-label='Plans'] button:has-text('Create plan')");
  }

  public void navigateToExplore() {
    page.navigate(baseUrl() + EXPLORE_PATH);
  }

  /** Paso 1 del modal: los datos del plan. El formulario no es nativo, así que los input de Vue se llenan igual que el resto. */
  public void typeName(String name) {
    this.typeIntoElement("#plan-name", name);
  }

  public void typeDescription(String description) {
    this.typeIntoElement("#plan-description", description);
  }

  /** {@code <input type="date">}, so the value has to be an ISO date. */
  public void typeEventDate(String isoDate) {
    this.typeIntoElement("#plan-date", isoDate);
  }

  /** Paso 1 -> paso 2 (selección de lugares): el formulario se envía al avanzar. */
  public void clickNextStep() {
    this.clickElement("button:has-text('Siguiente')");
  }

  /**
   * Paso 2: agrega el primer lugar del catálogo al borrador del plan. El botón + vive en cada
   * tarjeta de `<article>` del panel derecho (no en un `<li>`), y solo renderiza mientras el
   * panel de creación está abierto.
   */
  public void addFirstDraftPlace() {
    this.page.locator("#app > aside article button[aria-label^='Add ']").first().click();
  }

  /** Paso 2: envía el formulario del modal (POST /plans). */
  public void clickCreate() {
    this.clickElement("button:has-text('Crear plan')");
  }

  /** Flujo completo de creación: modal de /explore -> detalle del plan. */
  public void createPlanViaExplore(String name, String description, String isoDate) {
    navigateToCreatePlanModal();
    typeName(name);
    typeDescription(description);
    typeEventDate(isoDate);
    clickNextStep();
    addFirstDraftPlace();
    clickCreate();
    waitForDetailPath();
  }

  /**
   * [PPV] Mismo flujo pero marcando "Hacer público este plan" (checkbox del paso 1), para que el
   * listado público tenga una tarjeta que mostrar.
   */
  public void createPublicPlanViaExplore(String name, String description, String isoDate) {
    navigateToCreatePlanModal();
    typeName(name);
    typeDescription(description);
    typeEventDate(isoDate);
    this.page.locator("#plan-public").check();
    clickNextStep();
    addFirstDraftPlace();
    clickCreate();
    waitForDetailPath();
  }

  /**
   * The create handler redirects to {@code /plans/{id} } and the id comes from the
   * backend, so the detail page can only be awaited as a shape. {@link #waitForPath(String)}
   * quotes its argument and therefore cannot express it, and a glob ending in {@code /plans}
   * never matched this URL — which is exactly why the delete assertion below used to run
   * against the list page.
   */
  public void waitForDetailPath() {
    page.waitForURL(
      Pattern.compile(
        "[a-zA-Z][a-zA-Z0-9+.-]*://[^/]*" + Pattern.quote(LIST_PATH) + "/\\d+(?:[?;][^#]*)?$"
      )
    );
  }

  public String getDetailHeading() {
    return this.getElementText("main h2");
  }

  /** Rows of the listing table. Zero is the only value that proves a delete really deleted. */
  public int getPlanCount() {
    return this.page.locator("tbody tr").count();
  }

  public boolean hasPlanNamed(String name) {
    return (
      this.page.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(name)).count() > 0
    );
  }

  public void clickDetailOnRow(String name) {
    this.rowWith(name).locator("a:has-text('Ver detalle')").click();
  }

  /** The per-row delete form, which posts {@code _method=DELETE} to {@code /plans/{id}/delete}. */
  public void deletePlanNamed(String name) {
    this.rowWith(name).locator("form button[type=submit]").click();
  }

  private Locator rowWith(String name) {
    return this.page.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(name));
  }
}
