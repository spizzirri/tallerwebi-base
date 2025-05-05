package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ControladorArmaTuPcTest {

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoArmarUnaPCObtengoLaVistaArmaTuPcConUnArmadoPcDtoYUnaListaDeProcesadoresComoComponenteDto(){
        // Preparacion
        ControladorArmaTuPc controlador = new ControladorArmaTuPc();

        // Ejecucion
        ModelAndView modelAndView = controlador.armarUnaPc();

        // Validacion
        String vistaEsperada = "arma-tu-pc";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("procesadores"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertThat(((List<?>)(modelAndView.getModel().get("procesadores"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnProcesadorAArmadoPcDtoEntoncesEsteSeGuardaYObtengoLaVistaDeMotherboardsConUnaListaDeMotherboardsComoComponenteDto(){
        // Preparacion

        // Ejecucion

        // Validacion
    }
}
