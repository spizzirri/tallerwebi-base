package com.valhalla.presentation.shared;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * [PROF] Formato del avatar URL, AC-25 de docs/specs/11-PROF.md. Tabla de casos de la spec:
 * galeria y https pasan; http, javascript:, data: y basura en general rechazan; vacio es
 * opcional y pasa.
 */
public class AvatarUrlPolicyTest {

  @Test
  public void acceptsBundledGalleryPaths() {
    assertThat(AvatarUrlPolicy.isValid("/images/avatars/avatar-1.svg"), is(true));
  }

  @Test
  public void acceptsHttpsUrls() {
    assertThat(AvatarUrlPolicy.isValid("https://example.com/me.png"), is(true));
  }

  @Test
  public void acceptsEmptyValuesBecauseTheAvatarIsOptional() {
    assertThat(AvatarUrlPolicy.isValid(null), is(true));
    assertThat(AvatarUrlPolicy.isValid(""), is(true));
    assertThat(AvatarUrlPolicy.isValid("   "), is(true));
  }

  @Test
  public void acceptsSurroundingWhitespaceByTrimming() {
    assertThat(AvatarUrlPolicy.isValid("  /images/avatars/avatar-2.svg  "), is(true));
  }

  @Test
  public void rejectsHttpUrlsNotOnlyHttps() {
    assertThat(AvatarUrlPolicy.isValid("http://example.com/me.png"), is(false));
  }

  @Test
  public void rejectsScriptInjectionSchemes() {
    assertThat(AvatarUrlPolicy.isValid("javascript:alert(1)"), is(false));
    assertThat(AvatarUrlPolicy.isValid("data:image/svg+xml,<svg onload=alert(1)>"), is(false));
  }

  @Test
  public void rejectsAppPathsOutsideTheGallery() {
    assertThat(AvatarUrlPolicy.isValid("/images/plans/cover-1.svg"), is(false));
  }

  @Test
  public void rejectsBarePrefixesWithoutAFileName() {
    assertThat(AvatarUrlPolicy.isValid("https://"), is(false));
    assertThat(AvatarUrlPolicy.isValid("/images/avatars/"), is(false));
  }
}
