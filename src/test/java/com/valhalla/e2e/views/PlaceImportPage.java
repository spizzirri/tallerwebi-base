package com.valhalla.e2e.views;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for {@code /admin/places/import}: la lista de POIs, los markers del mapa y el mensaje
 * de error. La lista es la via de texto para elegir un punto, igual que el click en el marker.
 */
public class PlaceImportPage extends WebPage {

  public PlaceImportPage(Page page) {
    super(page);
  }

  public void navigateToImport() {
    page.navigate(baseUrl() + "/admin/places/import");
  }

  /** Espera a que la lista tenga exactamente {@code count} POIs: el fetch resuelve despues del load. */
  public void waitForPois(int count) {
    page.waitForFunction(
      "(n) => document.querySelectorAll('#app > aside ul li').length === n",
      count
    );
  }

  public void waitForError() {
    page.waitForSelector("#app > aside p[role='alert']");
  }

  public int poiCount() {
    return page.locator("#app > aside ul li").count();
  }

  public String statusText() {
    return this.getElementText("#app > aside p[aria-live='polite']").trim();
  }

  /** Un POI sin coordenadas no se dibuja, asi que el total puede ser menor que el de la lista. */
  public int markerCount() {
    return page.locator("#map path.leaflet-interactive").count();
  }

  public boolean hasError() {
    return page.locator("#app > aside p[role='alert']").count() > 0;
  }

  public String errorText() {
    return this.getElementText("#app > aside p[role='alert']").trim();
  }

  public void selectPoi(String name) {
    page
      .locator("#app > aside ul li a")
      .filter(new Locator.FilterOptions().setHasText(name))
      .click();
  }

  /** Filtro local por nombre: escribe en el input y Vue re-renderiza la lista. */
  public void filterByName(String term) {
    this.typeIntoElement("#poi-search", term);
  }
}
