package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InterceptorSesionTest {

  private InterceptorSesion interceptor;
  private HttpServletRequest requestMock;
  private HttpServletResponse responseMock;

  @BeforeEach
  public void init() {
    interceptor = new InterceptorSesion();
    requestMock = mock(HttpServletRequest.class);
    responseMock = mock(HttpServletResponse.class);
    when(requestMock.getContextPath()).thenReturn("/spring");
  }

  @Test
  public void sinSesionDeberiaRedirigirAlLogin() throws Exception {
    when(requestMock.getSession(false)).thenReturn(null);

    boolean continuar = interceptor.preHandle(requestMock, responseMock, new Object());

    assertThat(continuar, is(false));
    verify(responseMock).sendRedirect("/spring/login");
  }

  @Test
  public void conUsuarioLogueadoDeberiaDejarPasar() throws Exception {
    HttpSession sesion = mock(HttpSession.class);
    when(sesion.getAttribute(ControladorLogin.USUARIO_ID)).thenReturn(1L);
    when(requestMock.getSession(false)).thenReturn(sesion);

    boolean continuar = interceptor.preHandle(requestMock, responseMock, new Object());

    assertThat(continuar, is(true));
    verify(responseMock, never()).sendRedirect(anyString());
  }
}
