package com.valhalla.e2e.views;

import com.microsoft.playwright.Page;

public class LoginPage extends WebPage {

  public LoginPage(Page page) {
    this(page, "/admin/login");
  }

  /**
   * @param path login page to open: {@code /admin/login} or {@code /auth/login}
   *             [AUT-01].
   */
  public LoginPage(Page page, String path) {
    super(page);
    page.navigate(baseUrl() + path);
  }

  public String getErrorMessage() {
    return this.getElementText("p.alert.alert-danger.my-4");
  }

  /**
   * Notice shown after a successful logout. Spring Security redirects to
   * {@code ?logout=true}, and
   * the view keys the notice off that parameter, so its presence proves the whole
   * round trip
   * finished. A path assertion cannot: the redirect target carries a query
   * string, which
   * {@link #waitForPath} does not match.
   */
  public String getLogoutNotice() {
    return this.getElementText("p.bg-green-100");
  }

  public void typeEmail(String email) {
    this.typeIntoElement("#email", email);
  }

  public void typePassword(String password) {
    this.typeIntoElement("#password", password);
  }

  public void clickSignIn() {
    this.clickElement("#btn-login");
  }

  /**
   * [PROF] El logout ya no vive directo en el navbar: está dentro del menú modal
   * (components/navbar-menu.html), detrás del botón avatar. El helper abre el menú y recién
   * entonces clica el botón rojo "Cerrar sesion" (id único btn-logout).
   */
  public void clickLogout() {
    this.clickElement("#navbar-avatar");
    this.page.waitForSelector("#navbar-menu:not([hidden]) #btn-logout");
    this.clickElement("#navbar-menu:not([hidden]) #btn-logout");
  }

  public void clickRegister() {
    this.clickElement("#btn-register");
  }

  public void waitForSessionTimerToTick() {
    this.page.waitForFunction(
        "() => { const el = document.getElementById('session-clock-app'); " +
        "return el !== null && el.textContent.includes(':') " +
        "&& !el.textContent.includes('0:00'); }"
      );
  }
}
