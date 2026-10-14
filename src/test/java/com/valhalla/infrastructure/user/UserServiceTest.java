package com.valhalla.infrastructure.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.plan.Plan;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  private static final UUID PLAN_ID_A = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID PLAN_ID_B = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");

  private UserService userService;
  private UserRepository userRepositoryMock;
  private PlanRepository planRepositoryMock;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  public void init() {
    this.userRepositoryMock = mock(UserRepository.class);
    this.planRepositoryMock = mock(PlanRepository.class);
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.userService =
      new UserServiceImpl(this.userRepositoryMock, this.planRepositoryMock, this.passwordEncoder);
  }

  @Test
  public void shouldReturnAllUsersWhenFindAll() {
    // given
    User user1 = new User();
    user1.setId(1L);
    user1.setEmail("a@test.com");
    User user2 = new User();
    user2.setId(2L);
    user2.setEmail("b@test.com");
    when(this.userRepositoryMock.findAll()).thenReturn(List.of(user1, user2));

    // when
    List<User> users = this.userService.findAll();

    // then
    assertThat(users.size(), is(equalTo(2)));
    verify(this.userRepositoryMock, times(1)).findAll();
  }

  @Test
  public void shouldReturnUserWhenFindByIdExists() {
    // given
    Long id = 1L;
    User expectedUser = new User();
    expectedUser.setId(id);
    expectedUser.setEmail("test@test.com");
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.of(expectedUser));

    // when
    User actualUser = this.userService.findById(id);

    // then
    assertThat(actualUser, is(equalTo(expectedUser)));
  }

  @Test
  public void shouldThrowWhenFindByIdDoesNotExist() {
    // given
    Long id = 999L;
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.empty());

    // when and then
    assertThrows(UserNotFoundException.class, () -> this.userService.findById(id));
  }

  @Test
  public void shouldSaveNewUserWhenCreateWithValidData() {
    // given
    String email = "new@test.com";
    String password = "password123";
    String role = "USER";
    when(this.userRepositoryMock.existsByEmail(email)).thenReturn(false);

    // when
    this.userService.create(email, password, role, "John", "Doe");

    // then
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userRepositoryMock, times(1)).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getEmail(), is(equalTo(email)));
    assertThat(saved.getFirstName(), is(equalTo("John")));
    assertThat(saved.getLastName(), is(equalTo("Doe")));
    assertThat(saved.getRole(), is(equalTo(role)));
    assertThat(saved.getActive(), is(true));
    assertThat(saved.getPassword(), not(equalTo(password)));
    assertThat(this.passwordEncoder.matches(password, saved.getPassword()), is(true));
  }

  @Test
  public void shouldThrowWhenCreateWithExistingEmail() {
    // given
    String email = "exists@test.com";
    when(this.userRepositoryMock.existsByEmail(email)).thenReturn(true);

    // when and then
    assertThrows(
      UserAlreadyExists.class,
      () -> this.userService.create(email, "password123", "USER", "John", "Doe")
    );
    verify(this.userRepositoryMock, never()).save(any(User.class));
  }

  @Test
  public void shouldUpdateEmailAndRoleWhenUpdateWithValidData() {
    // given
    Long id = 1L;
    User existingUser = new User();
    existingUser.setId(id);
    existingUser.setEmail("old@test.com");
    existingUser.setRole("USER");
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.of(existingUser));

    // when
    this.userService.update(id, "new@test.com", "ADMIN", "Jane", "Smith");

    // then
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userRepositoryMock, times(1)).update(captor.capture());
    User updated = captor.getValue();
    assertThat(updated.getEmail(), is(equalTo("new@test.com")));
    assertThat(updated.getFirstName(), is(equalTo("Jane")));
    assertThat(updated.getLastName(), is(equalTo("Smith")));
    assertThat(updated.getRole(), is(equalTo("ADMIN")));
  }

  @Test
  public void shouldThrowWhenUpdateNonExistentUser() {
    // given
    Long id = 999L;
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.empty());

    // when and then
    assertThrows(
      UserNotFoundException.class,
      () -> this.userService.update(id, "new@test.com", "ADMIN", "Jane", "Smith")
    );
  }

  @Test
  public void shouldSetActiveFalseWhenDeactivate() {
    // given
    Long id = 1L;
    User user = new User();
    user.setId(id);
    user.setEmail("test@test.com");
    user.setActive(true);
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.of(user));

    // when
    this.userService.deactivate(id);

    // then
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userRepositoryMock, times(1)).update(captor.capture());
    User deactivated = captor.getValue();
    assertThat(deactivated.getActive(), is(false));
  }

  @Test
  public void shouldThrowWhenDeactivateNonExistentUser() {
    // given
    Long id = 999L;
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.empty());

    // when and then
    assertThrows(UserNotFoundException.class, () -> this.userService.deactivate(id));
  }

  @Test
  public void shouldDeleteUserWhenDeleteWithExistingId() {
    // given
    Long id = 1L;
    when(this.userRepositoryMock.existsById(id)).thenReturn(true);
    when(this.planRepositoryMock.findByAdministratorId(id)).thenReturn(List.of());

    // when
    this.userService.delete(id);

    // then
    verify(this.userRepositoryMock, times(1)).deleteById(id);
  }

  /**
   * La regresión del 500: plans.administrator_id es una FK a users.id, así que borrar al usuario con
   * sus planes encima viola la restricción. El orden es lo que arregla el bug, por eso se asserta
   * con InOrder y no solo que se llamen los dos.
   */
  @Test
  public void shouldDeleteThePlansOfTheUserBeforeDeletingTheUser() {
    // given
    Long id = 1L;
    when(this.userRepositoryMock.existsById(id)).thenReturn(true);
    when(this.planRepositoryMock.findByAdministratorId(id))
      .thenReturn(List.of(planOwnedBy(id, PLAN_ID_A), planOwnedBy(id, PLAN_ID_B)));

    // when
    this.userService.delete(id);

    // then
    InOrder inOrder = inOrder(this.planRepositoryMock, this.userRepositoryMock);
    inOrder.verify(this.planRepositoryMock).deleteById(PLAN_ID_A);
    inOrder.verify(this.planRepositoryMock).deleteById(PLAN_ID_B);
    inOrder.verify(this.userRepositoryMock).deleteById(id);
  }

  /**
   * El recorte por dueño no lo hace este servicio: lo hace la consulta. Lo que se verifica es que
   * se pregunte por los planes de <em>ese</em> usuario y no por el catálogo entero, porque un
   * findAll() ahí habría borrado los planes de todos.
   */
  @Test
  public void shouldAskForThePlansOfThatUserAndNotForTheWholeCatalogue() {
    // given
    Long id = 1L;
    when(this.userRepositoryMock.existsById(id)).thenReturn(true);
    when(this.planRepositoryMock.findByAdministratorId(id))
      .thenReturn(List.of(planOwnedBy(id, PLAN_ID_A)));

    // when
    this.userService.delete(id);

    // then
    verify(this.planRepositoryMock, times(1)).findByAdministratorId(id);
    verify(this.planRepositoryMock, never()).findAll();
    verify(this.planRepositoryMock, times(1)).deleteById(PLAN_ID_A);
    verify(this.userRepositoryMock, times(1)).deleteById(id);
  }

  private Plan planOwnedBy(Long administratorId, UUID planId) {
    Plan plan = new Plan();
    plan.setId(planId);
    User administrator = new User();
    administrator.setId(administratorId);
    plan.setAdministrator(administrator);
    return plan;
  }

  @Test
  public void shouldThrowWhenDeleteNonExistentUser() {
    // given
    Long id = 999L;
    when(this.userRepositoryMock.existsById(id)).thenReturn(false);

    // when and then
    assertThrows(UserNotFoundException.class, () -> this.userService.delete(id));
    verify(this.userRepositoryMock, never()).deleteById(any(Long.class));
  }

  @Test
  public void shouldSetActiveTrueWhenActivate() {
    Long id = 1L;
    User user = new User();
    user.setId(id);
    user.setEmail("test@test.com");
    user.setActive(false);
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.of(user));

    this.userService.activate(id);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userRepositoryMock, times(1)).update(captor.capture());
    assertThat(captor.getValue().getActive(), is(true));
  }

  @Test
  public void shouldThrowWhenActivateNonExistentUser() {
    Long id = 999L;
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.userService.activate(id));
  }

  @Test
  public void shouldReturnNewPasswordWhenRotatePassword() {
    Long id = 1L;
    User user = new User();
    user.setId(id);
    user.setEmail("test@test.com");
    user.setPassword(passwordEncoder.encode("oldpassword"));
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.of(user));

    String newPassword = this.userService.rotatePassword(id);

    assertThat(newPassword, is(not(equalTo(""))));
    assertThat(newPassword.length(), is(8));
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(this.userRepositoryMock, times(1)).update(captor.capture());
    assertThat(passwordEncoder.matches(newPassword, captor.getValue().getPassword()), is(true));
  }

  @Test
  public void shouldThrowWhenRotatePasswordNonExistentUser() {
    Long id = 999L;
    when(this.userRepositoryMock.findById(id)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.userService.rotatePassword(id));
  }

  @Test
  public void shouldGenerateRandomPassword() {
    String password = this.userService.generatePassword();
    assert password.length() == 8;
  }

  @Test
  public void shouldGenerateDifferentPasswords() {
    String first = this.userService.generatePassword();
    String second = this.userService.generatePassword();
    assert !first.equals(second);
  }
}
