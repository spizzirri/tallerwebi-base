package com.valhalla.e2e.views;

import com.microsoft.playwright.Page;

/** Password recovery request form (AC-07). */
public class ForgotPasswordPage extends WebPage {

  public ForgotPasswordPage(Page page) {
    super(page, "/auth/forgot-password");
  }

  public String getErrorMessage() {
    return this.getElementText("p.alert.alert-danger");
  }

  public void typeEmail(String email) {
    this.typeIntoElement("#email", email);
  }

  public void clickRecover() {
    this.clickElement("#btn-recover");
  }
}
