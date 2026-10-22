package com.valhalla.infrastructure.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecoverPasswordServiceTest {

  private static final String EMAIL = "recuperable@unlam.edu.ar";
  private static final UUID USER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");

  private UserRepository userRepositoryMock;
  private UserService userServiceMock;
  private RecoverPasswordService recoverPasswordService;

  @BeforeEach
  public void init() {
    this.userRepositoryMock = mock(UserRepository.class);
    this.userServiceMock = mock(UserService.class);
    this.recoverPasswordService =
      new RecoverPasswordService(this.userRepositoryMock, this.userServiceMock);
  }

  @Test
  public void shouldReturnTheRotatedPasswordWhenTheEmailIsRegistered() {
    // given
    User user = new User();
    user.setId(USER_ID);
    user.setEmail(EMAIL);
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    when(this.userServiceMock.rotatePassword(USER_ID)).thenReturn("tmp-9876");

    // when
    String tempPassword = this.recoverPasswordService.recoverPassword(EMAIL);

    // then
    assertThat(tempPassword, is("tmp-9876"));
    verify(this.userServiceMock).rotatePassword(USER_ID);
  }

  @Test
  public void shouldRejectAnUnknownEmailWithoutRotatingAnything() {
    // given
    when(this.userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.empty());

    // when and then (AC-08: nothing is created or modified)
    IllegalArgumentException thrown = assertThrows(
      IllegalArgumentException.class,
      () -> this.recoverPasswordService.recoverPassword(EMAIL)
    );
    assertThat(thrown.getMessage(), is("Email no encontrado"));
    verify(this.userServiceMock, never()).rotatePassword(any());
  }
}
