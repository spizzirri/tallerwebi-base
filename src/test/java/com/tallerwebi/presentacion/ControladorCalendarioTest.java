package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.infraestructura.RepositorioCalendarioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import com.tallerwebi.dominio.calendario.ServicioCalendario;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ControladorCalendarioTest {

  private ServicioCalendario servicioCalendario;
  private ControladorCalendario controladorCalendario;
//  private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init() {
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.controladorCalendario = new ControladorCalendario(this.servicioCalendario);
    }

    @Test
    public void queAlIrALaPantallaDeCalendarioMeMuestreLaVistaDeCalendario(){
        ModelAndView modelAndView = this.controladorCalendario.irCalendario();
        //mensaje
        String message = modelAndView.getModel().get("message").toString();

        assertThat(modelAndView.getViewName(),equalTo("calendario"));//vista correcta
        assertThat(message, equalToIgnoringCase("¿Cómo fue tu entrenamiento hoy?"));//mensaje correcto
    }

    @Test
    public void testGuardarItemRendimientoSuccess() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.DESCANSO);

        // Call the guardarItemRendimiento method
        ModelAndView modelAndView = controladorCalendario.verProgreso(itemRendimiento);

        // Verify that servicioCalendario.guardarItemRendimiento was called
        verify(servicioCalendario).guardarItemRendimiento(itemRendimiento);

        ModelMap model = modelAndView.getModelMap();
        assertEquals("redirect:/verProgreso", modelAndView.getViewName());
    }
}
