package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorCalendarioTest {

    private ServicioCalendario servicioCalendario;
    private ControladorCalendario controladorCalendario;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private HttpSession session;

    private Usuario usuario;

    @BeforeEach
    public void init() {
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.controladorCalendario = new ControladorCalendario(this.servicioCalendario);
        MockitoAnnotations.initMocks(this);
        usuario = new Usuario(); // Crear un usuario simulado
        when(session.getAttribute("usuario")).thenReturn(usuario);
    }

    @Test
    public void queAlIrALaPantallaDeCalendarioSinUsuarioRedirijaALogin() {
        // preparacion
        when(session.getAttribute("usuario")).thenReturn(null);

        // ejecucion
        ModelAndView modelAndView = controladorCalendario.mostrarCalendario(session);

        // verificacion
        assertEquals("redirect:/login", modelAndView.getViewName()); // Verificar redirección a login
    }

    @Test
    public void queSeLogreGuardarUnItemRendimientoSinUsuarioRedirijaALogin() {
        // preparacion
        when(session.getAttribute("usuario")).thenReturn(null);

        // ejecucion
        ModelAndView modelAndView = controladorCalendario.verProgreso(session);

        // verificacion
        assertEquals("redirect:/login", modelAndView.getViewName()); // Verificar redirección a login
    }

    @Test
    public void queAlIrVerProgresoSinUsuarioRedirijaALogin() {
        // preparacion
        when(session.getAttribute("usuario")).thenReturn(null);

        // ejecucion
        ModelAndView modelAndView = controladorCalendario.verProgreso(session);

        // verificacion
        assertEquals("redirect:/login", modelAndView.getViewName()); // Verificar redirección a login
    }

    @Test
    public void queAlIrALaPantallaDeCalendarioConUsuarioAutenticadoSeRendericeCorrectamente() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);

        // ejecución
        ModelAndView modelAndView = controladorCalendario.mostrarCalendario(session);

        // verificación
        assertEquals("calendario", modelAndView.getViewName());
        assertNotNull(modelAndView.getModel().get("itemRendimiento"));
        assertEquals(usuario, modelAndView.getModel().get("usuario"));
    }

    @Test
    public void dadoQueElUsuarioEstaEnLaVistaCalendarioQueSeLogreGuardarUnItemRendimientoConExito() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);
        ItemRendimiento itemRendimiento = new ItemRendimiento();

        // ejecución
        ModelAndView modelAndView = controladorCalendario.guardarItemRendimiento(itemRendimiento, session);

        // verificación
        assertEquals("redirect:/verProgreso", modelAndView.getViewName());
        verify(servicioCalendario, times(1)).guardarItemRendimiento(itemRendimiento);
    }

    @Test
    public void queAlIntentarGuardarUnItemRendimientoDuplicadoSeMuestreUnMensajeDeError() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);
        ItemRendimiento itemRendimiento = new ItemRendimiento();
        doThrow(new ItemRendimientoDuplicadoException("No se puede guardar tu rendimiento más de una vez el mismo día."))
                .when(servicioCalendario).guardarItemRendimiento(any(ItemRendimiento.class));

        // ejecución
        ModelAndView modelAndView = controladorCalendario.guardarItemRendimiento(itemRendimiento, session);

        // verificación
        assertEquals("calendario", modelAndView.getViewName());
        assertEquals("No se puede guardar tu rendimiento más de una vez el mismo día.", modelAndView.getModel().get("error"));
        assertEquals(usuario, modelAndView.getModel().get("usuario"));
    }

    @Test
    public void queAlVerProgresoConDatosSeRendericeCorrectamente() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);
        List<DatosItemRendimiento> itemsMock = Arrays.asList(
                new DatosItemRendimiento(new ItemRendimiento(TipoRendimiento.DESCANSO)),
                new DatosItemRendimiento(new ItemRendimiento(TipoRendimiento.ALTO))
        );
        when(servicioCalendario.obtenerItemsRendimiento()).thenReturn(itemsMock);

        // ejecución
        ModelAndView modelAndView = controladorCalendario.verProgreso(session);

        // verificación
        assertEquals("verProgreso", modelAndView.getViewName());
        assertEquals(itemsMock, modelAndView.getModel().get("datosItemRendimiento"));
        assertEquals(false, modelAndView.getModel().get("sinRendimiento"));
        assertEquals(usuario, modelAndView.getModel().get("usuario"));
    }

    @Test
    public void queAlVerProgresoSinDatosSeMuestreMensajeIndicativo() {
        // preparación
        when(session.getAttribute("usuario")).thenReturn(usuario);
        when(servicioCalendario.obtenerItemsRendimiento()).thenReturn(Collections.emptyList());

        // ejecución
        ModelAndView modelAndView = controladorCalendario.verProgreso(session);

        // verificación
        assertEquals("verProgreso", modelAndView.getViewName());
        assertEquals("¿Cómo fue tu entrenamiento hoy?", modelAndView.getModel().get("mensaje"));
        assertEquals(true, modelAndView.getModel().get("sinRendimiento"));
        assertEquals(usuario, modelAndView.getModel().get("usuario"));
    }

}
