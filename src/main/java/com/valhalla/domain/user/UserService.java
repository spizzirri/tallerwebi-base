package com.valhalla.domain.user;

import java.util.List;

public interface UserService {
  List<User> findAll();
  User findById(Long id);
  /** [PROF] El principal del principal de sesión lleva el email, no el id: este método lo cruza. */
  User getByEmail(String email);
  void create(String email, String password, String role, String firstName, String lastName);
  void update(Long id, String email, String role, String firstName, String lastName);
  void activate(Long id);
  void deactivate(Long id);
  void delete(Long id);
  String rotatePassword(Long id);
  String generatePassword();
  /** [PROF] Perfil self-service: nombres + avatar. Email/role quedan fuera a propósito. */
  void updateProfile(Long id, String firstName, String lastName, String avatarUrl);
  /**
   * [PROF] Cambio de contraseña self-service: exige la actual (U-09). Devuelve {@code false}
   * cuando la contraseña actual no coincide — sin escribir nada; {@code true} si el cambió se
   * guardó. Id inexistente lanza {@code UserNotFoundException}.
   */
  boolean changePassword(Long id, String currentRawPassword, String newRawPassword);
}
