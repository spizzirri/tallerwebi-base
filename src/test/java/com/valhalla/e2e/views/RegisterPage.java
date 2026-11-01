package com.valhalla.e2e.views;

import com.microsoft.playwright.Page;

/** Self-service registration: first name, last name, email, password and confirmation. */
public class RegisterPage extends WebPage {

  public RegisterPage(Page page) {
    super(page, "/auth/register");
  }

  public String getErrorMessage() {
    return this.getElementText("p.alert.alert-danger");
  }

  /**
   * Server-side field error, rendered by {@code th:errors} in a {@code span.text-red-600}. Distinct
   * from {@link #getErrorMessage()}, which is the whole-form alert the controller puts in the
   * model. Only one field can carry an error per submit, so the selector needs no field name.
   */
  public String getFieldError() {
    return this.getElementText("span.text-red-600");
  }

  /**
   * Whether the browser itself rejected the email, before anything was sent. The input is
   * {@code type="email"}, so a malformed value never reaches the server: this is what a user sees
   * instead of the {@code @Email} message on the DTO.
   */
  public boolean isEmailRejectedByBrowser() {
    return (Boolean) this.page.locator("#email").evaluate("el => !el.validity.valid");
  }

  public void typeEmail(String email) {
    this.typeIntoElement("#email", email);
  }

  public void typePassword(String password) {
    this.typeIntoElement("#password", password);
  }

  public void typeConfirmPassword(String password) {
    this.typeIntoElement("#confirmPassword", password);
  }

  public void typeFirstName(String firstName) {
    this.typeIntoElement("#firstName", firstName);
  }

  public void typeLastName(String lastName) {
    this.typeIntoElement("#lastName", lastName);
  }

  public void clickRegister() {
    this.clickElement("#btn-register");
  }
}
