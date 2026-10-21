package com.valhalla.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;

  private String password;
  private String firstName;
  private String lastName;
  /**
   * [PROF] Avatar opcional: {{@code https://...}} o un path de la app (/images/avatars/...). Sin
   * valor la vista cae al avatar de iniciales. La validacion del valor vive en presentation (es un
   * input de form); el agregado solo po normaliza el blanco a null.
   */
  private String avatarUrl;
  private String role;
  private Boolean active = false;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public void activate() {
    active = true;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  /**
   * [PROF] Actualiza solo los datos del perfil. Email y role NO se tocan (U-02): son identidad,
   * y el panel admin es quien los edita. El update de {@code UserService} tampoco los recibe, el
   * compilador lo obliga. Un avatar en blanco queda en null para que la vista caiga al fallback
   * de iniciales.
   */
  public void updateProfile(String firstName, String lastName, String avatarUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.avatarUrl = normalize(avatarUrl);
  }

  /**
   * [PROF] Cambio self-service de contrasena. El encoder llega por parametro a proposito: es el
   * mismo bean de {@code LoginService}/{@code UserServiceImpl}, no una re-implementacion. El
   * nuevo valor se persiste codificado, nunca en claro (U-04).
   */
  public void changePassword(String newRawPassword, PasswordEncoder encoder) {
    this.password = encoder.encode(newRawPassword);
  }

  private static String normalize(String avatarUrl) {
    return avatarUrl == null || avatarUrl.isBlank() ? null : avatarUrl.trim();
  }
}
