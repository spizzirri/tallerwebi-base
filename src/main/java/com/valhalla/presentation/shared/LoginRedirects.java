package com.valhalla.presentation.shared;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * A dónde va alguien que ya tiene sesión.
 *
 * <p>Las dos páginas de login la usan para no mostrarle un formulario a quien ya está adentro, y el
 * handler de login exitoso la usa para el mismo destino. La regla estaba escrita una vez por página
 * en vez de una vez por lugar, así que un cambio de home tenía que acordarse de los tres.
 *
 * <p>Sin proxy de scope y sin estado: son dos preguntas sobre el principal de la request.
 */
public final class LoginRedirects {

  private static final String ADMIN_LANDING = "/admin/home";

  /**
   * Destino de un usuario sin rol admin. Es público porque los tests de aterrizaje (unitarios y
   * E2E) afirman contra esta constante en vez de contra un literal: cuando el landing cambie
   * (p. ej. a /plans/public en PR #15) las pruebas lo siguen sin editarse.
   */
  public static final String USER_LANDING = "/explore";

  private LoginRedirects() {}

  /**
   * Si la request ya trae una sesión. El filtro de Spring Security devuelve null desde
   * {@code getUserPrincipal()} cuando el principal es anónimo, así que null es exactamente "no hay
   * nadie adentro" y alcanza con esto. Si alguna vez esa respuesta fuera un
   * {@code AnonymousAuthenticationToken} en vez de null, esta es la línea que hay que endurecer.
   *
   * @param authentication el principal de la request, o null si no hay sesión
   * @return true solo con una sesión real
   */
  public static boolean isSignedIn(Authentication authentication) {
    return authentication != null;
  }

  /**
   * El mismo destino que devuelve un login exitoso, para que la página de login no difiera de lo
   * que pasa después de loguearse.
   *
   * @param authentication un principal ya autenticado
   * @return {@code /admin/home} para un ADMIN, la vista de exploración (/explore) para el resto
   */
  public static String landingFor(Authentication authentication) {
    return isAdmin(authentication) ? ADMIN_LANDING : USER_LANDING;
  }

  /**
   * Si la principal es de rol ADMIN. La regla única que comparten el destino de login y el
   * navbar: acepta {@code ROLE_ADMIN} (como arma Spring Security los roles) y {@code ADMIN} crudo.
   *
   * @param authentication un principal, o null si no hay sesión
   * @return true sólo si las autoridades dicen ADMIN
   */
  public static boolean isAdmin(Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    return authentication
      .getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .anyMatch(role -> "ROLE_ADMIN".equals(role) || "ADMIN".equals(role));
  }

  /**
   * Si una URL de retorno es una ruta interna segura. El {@code redirect} viaja en la URL, así que
   * lo controla cualquiera: solo se aceptan rutas relativas que empiezan con {@code /} y no con
   * {@code //} (protocol-relative), sin {@code ://}, backslashes ni CR/LF. Sin este corte, un POST
   * con {@code redirect=https://otro-sitio} desviaría el login a un origen externo (open redirect).
   *
   * <p>La comparten los handlers de login exitoso y fallido: los dos reenvían el mismo parámetro.
   *
   * @param value la ruta candidata, tal como llegó en el parámetro
   * @return true solo para una ruta interna de la app
   */
  public static boolean isSafeInternalPath(String value) {
    if (value == null || value.isBlank() || !value.startsWith("/") || value.startsWith("//")) {
      return false;
    }
    if (
      value.contains("://") || value.contains("\\") || value.contains("\r") || value.contains("\n")
    ) {
      return false;
    }
    return true;
  }
}
