package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.infraestructura.RepositorioCalendarioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import com.tallerwebi.dominio.calendario.ServicioCalendario;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorCalendarioTest {

  private ServicioCalendario servicioCalendario;
  private ControladorCalendario controladorCalendario;

    @Autowired
    private MockMvc mockMvc;

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

//    @Test
//    public void queAlGuardarMasDeUnaVezUnItemRendimientoElMismoDiaAparezcaLaExcepcion() throws Exception {
//        ItemRendimiento itemRendimiento = new ItemRendimiento();
//        itemRendimiento.setFecha(LocalDate.now());
//
//        doThrow(new ItemRendimientoDuplicadoException("No se puede guardar más de una vez el mismo día."))
//                .when(servicioCalendario)
//                .guardarItemRendimiento(itemRendimiento);
//
//        // Act & Assert
//        mockMvc.perform(post("/calendario")
//                        .flashAttr("itemRendimiento", itemRendimiento))
//                .andExpect(status().isOk())
//                .andExpect(view().name("calendario"))
//                .andExpect(model().attributeExists("error"))
//                .andExpect(model().attribute("error", "No se puede guardar tu rendimiento más de una vez el mismo día."));
//        ItemRendimiento itemRendimiento = new ItemRendimiento();
//        itemRendimiento.setFecha(LocalDate.now());
//        itemRendimiento.setDescripcion("Descripción de prueba");
//
//        doThrow(new DuplicateItemRendimientoException("No se puede guardar más de una vez el mismo día."))
//                .when(itemRendimientoService)
//                .guardar(itemRendimiento);
//
//        // Act
//        ModelAndView modelAndView = ControladorCalendario.irCalendario(itemRendimiento);
//
//        // Assert
//        assertEquals("calendario", modelAndView.getViewName());
//        assertTrue(modelAndView.getModel().containsKey("error"));
//        assertEquals("No se puede guardar tu rendimiento más de una vez el mismo día.", modelAndView.getModel().get("error"));
//    }
//    }
}
