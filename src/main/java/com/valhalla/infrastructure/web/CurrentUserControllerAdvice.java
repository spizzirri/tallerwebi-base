package com.valhalla.infrastructure.web;

import com.valhalla.infrastructure.security.AppUserDetails;
import com.valhalla.presentation.shared.LoginRedirects;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Locale;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the signed-in identity to every view: the shared navbar renders an avatar with the
 * first initial of the real name ({@code userInitial}) and needs the full name for its
 * accessible label ({@code userDisplayName}).
 *
 * <p>Defensive by design: the principal is whatever the security layer placed there — an
 * {@link AppUserDetails} after a real form login, a plain Spring {@code User} (or any object
 * with a name) for synthetic MockMvc principals such as {@code SecurityMockMvcRequestPostProcessors.user(...)},
 * and null or an anonymous token when there is no session. Every one of those must render the
 * navbar without throwing or producing an empty avatar.
 *
 * <p>También expone {@code showAdminNav}, la variante del navbar: rol y área resueltos en una
 * sola cuenta, porque el template necesita el negado de esa combinación (usuario = no-admin o
 * no-área-admin) y ese OR no cae limpio repartido entre {@code sec:authorize} y {@code th:if}.
 */
@ControllerAdvice
public class CurrentUserControllerAdvice {

  @ModelAttribute("userInitial")
  public String userInitial(Principal principal) {
    String name = displayNameOf(principal);
    if (name == null) {
      return null;
    }
    return name.substring(0, 1).toUpperCase(Locale.ROOT);
  }

  @ModelAttribute("userDisplayName")
  public String userDisplayName(Principal principal) {
    return displayNameOf(principal);
  }

  /**
   * Si el navbar debe rendir su variante de admin (marca → /admin/home y link "Users"). Es rol
   * <em>y</em> área: un ADMIN navegando el área de usuario (/explore, /plans, /) ve el navbar de
   * usuario, y la variante admin queda para /admin/**. No es sólo una cuestión estética: el
   * navbar de usuario es el que ofrece las rutas que ese admin está visitando.
   *
   * <p>El logout no entra en esta cuenta y sigue siendo del rol: {@code /auth/logout} está
   * cerrado para ADMIN (cadena 1 de SecurityConfig), así que el botón de un admin apunta a
   * {@code /admin/logout} aunque los links sean los de usuario.
   */
  @ModelAttribute("showAdminNav")
  public boolean showAdminNav(Authentication authentication, HttpServletRequest request) {
    return LoginRedirects.isAdmin(authentication) && isInAdminArea(request);
  }

  /**
   * La ruta actual de la request, para que el link "Login" del navbar anónimo la pase como
   * {@code redirect} y tras loguearse la sesión vuelva a la página donde estaba (el vínculo lo
   * honora {@code CustomAuthenticationSuccessHandler}). Null en las páginas de auth/admin: volver
   * allí tras el login no tiene sentido y crearía un salto extra.
   */
  @ModelAttribute("currentPath")
  public String currentPath(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    if ("/auth".equals(path) || path.startsWith("/auth/") || isInAdminArea(request)) {
      return null;
    }
    return path;
  }

  /**
   * La ruta pedida, sin el context path, preguntando por el segmento entero: {@code /admin} o
   * {@code /admin/...}. Un prefijo suelto alcanzaría a un hipotético {@code /administrator}.
   */
  private static boolean isInAdminArea(HttpServletRequest request) {
    String path = request.getRequestURI().substring(request.getContextPath().length());
    return "/admin".equals(path) || path.startsWith("/admin/");
  }

  /**
   * Human-readable identity, or null when there is no session to speak for. Prefers the real
   * name carried by {@link AppUserDetails}; anything else falls back to the login name.
   *
   * <p>El principal de una request real es el {@link Authentication}, no la identidad de adentro:
   * {@code request.getUserPrincipal()} devuelve el token que Spring Security guardó en el contexto.
   * Por eso el token se desenvuelve antes de mirarlo: sin ese paso {@link AppUserDetails} nunca se
   * ve, la rama del nombre real queda muerta y el navbar termina mostrando el email de login.
   */
  private static String displayNameOf(Principal principal) {
    if (principal == null || principal instanceof AnonymousAuthenticationToken) {
      return null;
    }
    Object identity = principal instanceof Authentication auth ? auth.getPrincipal() : principal;
    if (identity instanceof AppUserDetails appUser) {
      String first = trimToNull(appUser.getFirstName());
      if (first != null) {
        String last = trimToNull(appUser.getLastName());
        return last == null ? first : first + " " + last;
      }
    }
    return trimToNull(principal.getName());
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
