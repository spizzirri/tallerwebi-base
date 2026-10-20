package com.valhalla.e2e.views;

import com.microsoft.playwright.Page;

/** Page object for {@code /admin/places/new} and {@code /admin/places/{id}/edit}. */
public class PlaceFormPage extends WebPage {

  public PlaceFormPage(Page page) {
    super(page);
  }

  public String getHeading() {
    return this.getElementText("h3");
  }

  public void typeName(String name) {
    this.typeIntoElement("#name", name);
  }

  public void selectCategory(String category) {
    this.page.selectOption("#category", category);
    this.page.locator("#category")
      .evaluate("el => el.dispatchEvent(new Event('change', {bubbles: true}))");
  }

  public void typeLatitude(String latitude) {
    this.typeIntoElement("#latitude", latitude);
  }

  public void typeLongitude(String longitude) {
    this.typeIntoElement("#longitude", longitude);
  }

  /** El mismo boton crea y actualiza; el label (Create/Update) cambia con {@code isEdit}. */
  public void clickSave() {
    this.clickElement("#btn-create");
  }
}
