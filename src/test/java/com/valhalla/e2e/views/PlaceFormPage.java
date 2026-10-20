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

  public String getName() {
    return page.inputValue("#name");
  }

  public String getCategory() {
    return page.inputValue("#category");
  }

  public String getAddress() {
    return page.inputValue("#address");
  }

  public String getLatitude() {
    return page.inputValue("#latitude");
  }

  public String getLongitude() {
    return page.inputValue("#longitude");
  }

  public String getOpeningHours() {
    return page.inputValue("#openingHours");
  }

  public String getPhone() {
    return page.inputValue("#phone");
  }

  public String getWebsite() {
    return page.inputValue("#website");
  }

  public void typeOpeningHours(String openingHours) {
    this.typeIntoElement("#openingHours", openingHours);
  }

  public void typePhone(String phone) {
    this.typeIntoElement("#phone", phone);
  }

  public void typeWebsite(String website) {
    this.typeIntoElement("#website", website);
  }

  /** El mismo boton crea y actualiza; el label (Create/Update) cambia con {@code isEdit}. */
  public void clickSave() {
    this.clickElement("#btn-create");
  }
}
