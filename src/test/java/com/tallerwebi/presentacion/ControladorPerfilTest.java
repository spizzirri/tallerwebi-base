package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.NoCargaPerfilExeption;
import com.tallerwebi.dominio.excepcion.NoPudoGuardarPerfilException;
import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
public class ControladorPerfilTest {

    @Mock
    private ServicioPerfil servicioPerfil;

    @Mock
    private ServicioLogin servicioLogin;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ControladorPerfil controladorPerfil;

    @BeforeEach
    public void init() {
        this.servicioPerfil = mock(ServicioPerfil.class);
        MockitoAnnotations.initMocks(this);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controladorPerfil).build();
    }

    @Test
    public void irPerfilConUsuarioNoAuntenticadoRetorneAlLogin() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(null);

        // ejecución
        ModelAndView modelAndView = controladorPerfil.irPerfil(session);

        // verificación
        assertEquals("redirect:/login", modelAndView.getViewName());
    }

    @Test
    public void dadoQueCadaUsuarioPoseeUnPerfilADeterminarQuePermitaIrPerfilConUsuarioSinPerfilDeterminado() {
        // preparación
        Usuario usuario = new Usuario();
        usuario.setPerfil(null);
        when(session.getAttribute("usuario")).thenReturn(usuario);

        // ejecución
        ModelAndView modelAndView = controladorPerfil.irPerfil(session);

        // verificación
        assertEquals("perfil", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("usuario"));
        assertTrue(modelAndView.getModel().containsKey("perfil"));
        assertNotNull(modelAndView.getModel().get("perfil"));
    }

    @Test
    public void QuePermitaIrPerfilConUsuarioConPerfilDeterminado() throws Exception {
        // preparación
        Usuario usuario = new Usuario();
        Perfil perfil = new Perfil();
        usuario.setPerfil(perfil);
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(servicioPerfil.generarRecomendacion(any(Perfil.class))).thenReturn("Recomendación de prueba");

        // ejecución
        ModelAndView modelAndView = controladorPerfil.irPerfil(session);

        // verificación
        assertEquals("perfil", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("usuario"));
        assertTrue(modelAndView.getModel().containsKey("perfil"));
        assertEquals("Recomendación de prueba", perfil.getRecomendacion());
    }

    @Test
    public void irPerfilYNoRecibaRecomendacionQueInserteErrorGenerandoRecomendacion() throws NoCargaPerfilExeption {
        // preparación
        Usuario usuario = new Usuario();
        Perfil perfil = new Perfil();
        usuario.setPerfil(perfil);
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(servicioPerfil.generarRecomendacion(any(Perfil.class))).thenThrow(new NoCargaPerfilExeption ("Error generando recomendación"));

        // ejecución
        ModelAndView modelAndView = controladorPerfil.irPerfil(session);

        // verificación
        assertEquals("error", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("mensaje"));
        assertTrue(((String) modelAndView.getModel().get("mensaje")).contains("Error al cargar el perfil: Error generando recomendación"));
    }

    @Test
    public void alGuardarPerfilConUsuarioNoAutenticadoDebeRetornarAlLogin() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(null);

        // ejecución
        ModelAndView modelAndView = controladorPerfil.guardarPerfil(new Perfil(), session);

        // verificación
        assertEquals("redirect:/login", modelAndView.getViewName());
    }

    @Test
    public void queElUsuarioLogreGuardarSuPerfilConExito() throws NoPudoGuardarPerfilException {
        // preparación
        Usuario usuario = new Usuario();
        Perfil perfil = new Perfil();
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(servicioPerfil.generarRecomendacion(any(Perfil.class))).thenReturn("Recomendación de prueba");

        // ejecución
        ModelAndView modelAndView = controladorPerfil.guardarPerfil(perfil, session);

        // verificación
        assertEquals("redirect:/perfil", modelAndView.getViewName());
        verify(servicioLogin, times(1)).guardarPerfil(usuario, perfil);
        assertEquals("Recomendación de prueba", perfil.getRecomendacion());
    }

    @Test
    public void alGuardarPerfilNoRecibaRecomendacionQueInserteErrorGenerandoRecomendacion() throws NoPudoGuardarPerfilException {
        // preparación
        Usuario usuario = new Usuario();
        Perfil perfil = new Perfil();
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(servicioPerfil.generarRecomendacion(any(Perfil.class))).thenThrow(new NoPudoGuardarPerfilException("Error generando recomendación"));

        // ejecución
        ModelAndView modelAndView = controladorPerfil.guardarPerfil(perfil, session);

        // verificación
        assertEquals("error", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("mensaje"));
        assertTrue(((String) modelAndView.getModel().get("mensaje")).contains("Error al guardar el perfil: Error generando recomendación"));
    }

    @Test
    public void queAlPresionarElBotonCerrarSesionRedireccioneAlLogin() {
        // Preparación
        when(session.getAttribute("usuario")).thenReturn(new Usuario());

        // Ejecución
        String result = controladorPerfil.cerrarSesion(session);

        // Verificación
        assertEquals("redirect:/login", result);
    }


}
