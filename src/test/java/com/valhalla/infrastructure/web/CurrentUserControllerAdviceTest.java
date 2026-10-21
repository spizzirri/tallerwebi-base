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

  @BeforeEach
  public void init() {
    userService = Mockito.mock(UserService.class);
    advice = new CurrentUserControllerAdvice(userService);
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
}
