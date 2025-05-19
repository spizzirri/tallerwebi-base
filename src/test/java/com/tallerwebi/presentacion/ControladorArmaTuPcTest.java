package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControladorArmaTuPcTest {

    ControladorArmaTuPc controlador;
    MockHttpSession session;
    ServicioArmaTuPc servicioMock;


    @BeforeEach
    public void init() {
        this.servicioMock = mock(ServicioArmaTuPc.class);
        this.controlador = new ControladorArmaTuPc(this.servicioMock);
        this.session = new MockHttpSession(); // simulo una session

    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoArmarUnaPCObtengoLaVistaArmaTuPcConUnArmadoPcDtoYUnaListaDeProcesadoresComoComponenteDto(){
        // Preparacion(hecha en el init)

        List<ComponenteDto> listaDeProcesadores = Arrays.asList(
                new ComponenteDto(1L,"Procesador","Procesador1", 1000D),
                new ComponenteDto(2L,"Procesador","Procesador2", 2000D),
                new ComponenteDto(3L,"Procesador","Procesador3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("Procesador")).thenReturn(listaDeProcesadores);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.armarUnaPc();

        // Validacion
        String vistaEsperada = "arma-tu-pc/index";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("procesadores"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("procesadores")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("procesadores"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("procesadores"))), everyItem(hasProperty("tipoComponente", is("Procesador")))); // valido que en esa lista vengan solo procesadores
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnProcesadorAlArmadoEntoncesEsteSeGuardaYMantieneEnSesionYRedirigeALaVistaDeMotherboards(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarProcesador(1L, this.session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/motherboard";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getProcesador(), is(procesadorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoMotherboardsEntoncesObtengoLaListaDeMotherboardsYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        List<ComponenteDto> listaMotherboards = Arrays.asList(
                new ComponenteDto(1L,"Motherboard","Motherboard1", 1000D),
                new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D),
                new ComponenteDto(3L,"Motherboard","Motherboard3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("Motherboard")).thenReturn(listaMotherboards);
        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador para ver si se mantiene la sesion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarMotherboards(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/motherboard";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("motherboards"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("motherboards")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("motherboards"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("motherboards"))), everyItem(hasProperty("tipoComponente", is("Motherboard")))); // valido que en la lista vengan solo motherboards
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado));// valido que el procesador elegido anteriormente siga en el armadoPcDto
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnaMotherboardEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaCoolers(){
        // Preparacion
        when(servicioMock.obtenerComponenteDtoPorId(2L))
                .thenReturn(new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D));
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
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoCoolersEntoncesObtengoLaListaDeCoolersYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        List<ComponenteDto> listaCoolers = Arrays.asList(
                new ComponenteDto(1L,"Cooler","Cooler1", 1000D),
                new ComponenteDto(2L,"Cooler","Cooler2", 2000D),
                new ComponenteDto(3L,"Cooler","Cooler3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("Cooler"))
                .thenReturn(listaCoolers);

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador para ver si se mantiene la sesion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarCoolers(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/coolers";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("coolers"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("coolers")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("coolers"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("coolers"))), everyItem(hasProperty("tipoComponente", is("Cooler")))); // valido que en la lista vengan solo Coolers
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que el procesador ya elegido se encuentre en el armadoPcDto
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnCoolerEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeMemorias(){
        // Preparacion
        when(servicioMock.obtenerComponenteDtoPorId(3L))
                .thenReturn(new ComponenteDto(3L,"Cooler","Cooler3", 3000D));

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarCooler(3L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/memoria";

        ComponenteDto coolerEsperado = new ComponenteDto();
        coolerEsperado.setId(3L);
        coolerEsperado.setTipoComponente("Cooler");
        coolerEsperado.setModelo("Cooler3");
        coolerEsperado.setPrecio(3000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getCooler(), is(coolerEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoMemoriasEntoncesObtengoLaListaDeMemoriasYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        List<ComponenteDto> listaMemorias = Arrays.asList(
                new ComponenteDto(1L,"Memoria","Memoria1", 1000D),
                new ComponenteDto(2L,"Memoria","Memoria2", 2000D),
                new ComponenteDto(3L,"Memoria","Memoria3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("Memoria"))
                .thenReturn(listaMemorias);

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarMemorias(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/memoria";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("memorias"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("memorias")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("memorias"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("memorias"))), everyItem(hasProperty("tipoComponente", is("Memoria")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSelecciono4UnidadesDeUnaMemoriaEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeGpus(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(3L))
                .thenReturn(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D));

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarMemoria(3L, 4, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/gpu";

        List<ComponenteDto> memoriasEsperadas =
                Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSelecciono1UnidadDeUnaMemoriaEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDeMemoriasParaCargarMas(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarMemoria(3L, 1, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/memoria";

        List<ComponenteDto> memoriasEsperadas =
                Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcConUnArmadoCon3UnidadesDeMemoria1CuandoIntentoAgregar2UnidadesDeMemoria2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeMemoriasConUnErrorDeLimite(){
        // Preparacion

        this.controlador.seleccionarMemoria(1L, 3, session);

        // Ejecucion

        ModelAndView modelAndView = controlador.seleccionarMemoria(2L, 2, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/memoria";
        String errorEsperado = "Supero el limite de memorias";

        List<ComponenteDto> memoriasEsperadas =
                Arrays.asList(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D)); // solo las 3 rams inicialmente esperadas

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
        assertNotNull(modelAndView.getModel().get("error"));
        assertThat(modelAndView.getModel().get("error"), equalTo(errorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoGpusEntoncesObtengoLaListaDeGpusYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarGpus(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/gpu";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("gpus"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("gpus")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("gpus"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("gpus"))), everyItem(hasProperty("tipoComponente", is("Gpu")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnaGpuEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeAlmacenamientos(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarGpu(1L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/almacenamiento";

        ComponenteDto gpuEsperada = new ComponenteDto();
        gpuEsperada.setId(1L);
        gpuEsperada.setTipoComponente("Gpu");
        gpuEsperada.setModelo("Gpu1");
        gpuEsperada.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGpu(), is(gpuEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoAlmacenamientosEntoncesObtengoLaListaDeAlmacenamientosYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarAlmacenamientos(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/almacenamiento";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("almacenamientos"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("almacenamientos")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("almacenamientos"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("almacenamientos"))), everyItem(hasProperty("tipoComponente", is("Almacenamiento")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSelecciono6UnidadesDeUnAlmacenamientoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeFuentes(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarAlmacenamiento(2L, 6, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/fuente";

        List<ComponenteDto> almacenamientoEsperado =
                Arrays.asList(new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSelecciono4UnidadesDeUnAlmacenamientoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeAlmacenamientoParaPoderSeguirAgregando(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarAlmacenamiento(2L, 4, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/almacenamiento";

        List<ComponenteDto> almacenamientoEsperado =
                Arrays.asList(new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCon4UnidadesDeAlmacenamiento1CuandoSelecciono3UnidadesDeAlmacenamiento2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeAlmacenamientoConUnErrorDeLimite(){
        // Preparacion

        this.controlador.seleccionarAlmacenamiento(1L, 4, session);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarAlmacenamiento(2L, 3, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/almacenamiento";
        String errorEsperado = "Supero el limite de almacenamiento";

        List<ComponenteDto> almacenamientoEsperado =
                Arrays.asList(new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
        assertNotNull(modelAndView.getModel().get("error"));
        assertThat(modelAndView.getModel().get("error"), equalTo(errorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoFuentesEntoncesObtengoLaListaDeFuentesYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarFuentes(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/fuente";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("fuentes"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("fuentes")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("fuentes"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("fuentes"))), everyItem(hasProperty("tipoComponente", is("Fuente")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnaFuenteEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeGabinetes(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarFuente(1L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/gabinete";

        ComponenteDto fuenteEsperada = new ComponenteDto();
        fuenteEsperada.setId(1L);
        fuenteEsperada.setTipoComponente("Fuente");
        fuenteEsperada.setModelo("Fuente1");
        fuenteEsperada.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getFuente(), is(fuenteEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoGabinetesEntoncesObtengoLaListaDeGabinetesYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarGabinetes(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/gabinete";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("gabinetes"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("gabinetes")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("gabinetes"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("gabinetes"))), everyItem(hasProperty("tipoComponente", is("Gabinete")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnGabineteEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeMonitores(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarGabinete(2L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/monitor";

        ComponenteDto gabineteEsperado = new ComponenteDto();
        gabineteEsperado.setId(2L);
        gabineteEsperado.setTipoComponente("Gabinete");
        gabineteEsperado.setModelo("Gabinete2");
        gabineteEsperado.setPrecio(2000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGabinete(), is(gabineteEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoMonitoresEntoncesObtengoLaListaDeMonitoresYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarMonitores(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/monitor";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("monitores"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("monitores")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("monitores"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("monitores"))), everyItem(hasProperty("tipoComponente", is("Monitor")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnMonitorEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDePerifericos(){
        // Preparacion


        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarMonitor(3L, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/periferico";

        ComponenteDto monitorEsperado = new ComponenteDto();
        monitorEsperado.setId(3L);
        monitorEsperado.setTipoComponente("Monitor");
        monitorEsperado.setModelo("Monitor3");
        monitorEsperado.setPrecio(3000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMonitor(), is(monitorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoPerifericosEntoncesObtengoLaListaDePerifericosYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        when(servicioMock.obtenerComponenteDtoPorId(1L))
                .thenReturn(new ComponenteDto(1L,"Procesador","Procesador1", 1000D));

        this.controlador.seleccionarProcesador(1L, session);// cargo un procesador

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarPerifericos(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/periferico";


        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("perifericos"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("perifericos")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("perifericos"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("perifericos"))), everyItem(hasProperty("tipoComponente", is("Periferico")))); // valido que en la lista vengan solo Memorias
        assertThat(((ArmadoPcDto)modelAndView.getModel().get("armadoPcDto")).getProcesador(), is(procesadorEsperado)); // valido que la motherboard ya elegida se encuentre en el armadoPcDto(que estaba en la sesion)
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnPerifericoEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDePerifericosParaSeguirAgregando(){
        // Preparacion

        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarPeriferico(1L, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/periferico";

        List<ComponenteDto> perifericosEsperados = Arrays.asList(new ComponenteDto(1L, "Periferico", "Periferico1", 1000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), perifericosEsperados);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoYaSeSelecciono10PerifericoEntoncesEnElArmadoPcDtoDeLaSesionObtengoLaVistaDeResumen(){
        // Preparacion

        this.controlador.seleccionarPeriferico(1L, session);
        this.controlador.seleccionarPeriferico(2L, session);
        this.controlador.seleccionarPeriferico(3L, session);
        this.controlador.seleccionarPeriferico(4L, session);
        this.controlador.seleccionarPeriferico(5L, session);
        this.controlador.seleccionarPeriferico(6L, session);
        this.controlador.seleccionarPeriferico(7L, session);
        this.controlador.seleccionarPeriferico(8L, session);
        this.controlador.seleccionarPeriferico(9L, session);


        // Ejecucion
        ModelAndView modelAndView = this.controlador.seleccionarPeriferico(1L, session);

        // Validacion
        String vistaEsperada = "redirect:arma-tu-pc/resumen";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoObtengoElResumenObtengoLaVistaDeResumenConElArmadoPcDtoDeLaSession(){
        // Preparacion

        this.controlador.seleccionarProcesador(1L, session);
        this.controlador.seleccionarMotherboard(2L, session);
        this.controlador.seleccionarCooler(3L, session);
        this.controlador.seleccionarMemoria(1L,2, session);
        this.controlador.seleccionarGpu(2L, session);
        this.controlador.seleccionarAlmacenamiento(3L, 3, session);
        this.controlador.seleccionarFuente(1L, session);
        this.controlador.seleccionarGabinete(2L, session);
        this.controlador.seleccionarMonitor(3L, session);
        this.controlador.seleccionarPeriferico(1L, session);
        this.controlador.seleccionarPeriferico(2L, session);


        // Ejecucion
        ModelAndView modelAndView = this.controlador.obtenerResumen(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/resumen";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);
        ComponenteDto motherboardEsperada = new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D);
        ComponenteDto coolerEsperado = new ComponenteDto(3L, "Cooler", "Cooler3", 3000D);
        List<ComponenteDto> memoriasEsperadas = Arrays.asList(
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D));
        ComponenteDto gpuEsperada = new ComponenteDto(2L, "Gpu", "Gpu2", 2000D);
        List<ComponenteDto> almacenamientoEsperado = Arrays.asList(
                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D),
                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D),
                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D));
        ComponenteDto fuenteEsperada = new ComponenteDto(1L, "Fuente", "Fuente1", 1000D);
        ComponenteDto gabineteEsperado = new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D);
        ComponenteDto monitorEsperado = new ComponenteDto(3L, "Monitor", "Monitor3", 3000D);
        List<ComponenteDto> perifericosEsperados = Arrays.asList(
                new ComponenteDto(1L, "Periferico", "Periferico1", 1000D),
                new ComponenteDto(2L, "Periferico", "Periferico2", 2000D));

//        ArmadoPcDto armadoEsperado = new ArmadoPcDto(procesadorEsperado,
//                                                    motherboardEsperada,
//                                                    coolerEsperado,
//                                                    memoriasEsperadas,
//                                                    gpuEsperada,
//                                                    almacenamientoEsperado,
//                                                    fuenteEsperada,
//                                                    gabineteEsperado,
//                                                    monitorEsperado,
//                                                    perifericosEsperados
//                                                 );

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getProcesador(), procesadorEsperado);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMotherboard(), motherboardEsperada);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getCooler(), coolerEsperado);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGpu(), gpuEsperada);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getFuente(), fuenteEsperada);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGabinete(), gabineteEsperado);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMonitor(), monitorEsperado);
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), perifericosEsperados);

    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoQuieroObtenerElResumenDeUnArmadoSinProcesadorMotherboardCoolerOGabineteObtengoLaVistaDeResumenConUnMensajeDeError(){
        // Preparacion

        this.controlador.seleccionarMemoria(1L,2, session);
        this.controlador.seleccionarGpu(2L, session);
        this.controlador.seleccionarAlmacenamiento(3L, 3, session);
        this.controlador.seleccionarFuente(1L, session);
        this.controlador.seleccionarMonitor(3L, session);
        this.controlador.seleccionarPeriferico(1L, session);
        this.controlador.seleccionarPeriferico(2L, session);


        // Ejecucion
        ModelAndView modelAndView = this.controlador.obtenerResumen(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/resumen";
        String errorEsperado = "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(modelAndView.getModel().get("error"), equalTo(errorEsperado));
    }
}
