package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.alimentacion.ServicioAlimentacion;

import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

public class ControladorAlimentacionTest {

    private ServicioAlimentacion servicioAlimentacion;
    private ControladorAlimentacion controladorAlimentacion;
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        this.servicioAlimentacion = mock(ServicioAlimentacion.class);
        this.controladorAlimentacion = new ControladorAlimentacion(this.servicioAlimentacion);
    }

    @Test
    public void queAlIrALaPantallaDeAlimentacionMeMuestreLaVistaDeAlimentacion() {
        // Crear un usuario simulado y una sesión simulada
        Usuario usuarioSimulado = new Usuario();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("usuario", usuarioSimulado);

        // Invocar el método del controlador con la sesión simulada
        ModelAndView modelAndView = controladorAlimentacion.irAlimentacion(session);

        // Verificar que el nombre de la vista sea "alimentacion"
        assertThat(modelAndView.getViewName(), equalTo("alimentacion"));
    }

}