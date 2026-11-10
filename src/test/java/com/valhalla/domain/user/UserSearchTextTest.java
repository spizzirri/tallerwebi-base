package com.valhalla.domain.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/** [USEARCH] T-USEARCH-001: normalizacion y escape del texto de busqueda de personas. */
public class UserSearchTextTest {

  @Test
  void shouldNormalizeCaseAndAccents() {
    assertThat(UserSearchText.normalize("  JOSÉ  "), equalTo("jose"));
    assertThat(UserSearchText.normalize("Álvarez"), equalTo("alvarez"));
    assertThat(UserSearchText.normalize("Muñoz"), equalTo("munoz"));
  }

  @Test
  void shouldTreatNullAndBlankAsAnEmptyQuery() {
    assertThat(UserSearchText.normalize(null), equalTo(""));
    assertThat(UserSearchText.normalize("   "), equalTo(""));
  }

  @Test
  void shouldEscapeLikeWildcardsSoTheySearchLiteralText() {
    assertThat(UserSearchText.escapeLike("100%"), equalTo("100!%"));
    assertThat(UserSearchText.escapeLike("a_b"), equalTo("a!_b"));
    assertThat(UserSearchText.escapeLike("wow!"), equalTo("wow!!"));
  }
}
