package com.valhalla.presentation.profile;

import jakarta.validation.constraints.NotBlank;

/**
 * [PROF] Edicion del perfil propio. El id del usuario viaja por la sesion, NUNCA por este form
 * (AC-04): el controller toma el usuario del principal y este DTO solo lleva lo que edita la
 * gente - nombres y avatar. avatarUrl es opcional y su formato lo valida el controller (AC-25):
 * https://... o /images/avatars/...
 */
public class ProfileForm {

  @NotBlank(message = "El nombre no puede quedar vacio")
  private String firstName;

  @NotBlank(message = "El apellido no puede quedar vacio")
  private String lastName;

  private String avatarUrl;

  public ProfileForm() {}

  public ProfileForm(String firstName, String lastName, String avatarUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatarUrl = avatarUrl;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
