package com.valhalla.infrastructure.user;

import com.valhalla.domain.bookmark.PlanBookmarkRepository;
import com.valhalla.domain.exception.UserAlreadyExists;
import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.plan.PlanRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserRepository;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lives in infrastructure, not domain: the domain package must not depend on the framework. */
@Service
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PlanRepository planRepository;
  private final PlanBookmarkRepository planBookmarkRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserServiceImpl(
    UserRepository userRepository,
    PlanRepository planRepository,
    PlanBookmarkRepository planBookmarkRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.planRepository = planRepository;
    this.planBookmarkRepository = planBookmarkRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public User findById(UUID id) {
    return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public User getByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  @Override
  public void create(
    String email,
    String password,
    String role,
    String firstName,
    String lastName
  ) {
    if (userRepository.existsByEmail(email)) {
      throw new UserAlreadyExists();
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setRole(role);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setActive(true);
    userRepository.save(user);
  }

  @Override
  public void update(UUID id, String email, String role, String firstName, String lastName) {
    User user = findById(id);
    user.setEmail(email);
    user.setRole(role);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    userRepository.update(user);
  }

  @Override
  public void deactivate(UUID id) {
    User user = findById(id);
    user.setActive(false);
    userRepository.update(user);
  }

  @Override
  public void activate(UUID id) {
    User user = findById(id);
    user.setActive(true);
    userRepository.update(user);
  }

  /**
   * Borra los planes del usuario antes que el usuario. {@code plans.administrator_id} es una FK a
   * {@code users.id} sin cascada, así que borrar al usuario con planes encima viola la restricción y
   * la app responde 500. La columna se llama {@code administrator_id} pero la llenan usuarios
   * comunes: cualquiera que haya armado un plan caía en ese 500.
   *
   * <p>[MARK] Los guardados del usuario también cuelgan de {@code users.id}: se borran antes por la
   * misma razón. Ojo, los guardados <em>ajenos</em> de sus planes no se tocan: sobreviven como
   * snapshot y la card queda "Eliminado".
   *
   * <p>El orden es lo único que importa acá, y la clase entera es {@code @Transactional}, así que
   * las operaciones van o no van juntas.
   */
  @Override
  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException();
    }
    this.planRepository.findByAdministratorId(id)
      .forEach(plan -> this.planRepository.deleteById(plan.getId()));
    this.planBookmarkRepository.deleteAllByUserId(id);
    this.userRepository.deleteById(id);
  }

  @Override
  public String rotatePassword(UUID id) {
    User user = findById(id);
    String newPassword = generatePassword();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.update(user);
    return newPassword;
  }

  @Override
  public String generatePassword() {
    return java.util.UUID.randomUUID().toString().substring(0, 8);
  }

  /** [PROF] Solo toca campos de perfil (User.updateProfile ya excluye email/role). */
  @Override
  public void updateProfile(UUID id, String firstName, String lastName, String avatarUrl) {
    User user = findById(id);
    user.updateProfile(firstName, lastName, avatarUrl);
    userRepository.update(user);
  }

  /**
   * [PROF] Exigir la contraseña actual es obligatorio (11-PROF, decisión: XSS/dispositivo
   * compartido). Se compara contra el mismo encoder del login; un mismatch NO escribe nada.
   * {@code rotatePassword(id)} sigue como está: lo usa el panel admin y la recuperación.
   */
  @Override
  public boolean changePassword(UUID id, String currentRawPassword, String newRawPassword) {
    User user = findById(id);
    if (!passwordEncoder.matches(currentRawPassword, user.getPassword())) {
      return false;
    }
    user.changePassword(newRawPassword, passwordEncoder);
    userRepository.update(user);
    return true;
  }
}
