package com.tallerwebi.presentacion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

/** Manda al login a quien intenta entrar a una página privada sin haber iniciado sesión. */
public class InterceptorSesion implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws IOException {
    HttpSession sesion = request.getSession(false);
    if (sesion != null && sesion.getAttribute(ControladorLogin.USUARIO_ID) != null) {
      return true;
    }
    response.sendRedirect(request.getContextPath() + "/login");
    return false;
  }
}
