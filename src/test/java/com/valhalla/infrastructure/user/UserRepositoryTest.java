package com.valhalla.infrastructure.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.integration.JpaIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@JpaIntegrationTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @Transactional
  @Rollback
  public void shouldSaveANewUser() {
    String newUserEmail = "new.user@test.com";
    // given
    User user = this.givenAUser(newUserEmail, "1234", "USER");

    // when
    this.whenISaveUser(user);

    // then
    this.thenTheUserWasSaved(newUserEmail, user);
  }

  @Test
  @Transactional
  @Rollback
  public void shouldFindUserWhenEmailExists() {
    String email = "test@test.com";
    User user = this.givenAUser(email, "123", "USER");
    this.givenUserExists(user);

    User result = this.whenIGetUserByEmail(email);

    this.thenTheRetrievedUserMatchesExpected(result, user);
  }

  @Test
  @Transactional
  public void shouldNotFindUserWhenEmailDoesNotExist() {
    User result = this.whenIGetUserByEmail("test@test.com");
    this.thenTheRetrievedUserIsNull(result);
  }

  @Test
  @Transactional
  @Rollback
  public void shouldUpdateAnExistingUser() {
    String email = "test@test.com";
    User user = this.givenAUser(email, "123", "USER");
    this.givenUserExists(user);

    user.setPassword("4567");
    user.setActive(true);
    user.setRole("ADMIN");

    this.whenIUpdateUser(user);

    User result = this.whenIGetUserByEmail(email);
    this.thenTheRetrievedUserMatchesExpected(result, user);
  }

  @Test
  @Transactional
  @Rollback
  public void shouldThrowWhenUpdatingANonPersistedUser() {
    User user = this.givenAUser("missing@test.com", "123", "USER");

    // Without an ID (not persisted), update must throw UserNotFoundException.
    this.thenUserNotFoundExceptionIsThrown(user);
  }

  @Test
  @Transactional
  @Rollback
  public void shouldThrowWhenUpdatingUserWithUnknownId() {
    User user = this.givenAUser("missing@test.com", "123", "USER");
    user.setId(UUID.fromString("0192f0a1-0000-7000-8000-0000000003e7"));

    // With an ID but no associated record, update must throw UserNotFoundException.
    this.thenUserNotFoundExceptionIsThrown(user);
  }

  private User givenAUser(String email, String password, String role) {
    User user = new User();
    user.setEmail(email);
    user.setPassword(password);
    user.setRole(role);
    return user;
  }

  private void givenUserExists(User user) {
    this.userRepository.save(user);
  }

  private void whenISaveUser(User user) {
    this.userRepository.save(user);
  }

  private User whenIGetUserByEmail(String email) {
    return this.userRepository.findByEmail(email).orElse(null);
  }

  private void whenIUpdateUser(User user) {
    this.userRepository.update(user);
  }

  private void thenTheUserWasSaved(String email, User expectedUser) {
    User retrievedUser = this.userRepository.findByEmail(email).orElse(null);
    this.thenTheRetrievedUserMatchesExpected(expectedUser, retrievedUser);
  }

  private void thenTheRetrievedUserMatchesExpected(User retrievedUser, User expectedUser) {
    assertThat(retrievedUser, is(not(nullValue())));
    assertThat(retrievedUser.getEmail(), is(equalTo(expectedUser.getEmail())));
    assertThat(retrievedUser.getPassword(), is(equalTo(expectedUser.getPassword())));
    assertThat(retrievedUser.getActive(), is(equalTo(expectedUser.getActive())));
    assertThat(retrievedUser.getRole(), is(equalTo(expectedUser.getRole())));
  }

  private void thenTheRetrievedUserIsNull(User retrievedUser) {
    assertThat(retrievedUser, is(nullValue()));
  }

  private void thenUserNotFoundExceptionIsThrown(User user) {
    assertThrows(
      UserNotFoundException.class,
      () -> {
        this.whenIUpdateUser(user);
      }
    );
  }
}
