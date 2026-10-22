package com.valhalla.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.*;
import com.valhalla.e2e.views.UserFormPage;
import com.valhalla.e2e.views.UsersPage;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserViewABME2E extends E2eBase {

  UsersPage usersPage;

  @BeforeEach
  void loginAsAdmin() {
    signInAsAdmin();
    usersPage = new UsersPage(page);
    usersPage.navigateToUsers();
  }

  @Test
  void shouldListExistingAdminUser() throws MalformedURLException {
    givenAdminIsOnUsersPage();
    thenShouldSeeHeading("Users");
    thenShouldSeeAtLeastOneUser();
    thenShouldSeeRowWithEmail("test@unlam.edu.ar");
  }

  @Test
  void shouldCreateNewUserAndSeeItInTheList() throws MalformedURLException {
    givenAdminIsOnUsersPage();
    whenAdminClicksNewUser();
    thenShouldSeeHeading("New User");
    whenAdminFillsForm("newe2e@unlam.edu.ar", "USER");
    whenAdminClicksCreate();
    thenShouldBeRedirectedToUsersList();
    thenShouldSeeRowWithEmail("newe2e@unlam.edu.ar");
  }

  @Test
  void shouldEditExistingUser() throws MalformedURLException {
    givenAdminIsOnUsersPage();
    whenAdminClicksNewUser();
    whenAdminFillsForm("editable@unlam.edu.ar", "USER");
    whenAdminClicksCreate();
    thenShouldBeRedirectedToUsersList();

    whenAdminClicksEditOnRow("editable@unlam.edu.ar");
    thenShouldSeeHeading("Edit User");
    whenAdminFillsForm("edited@unlam.edu.ar", "ADMIN");
    whenAdminClicksUpdate();
    thenShouldBeRedirectedToUsersList();
    thenShouldSeeRowWithEmail("edited@unlam.edu.ar");
  }

  @Test
  void shouldDeactivateAndThenDeleteUser() throws MalformedURLException {
    givenAdminIsOnUsersPage();
    whenAdminClicksNewUser();
    whenAdminFillsForm("deactivatable@unlam.edu.ar", "USER");
    whenAdminClicksCreate();
    thenShouldBeRedirectedToUsersList();
    thenShouldSeeRowWithEmail("deactivatable@unlam.edu.ar");

    whenAdminClicksDeactivateOnRow("deactivatable@unlam.edu.ar");
    thenShouldBeRedirectedToUsersList();
    thenShouldSeeInactiveOnRow("deactivatable@unlam.edu.ar");

    whenAdminClicksDeleteOnRow("deactivatable@unlam.edu.ar");
    thenShouldBeRedirectedToUsersList();
    thenShouldNotSeeRowWithEmail("deactivatable@unlam.edu.ar");
  }

  // --- given ---

  private void givenAdminIsOnUsersPage() throws MalformedURLException {
    usersPage.waitForPath("/admin/users");
    URL url = usersPage.getCurrentUrl();
    assertThat(url.getPath(), matchesPattern("^/admin/users(?:;jsessionid=[^/\\s]+)?$"));
  }

  // --- when ---

  private void whenAdminClicksNewUser() {
    usersPage.clickNewUser();
  }

  private void whenAdminClicksEditOnRow(String email) {
    usersPage.clickEditOnRow(email);
  }

  private void whenAdminClicksDeactivateOnRow(String email) {
    usersPage.clickDeactivateOnRow(email);
  }

  private void whenAdminClicksDeleteOnRow(String email) {
    usersPage.clickDeleteOnRow(email);
  }

  private void whenAdminFillsForm(String email, String role) {
    UserFormPage formPage = new UserFormPage(context.pages().get(0));
    formPage.typeFirstName("Test");
    formPage.typeLastName("User");
    formPage.typeEmail(email);
    formPage.selectRole(role);
  }

  private void whenAdminClicksCreate() {
    UserFormPage formPage = new UserFormPage(context.pages().get(0));
    formPage.clickCreate();
  }

  private void whenAdminClicksUpdate() {
    UserFormPage formPage = new UserFormPage(context.pages().get(0));
    formPage.clickCreate();
  }

  // --- then ---

  private void thenShouldSeeHeading(String expected) throws MalformedURLException {
    String heading;
    String path = usersPage.getCurrentUrl().getPath();
    boolean onFormPage = path.contains("/users/new") || path.matches(".*/users/[0-9a-f-]{36}/edit");
    if (onFormPage) {
      heading = new UserFormPage(context.pages().get(0)).getHeading();
    } else {
      heading = usersPage.getHeading();
    }
    assertThat(heading, equalToIgnoringCase(expected));
  }

  private void thenShouldSeeAtLeastOneUser() {
    assertThat(usersPage.getUserCount(), is(greaterThan(0)));
  }

  private void thenShouldSeeRowWithEmail(String email) {
    usersPage.waitForPath("/admin/users");
    assertThat(usersPage.hasRowWithEmail(email), is(true));
  }

  private void thenShouldNotSeeRowWithEmail(String email) {
    usersPage.waitForPath("/admin/users");
    assertThat(usersPage.hasRowWithEmail(email), is(false));
  }

  private void thenShouldBeRedirectedToUsersList() {
    usersPage.waitForPath("/admin/users");
  }

  private void thenShouldSeeInactiveOnRow(String email) {
    usersPage.waitForDeactivated(email);
  }
}
