package com.valhalla.infrastructure.security;

import com.valhalla.presentation.shared.LoginRedirects;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Vuelve a la página de login con el error, conservando el {@code redirect} de origen: si alguien
 * estaba parado en el plan público (o en cualquier otra página) e ingresa mal la contraseña, el
 * reintento tiene que volver a ese mismo plan, no perderse en la landing.
 *
 * <p>La URL de fallo se arma a mano en vez de usar el {@code failureUrl} fijo de la config, porque
 * ese destino estático se llevaría el parámetro. El valor se valida con
 * {@link LoginRedirects#isSafeInternalPath(String)} antes de reenviarlo (anti open-redirect).
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

  /** Mismo parámetro que reenvía el handler de éxito (ver CustomAuthenticationSuccessHandler). */
  private static final String REDIRECT_PARAM = "redirect";
  private static final String ERROR_URL = "/auth/login?error=true";

  @Override
  public void onAuthenticationFailure(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException exception
  ) throws IOException, ServletException {
    String target = ERROR_URL;
    String redirect = request.getParameter(REDIRECT_PARAM);
    if (LoginRedirects.isSafeInternalPath(redirect)) {
      target += "&" + REDIRECT_PARAM + "=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
    }
    response.sendRedirect(request.getContextPath() + target);
  }
}
