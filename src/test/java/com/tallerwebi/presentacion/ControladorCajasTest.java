package com.tallerwebi.presentacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import com.tallerwebi.dominio.ServicioCaja;
import com.tallerwebi.dominio.excepcion.NoHayCajasExistente;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.ArrayList;
import java.util.List;

public class ControladorCajasTest {

    
    private ServicioCaja servicioCaja;
    private ControladorCajas controladorCajas;

    @BeforeEach
    public void init(){
        this.servicioCaja =  mock(ServicioCaja.class);
        this.controladorCajas = new ControladorCajas(this.servicioCaja);
    }


    @Test
    public void dadoQueSePuedenConsultarCajasCuandoLasConsultoSinHaberAgregadoObtengoUnMensajeNoHayCajas() {

        // Peticion de tipo GET

        // preparacion
        // List<CajaDto> cajasDto = new ArrayList<>();
        // when(servicioCaja.obtener()).thenReturn(cajasDto);
        doThrow(NoHayCajasExistente.class).when(servicioCaja).obtener();

        // ejecucion
        ModelAndView modelAndView = controladorCajas.mostrarCajas();

        // verificacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("cajas"));
        List<CajaDto> cajasDtoObtenidas = (List<CajaDto>) modelAndView.getModel().get("cajas");
        assertThat(cajasDtoObtenidas.size(), equalTo(0));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("No hay cajas."));
    }

    @Test
    public void dadoQueExistenCajasCuandoLasConsultoSeMuestran3Cajas() {

        // Peticion de tipo GET
        List<CajaDto> cajasDto = new ArrayList<>();
        cajasDto.add(new CajaDto());
        cajasDto.add(new CajaDto());
        cajasDto.add(new CajaDto());
        when(servicioCaja.obtener()).thenReturn(cajasDto);

        // preparacion

        // ejecucion
        ModelAndView modelAndView = controladorCajas.mostrarCajas();

        // verificacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("cajas"));
        List<CajaDto> cajasDtoObtenidas = (List<CajaDto>) modelAndView.getModel().get("cajas");
        assertThat(cajasDtoObtenidas.size(), equalTo(3));
        assertThat(modelAndView.getModel().get("exito").toString(), equalToIgnoringCase("Hay cajas."));
    }

    // Peticion PUT, POST

}
