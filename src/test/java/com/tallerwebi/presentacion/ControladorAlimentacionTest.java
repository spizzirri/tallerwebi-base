package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.alimentacion.ServicioAlimentacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void queAlIrALaPantallaDeAlimentacionMeMuestreLaVistaDeAlimentacion(){
        ModelAndView modelAndView = this.controladorAlimentacion.irAlimentacion();

        assertThat(modelAndView.getViewName(),equalTo("alimentacion"));
    }
}