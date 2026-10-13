package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import com.microsoft.playwright.*;
import com.valhalla.e2e.views.LoginPage;
import com.valhalla.e2e.views.UserFormPage;
import com.valhalla.presentation.shared.LoginRedirects;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginViewE2E extends E2eBase {

  LoginPage loginPage;

  @BeforeEach
  void openLoginPage() {
    loginPage = new LoginPage(page);
  }

  @Test
  void shouldShowPlanItInTheNavbar() throws MalformedURLException {
    givenUserIsOnLoginPage();
    thenShouldSeePlanItInNavbar();
  }

  @Test
  void shouldShowErrorWhenSigningInWithAnUnknownUser() {
    givenUserFillsLoginFormWith("damian@unlam.edu.ar", "unlam");
    whenUserClicksSignIn();
    thenShouldSeeAnErrorMessage();
  }

  @Test
  void shouldLogoutAndReturnToLoginPage() throws MalformedURLException {
    givenUserFillsLoginFormWith("test@unlam.edu.ar", "password");
    whenUserClicksSignIn();
    thenShouldBeRedirectedToHome();
    whenUserClicksLogout();
    // Notice before path: the locator auto-waits, which absorbs the redirect race, and it is the
    // only assertion here that can tell a completed logout from any other reason to be back on
    // the login form. A path check alone cannot.
    thenShouldSeeSignedOutNotice();
    thenShouldBeRedirectedToLogin();
  }

  @Test
  void shouldRegisterAUserAndSignInSuccessfully() throws MalformedURLException {
    // Registration is admin-only now — create user via admin panel, then login
    String generatedPassword = givenAdminCreatesUser("juan@unlam.edu.ar");
    givenUserIsOnLoginPage();
    givenUserFillsLoginFormWith("juan@unlam.edu.ar", generatedPassword);
    whenUserClicksSignIn();
    // AC-04: juan is a USER, so LoginRedirects lands him on USER_LANDING and not /admin/home.
    // Waiting for /admin/home here is what left this job red in CI (P-01); waiting for "/"
    // became equally wrong when 5839988 moved the USER landing to /explore.
    thenShouldBeRedirectedToLanding();
  }

  private void thenShouldSeePlanItInNavbar() {
    String text = loginPage.getNavbarBrand();
    assertThat("PlanIt", equalToIgnoringCase(text));
  }

  private void givenUserIsOnLoginPage() throws MalformedURLException {
    URL loginUrl = loginPage.getCurrentUrl();
    assertThat(loginUrl.getPath(), matchesPattern("^/admin/login(?:;jsessionid=[^/\\s]+)?$"));
  }

  private void whenUserClicksSignIn() {
    loginPage.clickSignIn();
  }

  private void whenUserClicksLogout() {
    loginPage.clickLogout();
  }

  private void thenShouldBeRedirectedToLogin() throws MalformedURLException {
    loginPage.waitForPath("/admin/login");
    URL url = loginPage.getCurrentUrl();
    assertThat(url.getPath(), matchesPattern("^/admin/login(?:;jsessionid=[^/\\s]+)?$"));
  }

  private void thenShouldSeeSignedOutNotice() {
    assertThat(
      "the notice only renders on a completed logout",
      loginPage.getLogoutNotice(),
      containsString("You have been signed out")
    );
  }

  private void thenShouldBeRedirectedToHome() throws MalformedURLException {
    loginPage.waitForPath("/admin/home");
    URL url = loginPage.getCurrentUrl();
    assertThat(url.getPath(), matchesPattern("^/admin/home(?:;jsessionid=[^/\\s]+)?$"));
    loginPage.waitForSessionTimerToTick();
  }

  private void thenShouldBeRedirectedToLanding() throws MalformedURLException {
    // Contra la constante, no un literal: tolera el cambio de landing (p. ej. /plans/public).
    loginPage.waitForPath(LoginRedirects.USER_LANDING);
    URL url = loginPage.getCurrentUrl();
    assertThat(
      url.getPath(),
      matchesPattern(
        "^" + Pattern.quote(LoginRedirects.USER_LANDING) + "(?:;jsessionid=[^/\\s]+)?$"
      )
    );
  }

  private void thenShouldSeeAnErrorMessage() {
    String text = loginPage.getErrorMessage();
    assertThat("Invalid email or password", equalToIgnoringCase(text));
  }

  private void givenUserFillsLoginFormWith(String email, String password) {
    loginPage.typeEmail(email);
    loginPage.typePassword(password);
  }

  private String givenAdminCreatesUser(String email) {
    LoginPage adminLogin = signInAsAdmin();
    adminLogin.navigate(adminLogin.baseUrl() + "/admin/users/new");
    UserFormPage formPage = new UserFormPage(page);
    formPage.typeFirstName("Juan");
    formPage.typeLastName("Perez");
    formPage.typeEmail(email);
    formPage.selectRole("USER");
    formPage.clickCreate();
    formPage.waitForPath("/admin/users");
    // Read the generated password displayed on the users page
    String generatedPassword = page.locator("code").textContent();
    newContext();
    loginPage = new LoginPage(page);
    return generatedPassword;
  }
}
