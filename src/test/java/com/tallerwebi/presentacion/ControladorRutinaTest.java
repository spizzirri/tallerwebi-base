package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorRutinaTest {

    private ServicioRutina servicioRutina;
    private ControladorRutina controladorRutina;

    @BeforeEach
    public void init() {
        this.servicioRutina = mock(ServicioRutina.class);
        this.controladorRutina = new ControladorRutina(this.servicioRutina);
    }

    @Test
    public void queAlIrALaPantallaDeRutinaMeMuestreLaVistaDeRutina(){
        ModelAndView modelAndView = this.controladorRutina.irRutina();
        assertThat(modelAndView.getViewName(),equalTo("rutina"));
    }

    @Test
    public void queAlIrALaPantallaDeRutinaSeMuestreLaVistaConRutinas(){
        //preparacion
        List <Rutina> rutinasMock = new ArrayList<>();
        rutinasMock.add(new Rutina());
        rutinasMock.add(new Rutina());
        rutinasMock.add(new Rutina());
        when(this.servicioRutina.obtenerRutinas()).thenReturn(rutinasMock);
        //ejecucion
        ModelAndView modelAndView = this.controladorRutina.irRutina();
        //verificacion
        assertThat(modelAndView.getViewName(),equalTo("rutina"));
        List<Rutina> rutinasObtenidas = (List<Rutina>) modelAndView.getModel().get("rutinas");
        assertThat(rutinasObtenidas.size(),equalTo(3));
    }

}
