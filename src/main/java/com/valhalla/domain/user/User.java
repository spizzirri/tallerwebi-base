package com.valhalla.domain.user;

import com.valhalla.domain.plan.UuidVersion7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Locale;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
public class User {

  /** [PROF+THEME] Los dos valores que entiende el CSS en {@code data-theme}. */
  public static final String THEME_LIGHT = "light";

  public static final String THEME_DARK = "dark";

  @Id
  @UuidGenerator(algorithm = UuidVersion7Generator.class)
  private UUID id;

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
  /**
   * [PROF+THEME] Preferencia de tema del usuario: {@code "light"}, {@code "dark"} o null (sin
   * preferencia: el front cae a localStorage/sistema). La columna guarda exactamente el valor que
   * el script temprano escribe en {@code data-theme}, por eso no se usa un enum con mayusculas.
   */
  private String themePreference;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
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

  public String getThemePreference() {
    return themePreference;
  }

  /**
   * [PROF+THEME] Null-safe: blank/null limpian la preferencia (el front cae a localStorage/sistema)
   * y los valores validos se guardan normalizados en minusculas, que es lo que lee {@code
   * data-theme}. Un valor fuera de {@link #THEME_LIGHT}/{@link #THEME_DARK} es un error de
   * programacion: el controller lo filtra antes con {@link #isValidTheme(String)}.
   */
  public void setThemePreference(String themePreference) {
    String normalized = normalizeTheme(themePreference);
    if (normalized != null && !isValidTheme(normalized)) {
      throw new IllegalArgumentException("Tema invalido: " + themePreference);
    }
    this.themePreference = normalized;
  }

  /** [PROF+THEME] Los unicos valores que el modal puede postear (case-insensitive). */
  public static boolean isValidTheme(String themePreference) {
    return (
      THEME_LIGHT.equalsIgnoreCase(themePreference) || THEME_DARK.equalsIgnoreCase(themePreference)
    );
  }

  /**
   * [PROF] Actualiza solo los datos del perfil. Email y role NO se tocan (U-02): son identidad,
   * y el panel admin es quien los edita. El update de {@code UserService} tampoco los recibe, el
   * compilador lo obliga. Un avatar en blanco queda en null para que la vista caiga al fallback
   * de iniciales.
   */
  public void updateProfile(String newFirstName, String newLastName, String newAvatarUrl) {
    this.firstName = newFirstName;
    this.lastName = newLastName;
    this.avatarUrl = normalize(newAvatarUrl);
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

  private static String normalizeTheme(String themePreference) {
    return themePreference == null || themePreference.isBlank()
      ? null
      : themePreference.trim().toLowerCase(Locale.ROOT);
  }
}
