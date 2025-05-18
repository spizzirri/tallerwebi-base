package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioJugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorHomeTest {
    private ControladorHome controladorHome;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioJugador servicioJugadorMock;

    @BeforeEach
    public void init() {
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        servicioJugadorMock = mock(ServicioJugador.class);
        controladorHome = new ControladorHome(servicioJugadorMock);
    }

    @Test
    public void devuelveModelAndView() {
        when(requestMock.getSession()).thenReturn(sessionMock);
        ModelAndView modelAndView = controladorHome.getHome(requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
    }
}
