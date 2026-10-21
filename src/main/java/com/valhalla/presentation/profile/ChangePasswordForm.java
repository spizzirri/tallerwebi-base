package com.valhalla.presentation.profile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * [PROF] Input del cambio self-service de contrasena. {@code @Size(min = 8)} es la regla del
 * perfil (AC-08): mas estricta que el minimo del registro (6) a proposito — la cuenta ya tiene
 * valor, el cambio debe subir el estandar. La coincidencia de la confirmacion es validacion
 * cross-field: anotada en el getter y remarcada para {@code th:errors}.
 */
public class ChangePasswordForm {

  @NotBlank(message = "Ingresa tu contrasena actual")
  private String currentPassword;

  @NotBlank(message = "Ingresa la contrasena nueva")
  @Size(min = 8, message = "La contrasena nueva debe tener al menos 8 caracteres")
  private String newPassword;

  @NotBlank(message = "Repite la contrasena nueva")
  private String confirmPassword;

  public ChangePasswordForm() {}

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  @AssertTrue(message = "La contrasena nueva y su confirmacion no coinciden")
  public boolean isPasswordsMatch() {
    if (newPassword == null || confirmPassword == null) {
      return false;
    }
    return newPassword.equals(confirmPassword);
  }
}
