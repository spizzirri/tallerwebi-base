package com.valhalla.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
  List<User> findAll();
  User findById(UUID id);
  /** [PROF] El principal del principal de sesión lleva el email, no el id: este método lo cruza. */
  User getByEmail(String email);
  /** Igual que {@link #getByEmail} pero sin excepción: para cuando el usuario puede no existir. */
  Optional<User> findOptionalByEmail(String email);
  void create(String email, String password, String role, String firstName, String lastName);
  void update(UUID id, String email, String role, String firstName, String lastName);
  void activate(UUID id);
  void deactivate(UUID id);
  void delete(UUID id);
  String rotatePassword(UUID id);
  String generatePassword();
  /** [PROF] Perfil self-service: nombres + avatar. Email/role quedan fuera a propósito. */
  void updateProfile(UUID id, String firstName, String lastName, String avatarUrl);
  /**
   * [PROF] Cambio de contraseña self-service: exige la actual (U-09). Devuelve {@code false}
   * cuando la contraseña actual no coincide — sin escribir nada; {@code true} si el cambió se
   * guardó. Id inexistente lanza {@code UserNotFoundException}.
   */
  boolean changePassword(UUID id, String currentRawPassword, String newRawPassword);
  /**
   * [PROF+THEME] Guarda la preferencia de tema del usuario (U-13). El controller valida con
   * {@code User.isValidTheme} antes de llamar; un valor inválido que llegue igual lanza
   * {@code IllegalArgumentException} desde el agregado, sin escribir.
   */
  void updateThemePreference(UUID id, String themePreference);
}
