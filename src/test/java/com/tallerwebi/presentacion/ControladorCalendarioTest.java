package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.infraestructura.RepositorioCalendarioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import com.tallerwebi.dominio.calendario.ServicioCalendario;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    public void queAlIrALaPantallaDeCalendarioMeMuestreLaVistaDeCalendario() {
        ModelAndView modelAndView = controladorCalendario.irCalendario(session);
        // mensaje
        String message = modelAndView.getModel().get("message").toString();

        assertThat(modelAndView.getViewName(), equalTo("calendario")); // vista correcta
        assertThat(message, equalToIgnoringCase("¿Cómo fue tu entrenamiento hoy?")); // mensaje correcto
        assertTrue(modelAndView.getModel().containsKey("usuario")); // verificar que el usuario está en el modelo
    }

    @Test
    public void queSeLogreGuardarUnItemRendimientoRedireccionandoHaciaVerProgreso() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.DESCANSO);

        ModelAndView modelAndView = controladorCalendario.verProgreso(itemRendimiento, session);

        verify(servicioCalendario).guardarItemRendimiento(itemRendimiento);

        ModelMap model = modelAndView.getModelMap();
        assertEquals("redirect:/verProgreso", modelAndView.getViewName());
    }

    @Test
    public void queAlGuardarMasDeUnaVezUnItemRendimientoElMismoDiaAparezcaLaExcepcion() throws Exception {
        ItemRendimiento itemRendimiento = new ItemRendimiento();
        itemRendimiento.setFecha(LocalDate.now());
        doThrow(new ItemRendimientoDuplicadoException("No se puede guardar tu rendimiento más de una vez el mismo día."))
                .when(servicioCalendario)
                .guardarItemRendimiento(itemRendimiento);

        ModelAndView modelAndView = controladorCalendario.verProgreso(itemRendimiento, session);

        assertEquals("calendario", modelAndView.getViewName());
        assertTrue(modelAndView.getModel().containsKey("error"));
        assertEquals("No se puede guardar tu rendimiento más de una vez el mismo día.", modelAndView.getModel().get("error"));
        assertTrue(modelAndView.getModel().containsKey("usuario")); // verificar que el usuario está en el modelo
    }

}
