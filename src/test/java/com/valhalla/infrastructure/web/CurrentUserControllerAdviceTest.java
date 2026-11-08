package com.valhalla.infrastructure.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import com.valhalla.infrastructure.security.AppUserDetails;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Unit tests of {@link CurrentUserControllerAdvice}. Every request renders the navbar through
 * these attributes, so each principal the security layer can hand over gets its own case:
 * a real login (the token Spring Security stores, whose inside is an {@link AppUserDetails}), a
 * synthetic MockMvc principal, and no session at all.
 */
public class CurrentUserControllerAdviceTest {

  private static final String EMAIL = "ana@test.com";

  private CurrentUserControllerAdvice advice;
  private UserService userService;
  private com.valhalla.domain.notification.NotificationService notificationService;

  @BeforeEach
  public void init() {
    userService = Mockito.mock(UserService.class);
    notificationService = Mockito.mock(com.valhalla.domain.notification.NotificationService.class);
    advice = new CurrentUserControllerAdvice(userService, notificationService);
  }

  // --- userInitial / userDisplayName ---

  @Test
  public void shouldShowTheInitialOfTheRealFirstNameAndTheFullName() {
    Authentication principal = appUser("Ana", "Perez");

    assertThat(advice.userInitial(principal), is("A"));
    assertThat(advice.userDisplayName(principal), is("Ana Perez"));
  }

  @Test
  public void shouldShowTheFirstNameAloneWhenThereIsNoSurname() {
    assertThat(advice.userDisplayName(appUser("Ana", null)), is("Ana"));
  }

  @Test
  public void shouldFallBackToTheLoginNameWhenTheRealNameIsMissing() {
    assertThat(advice.userDisplayName(appUser(null, null)), is(EMAIL));
    assertThat(advice.userInitial(appUser(null, null)), is("A"));
  }

  @Test
  public void shouldFallBackToTheLoginNameForASyntheticPrincipal() {
    Authentication principal = new UsernamePasswordAuthenticationToken(EMAIL, null);

    assertThat(advice.userInitial(principal), is("A"));
    assertThat(advice.userDisplayName(principal), is(EMAIL));
  }

  @Test
  public void shouldShowNothingInTheNavbarWhenThereIsNoSession() {
    assertThat(advice.userInitial(null), is(nullValue()));
    assertThat(advice.userDisplayName(null), is(nullValue()));
    assertThat(
      advice.userInitial(
        new AnonymousAuthenticationToken("key", "anonymousUser", roles("ROLE_ANONYMOUS"))
      ),
      is(nullValue())
    );
  }

  // --- userAvatarUrl: el navbar refleja el avatar vigente del usuario ---

  @Test
  public void shouldExposeTheAvatarUrlOfTheSignedInUser() {
    User user = Mockito.mock(User.class);
    when(user.getAvatarUrl()).thenReturn("/images/avatars/avatar-2.svg");
    when(userService.getByEmail(EMAIL)).thenReturn(user);

    assertThat(advice.userAvatarUrl(appUser("Ana", "Perez")), is("/images/avatars/avatar-2.svg"));
  }

  @Test
  public void shouldFallBackToNullWithoutAvatarUrl() {
    User user = Mockito.mock(User.class);
    when(user.getAvatarUrl()).thenReturn("   ");
    when(userService.getByEmail(EMAIL)).thenReturn(user);

    assertThat(advice.userAvatarUrl(appUser("Ana", "Perez")), is(nullValue()));
  }

  @Test
  public void shouldFallBackToNullWhenThePrincipalDoesNotResideInTheUserTable() {
    when(userService.getByEmail(EMAIL)).thenThrow(new UserNotFoundException());

    assertThat(advice.userAvatarUrl(appUser("Ana", "Perez")), is(nullValue()));
  }

  @Test
  public void shouldNotConsultTheUserTableWithoutASession() {
    assertThat(advice.userAvatarUrl(null), is(nullValue()));
    assertThat(
      advice.userAvatarUrl(
        new AnonymousAuthenticationToken("key", "anonymousUser", roles("ROLE_ANONYMOUS"))
      ),
      is(nullValue())
    );
    Mockito.verifyNoInteractions(userService);
  }

  // --- userThemePreference: el modo elegido viaja server-side al head ---

  @Test
  public void shouldExposeTheThemePreferenceOfTheSignedInUser() {
    User user = Mockito.mock(User.class);
    when(user.getThemePreference()).thenReturn("dark");
    when(userService.getByEmail(EMAIL)).thenReturn(user);

    assertThat(advice.userThemePreference(appUser("Ana", "Perez")), is("dark"));
  }

  @Test
  public void shouldFallBackToNullWhenTheUserHasNoStoredPreference() {
    User user = Mockito.mock(User.class);
    when(user.getThemePreference()).thenReturn(null);
    when(userService.getByEmail(EMAIL)).thenReturn(user);

    assertThat(advice.userThemePreference(appUser("Ana", "Perez")), is(nullValue()));
  }

  @Test
  public void shouldFallBackToNullForTheThemeWhenThePrincipalIsSynthetic() {
    when(userService.getByEmail(EMAIL)).thenThrow(new UserNotFoundException());

    assertThat(advice.userThemePreference(appUser("Ana", "Perez")), is(nullValue()));
  }

  @Test
  public void shouldNotConsultTheUserTableForTheThemeWithoutASession() {
    assertThat(advice.userThemePreference(null), is(nullValue()));
    assertThat(
      advice.userThemePreference(
        new AnonymousAuthenticationToken("key", "anonymousUser", roles("ROLE_ANONYMOUS"))
      ),
      is(nullValue())
    );
    Mockito.verifyNoInteractions(userService);
  }

  // --- showAdminNav ---

  @Test
  public void shouldOfferTheAdminNavbarOnlyToAnAdminInsideTheAdminArea() {
    Authentication admin = authenticatedAs("ROLE_ADMIN");

    assertThat(advice.showAdminNav(admin, requestFor("/admin/home")), is(true));
    assertThat(advice.showAdminNav(admin, requestFor("/admin")), is(true));
    assertThat(advice.showAdminNav(admin, requestFor("/explore")), is(false));
    assertThat(
      advice.showAdminNav(authenticatedAs("ROLE_USER"), requestFor("/admin/home")),
      is(false)
    );
    assertThat(advice.showAdminNav(null, requestFor("/admin/home")), is(false));
  }

  // --- themeRedirectUrl: el POST del toggle de tema vuelve a la pagina actual ---

  @Test
  public void shouldReturnTheCurrentPageForTheThemeRedirect() {
    assertThat(advice.themeRedirectUrl(requestFor("/profile/abc")), is("/profile/abc"));
  }

  @Test
  public void shouldKeepTheQueryStringForTheThemeRedirect() {
    assertThat(
      advice.themeRedirectUrl(requestFor("/for-you", "q=cafe&category=CAFE")),
      is("/for-you?q=cafe&category=CAFE")
    );
  }

  @Test
  public void shouldKeepTheAdminAreaForTheThemeRedirect() {
    assertThat(
      advice.themeRedirectUrl(requestFor("/admin/places/import")),
      is("/admin/places/import")
    );
    assertThat(
      advice.themeRedirectUrl(requestFor("/admin/places/import", "page=2")),
      is("/admin/places/import?page=2")
    );
  }

  @Test
  public void shouldDropTheContextPathAndAnEmptyQueryFromTheThemeRedirect() {
    MockHttpServletRequest request = requestFor("/valhalla/explore");
    request.setContextPath("/valhalla");
    request.setQueryString("   ");

    assertThat(advice.themeRedirectUrl(request), is("/explore"));
  }

  // --- [NOTIF] unreadNotificationCount: el badge de la campana ---

  @Test
  public void NOTIF_shouldExposeTheUnreadCountOfTheSignedInUser() {
    java.util.UUID id = java.util.UUID.randomUUID();
    User user = Mockito.mock(User.class);
    when(user.getId()).thenReturn(id);
    when(userService.getByEmail(EMAIL)).thenReturn(user);
    when(notificationService.unreadCount(id)).thenReturn(3L);

    assertThat(advice.unreadNotificationCount(appUser("Ana", "Perez")), is(3L));
  }

  @Test
  public void NOTIF_shouldBadgeZeroWhenThePrincipalIsSynthetic() {
    when(userService.getByEmail(EMAIL)).thenThrow(new UserNotFoundException());

    assertThat(advice.unreadNotificationCount(appUser("Ana", "Perez")), is(0L));
  }

  @Test
  public void NOTIF_shouldNotConsultNotificationsWithoutASession() {
    assertThat(advice.unreadNotificationCount(null), is(0L));
    assertThat(
      advice.unreadNotificationCount(
        new AnonymousAuthenticationToken("key", "anonymousUser", roles("ROLE_ANONYMOUS"))
      ),
      is(0L)
    );
    Mockito.verifyNoInteractions(notificationService);
  }

  // --- helpers ---

  /** The shape a real login has: a token whose principal is the {@link AppUserDetails}. */
  private static Authentication appUser(String firstName, String lastName) {
    AppUserDetails details = new AppUserDetails(
      EMAIL,
      "password",
      true,
      firstName,
      lastName,
      roles("ROLE_USER")
    );
    return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
  }

  private static Authentication authenticatedAs(String role) {
    return new UsernamePasswordAuthenticationToken(EMAIL, null, roles(role));
  }

  private static List<SimpleGrantedAuthority> roles(String role) {
    return List.of(new SimpleGrantedAuthority(role));
  }

  private static MockHttpServletRequest requestFor(String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(uri);
    request.setContextPath("");
    return request;
  }

  private static MockHttpServletRequest requestFor(String uri, String query) {
    MockHttpServletRequest request = requestFor(uri);
    request.setQueryString(query);
    return request;
  }
}
