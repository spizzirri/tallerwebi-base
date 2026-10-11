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
    // ruta. Sin el parametro se cae en la landing comun: un USER aterriza en /explore, el catalogo
    // donde se exploran lugares y se arman planes. La regla de la landing es compartida con las
    // dos paginas de login (LoginRedirects), asi que las tres coinciden en el mismo destino.
    String target = request.getParameter(REDIRECT_PARAM);
    if (!LoginRedirects.isSafeInternalPath(target)) {
      target = LoginRedirects.landingFor(authentication);
    }
    response.sendRedirect(request.getContextPath() + target);
  }
}
