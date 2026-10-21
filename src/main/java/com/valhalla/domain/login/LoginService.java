package com.valhalla.domain.login;

import com.valhalla.domain.user.User;

public interface LoginService {
  User findUser(String email, String password);

  /** Self-service registration (AC-01): email + password only, no personal data collected. */
  void register(String email, String password);

  void register(String email, String password, String firstName, String lastName);

  /**
   * [PROF] Self-service con avatar opcional (AC-23). El de 4 argumentos queda para los accesos
   * de invitados/admin y delega con avatar nulo. La validacion del formato la hace la capa
   * web (AC-25): al dominio le llega el valor ya saneado.
   */
  void register(String email, String password, String firstName, String lastName, String avatarUrl);
}
