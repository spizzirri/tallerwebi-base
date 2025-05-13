package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ControladorArmaTuPcTest {

    ControladorArmaTuPc controlador;

    @BeforeEach
    public void init() {
        this.controlador = new ControladorArmaTuPc();
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoArmarUnaPCObtengoLaVistaArmaTuPcConUnArmadoPcDtoYUnaListaDeProcesadoresComoComponenteDto(){
        // Preparacion(hecha en el init)

        // Ejecucion
        ModelAndView modelAndView = this.controlador.armarUnaPc();

        // Validacion
        String vistaEsperada = "arma-tu-pc/index";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("procesadores"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertThat(((List<?>)(modelAndView.getModel().get("procesadores"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnProcesadorAlArmadoEntoncesEsteSeGuardaYMantieneEnSesionYRedirigeALaVistaDeMotherboards(){
        // Preparacion

        MockHttpSession session = new MockHttpSession(); // simulo una session http

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarProcesador(1L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/motherboard";

        ComponenteDto procesadorEsperado = new ComponenteDto();
        procesadorEsperado.setId(1L);
        procesadorEsperado.setTipoComponente("Procesador");
        procesadorEsperado.setModelo("Procesador1");
        procesadorEsperado.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getProcesador(), is(procesadorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcYUnArmadoConProcesadorEnLaSesionCuandoCargoMotherboardsEntoncesObtengoLaListaDeMotherboardsYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        MockHttpSession session = new MockHttpSession(); // simulo una session http
        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarMotherboards(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/motherboard";

        ComponenteDto procesadorEsperado = new ComponenteDto();
        procesadorEsperado.setId(1L);
        procesadorEsperado.setTipoComponente("Procesador");
        procesadorEsperado.setModelo("Procesador1");
        procesadorEsperado.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("motherboards"), instanceOf(List.class)); // valido que se haya devuelto una lista
        // deberia verificar que sean de tipo "motherboard".
        assertThat(((List<?>)(modelAndView.getModel().get("motherboards"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnaMotherboardEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaCoolers(){
        // Preparacion

        MockHttpSession session = new MockHttpSession(); // simulo una session http

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarMotherboard(2L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/cooler";

        ComponenteDto motherEsperada = new ComponenteDto();
        motherEsperada.setId(2L);
        motherEsperada.setTipoComponente("Motherboard");
        motherEsperada.setModelo("Motherboard2");
        motherEsperada.setPrecio(2000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMotherboard(), is(motherEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcYUnArmadoConProcesadorYMotherEnLaSesionCuandoCargoCoolersEntoncesObtengoLaListaDeCoolersYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        MockHttpSession session = new MockHttpSession(); // simulo una session http
        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador
        this.controlador.seleccionarMotherboard(2L, session);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarCoolers(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/coolers";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);
        ComponenteDto motherEsperada = new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("coolers"), instanceOf(List.class)); // valido que se haya devuelto una lista
        // deberia verificar que sean de tipo "coolers".
        assertThat(((List<?>)(modelAndView.getModel().get("coolers"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado));
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getMotherboard(), is(motherEsperada));
    }
}
