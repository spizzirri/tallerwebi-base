package com.valhalla.domain.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * [PROF] Cambios de perfil y de contrasena sobre el agregado User.
 * Escenarios docs/specs/11-PROF.md: U-01..U-06.
 */
public class UserProfileTest {

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User();
    user.setEmail("owner@example.com");
    user.setRole("USER");
    user.setPassword("legacy-encoded-hash");
  }

  // --- U-01 ---

  @Test
  public void updateProfileCopiesFirstNameAndLastName() {
    user.updateProfile("Ada", "Lovelace", null);
    assertThat(user.getFirstName(), is("Ada"));
    assertThat(user.getLastName(), is("Lovelace"));
  }

  // --- U-02 ---

  @Test
  public void updateProfileDoesNotTouchEmailOrRole() {
    user.updateProfile("Ada", "Lovelace", null);
    assertThat(user.getEmail(), is("owner@example.com"));
    assertThat(user.getRole(), is("USER"));
  }

  // --- U-03, U-04 ---

  @Test
  public void changePasswordReencodesWithTheEncoderAndNeverStoresPlaintext() {
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    String newRaw = "nueva-contrasena";
    String previousHash = user.getPassword();

    user.changePassword(newRaw, encoder);

    assertThat(user.getPassword(), is(not(newRaw)));
    assertThat(user.getPassword(), is(not(previousHash)));
    assertThat(encoder.matches(newRaw, user.getPassword()), is(true));
    assertThat(encoder.matches(previousHash, user.getPassword()), is(false));
    assertThat(user.getPassword(), is(not(nullValue())));
  }

  // --- U-05 ---

  @Test
  public void updateProfileKeepsAnAvatarUrlWithAValue() {
    user.updateProfile("Ada", "Lovelace", "/images/avatars/avatar-2.svg");
    assertThat(user.getAvatarUrl(), is("/images/avatars/avatar-2.svg"));
  }

  // --- U-06 ---

  @Test
  public void updateProfileNormalizesABlankAvatarUrlToNull() {
    user.updateProfile("Ada", "Lovelace", "   ");
    assertThat(user.getAvatarUrl(), is(nullValue()));
  }
}
