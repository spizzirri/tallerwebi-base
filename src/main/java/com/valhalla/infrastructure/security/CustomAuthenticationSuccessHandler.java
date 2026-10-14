package com.valhalla.infrastructure.security;

import com.valhalla.presentation.shared.LoginRedirects;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  /** Parametro con la URL de origen que arma el navbar (ver CurrentUserControllerAdvice.currentPath). */
  private static final String REDIRECT_PARAM = "redirect";

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  ) throws IOException, ServletException {
    request.getSession().setAttribute("loginTime", System.currentTimeMillis());

    // Si el login vino de una pagina concreta (el plan publico, /explore, etc.), se vuelve a esa
    // ruta. Sin el parametro, o con redirect=/ (lo que arma el navbar en la landing), se cae en la
    // landing comun: un USER aterriza en el feed "Para vos" (/for-you), el home que define
    // LoginRedirects.USER_LANDING. La regla vive en LoginRedirects, compartida con las dos paginas
    // de login, asi que las tres coinciden en el mismo destino.
    String target = LoginRedirects.afterLogin(request.getParameter(REDIRECT_PARAM), authentication);
    response.sendRedirect(request.getContextPath() + target);
  }
}
