package com.valhalla.presentation.shared;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class LoginRedirectsTest {

  private static final String ADMIN_LANDING = "/admin/home";

  @Test
  public void shouldNotBeSignedInWithoutAnAuthentication() {
    assertThat(LoginRedirects.isSignedIn(null), is(false));
  }

  @Test
  public void shouldBeSignedInWithAnAuthentication() {
    assertThat(LoginRedirects.isSignedIn(authenticationWithRoles("ROLE_USER")), is(true));
  }

  @Test
  public void shouldLandAnAdminOnTheAdminHome() {
    assertThat(LoginRedirects.landingFor(authenticationWithRoles("ROLE_ADMIN")), is(ADMIN_LANDING));
  }

  @Test
  public void shouldLandACommonUserOnTheUserLanding() {
    // Contra la constante, no contra un literal: sobrevive el cambio de landing a /plans/public
    assertThat(
      LoginRedirects.landingFor(authenticationWithRoles("ROLE_USER")),
      is(LoginRedirects.USER_LANDING)
    );
  }

  @Test
  public void shouldNotBeAdminWithoutAnAuthentication() {
    assertThat(LoginRedirects.isAdmin(null), is(false));
  }

  @Test
  public void shouldBeAdminWithThePrefixedRole() {
    assertThat(LoginRedirects.isAdmin(authenticationWithRoles("ROLE_ADMIN")), is(true));
  }

  @Test
  public void shouldBeAdminWithTheBareRole() {
    assertThat(LoginRedirects.isAdmin(authenticationWithRoles("ADMIN")), is(true));
  }

  @Test
  public void shouldNotBeAdminWithAUserRole() {
    assertThat(LoginRedirects.isAdmin(authenticationWithRoles("ROLE_USER")), is(false));
  }

  @Test
  public void shouldRejectANullRedirect() {
    assertThat(LoginRedirects.isSafeInternalPath(null), is(false));
  }

  @Test
  public void shouldRejectABlankRedirect() {
    assertThat(LoginRedirects.isSafeInternalPath(" "), is(false));
  }

  @Test
  public void shouldRejectARedirectWithoutLeadingSlash() {
    assertThat(LoginRedirects.isSafeInternalPath("plans"), is(false));
  }

  @Test
  public void shouldRejectAProtocolRelativeRedirect() {
    assertThat(LoginRedirects.isSafeInternalPath("//otro-sitio.com"), is(false));
  }

  @Test
  public void shouldRejectARedirectWithAScheme() {
    assertThat(LoginRedirects.isSafeInternalPath("https://otro-sitio.com"), is(false));
  }

  @Test
  public void shouldRejectARedirectWithABackslash() {
    assertThat(LoginRedirects.isSafeInternalPath("/plans\\admin"), is(false));
  }

  @Test
  public void shouldRejectARedirectWithControlCharacters() {
    assertThat(LoginRedirects.isSafeInternalPath("/plans\r\njunk"), is(false));
  }

  @Test
  public void shouldAcceptAnInternalPath() {
    assertThat(LoginRedirects.isSafeInternalPath("/plans/5/public"), is(true));
  }

  private static Authentication authenticationWithRoles(String... roles) {
    List<GrantedAuthority> authorities = Arrays
      .stream(roles)
      .map(SimpleGrantedAuthority::new)
      .map(GrantedAuthority.class::cast)
      .toList();
    return new TestingAuthenticationToken("someone@example.com", "n/a", authorities);
  }
}
