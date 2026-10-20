package com.valhalla.e2e.views;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Page object for the {@code /admin/places} listing, mirror of {@link UsersPage}. */
public class PlacesAdminPage extends WebPage {

  public PlacesAdminPage(Page page) {
    super(page);
  }

  public void navigateToPlaces() {
    page.navigate(baseUrl() + "/admin/places");
  }

  public String getHeading() {
    return this.getElementText("h2");
  }

  public void clickNewPlace() {
    this.clickElement("a[href='/admin/places/new']");
  }

  public boolean hasRowWithName(String name) {
    return this.rowWith(name).count() > 0;
  }

  public void clickEditOnRow(String name) {
    this.rowWith(name).locator("a:has-text('Edit')").click();
  }

  /** La baja es el form por fila, que postea {@code _method=DELETE} a /admin/places/{id}/delete. */
  public void clickDeleteOnRow(String name) {
    this.rowWith(name).locator("button:has-text('Delete')").click();
  }

  private Locator rowWith(String name) {
    return this.page.locator("tbody tr").filter(new Locator.FilterOptions().setHasText(name));
  }
}
