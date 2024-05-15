package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.calendario.ServicioCalendario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

public class ControladorVerProgresoTest {

    private ServicioCalendario servicioCalendario;
    private ControladorVerProgreso controladorVerProgreso;
//  private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init() {
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.controladorVerProgreso = new ControladorVerProgreso(this.servicioCalendario);
    }

    @Test
    public void queAlIrALaPantallaDeVerProgresoMeMuestreLaVistaDeCalendario(){
        ModelAndView modelAndView = this.controladorVerProgreso.irVerProgreso();

        assertThat(modelAndView.getViewName(),equalTo("verProgreso"));//vista correcta
    }



}
