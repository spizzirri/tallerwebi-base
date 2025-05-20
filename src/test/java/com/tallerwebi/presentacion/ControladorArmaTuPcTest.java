package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarComponentesDeTipoProcesadorObtengoUnaListaDeComponenteDtoDeTipoProcesadorYUnArmadoPcDtoParaCargar(){
        // Preparacion(hecha en el init)

        List<ComponenteDto> listaProcesadores = Arrays.asList(
                new ComponenteDto(1L,"Procesador","Procesador1", 1000D),
                new ComponenteDto(2L,"Procesador","Procesador2", 2000D),
                new ComponenteDto(3L,"Procesador","Procesador3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("procesador")).thenReturn(listaProcesadores);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("procesador", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/procesador";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("procesadorLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("procesadorLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("procesadorLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("procesadorLista"))), everyItem(hasProperty("tipoComponente", is("Procesador")))); // valido que en esa lista vengan solo procesadores
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnProcesadorAlArmadoEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeMotherboards() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("procesador", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("procesador", 1L, 1,session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/motherboard";

        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getProcesador(), is(procesadorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMotherboardsEntoncesObtengoUnaListaDeComponenteDtoDeTipoMotherboardYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMotherboards = Arrays.asList(
                new ComponenteDto(1L,"Motherboard","Motherboard1", 1000D),
                new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D),
                new ComponenteDto(3L,"Motherboard","Motherboard3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("motherboard")).thenReturn(listaMotherboards);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("motherboard", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/motherboard";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("motherboardLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("motherboardLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("motherboardLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("motherboardLista"))), everyItem(hasProperty("tipoComponente", is("Motherboard")))); // valido que en la lista vengan solo motherboards
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnaMotherboardAlArmadoEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeCoolers() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setMotherboard(new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("motherboard", armadoPcDtoARetornar)).thenReturn(false);
        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("motherboard", 2L, 1,session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/cooler";

        ComponenteDto motherEsperada = new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getMotherboard(), is(motherEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarCoolersEntoncesObtengoUnaListaDeComponenteDtoDeTipoCoolerYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaCoolers = Arrays.asList(
                new ComponenteDto(1L,"Cooler","Cooler1", 1000D),
                new ComponenteDto(2L,"Cooler","Cooler2", 2000D),
                new ComponenteDto(3L,"Cooler","Cooler3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("cooler")).thenReturn(listaCoolers);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("cooler", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/cooler";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("coolerLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("coolerLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("coolerLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("coolerLista"))), everyItem(hasProperty("tipoComponente", is("Cooler")))); // valido que en la lista vengan solo Coolers
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnCoolerAlArmadoEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeMemorias() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setCooler(new ComponenteDto(3L, "Cooler", "Cooler3", 3000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("cooler", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("cooler", 3L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/memoria";

        ComponenteDto coolerEsperado = new ComponenteDto();
        coolerEsperado.setId(3L);
        coolerEsperado.setTipoComponente("Cooler");
        coolerEsperado.setModelo("Cooler3");
        coolerEsperado.setPrecio(3000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getCooler(), is(coolerEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMemoriasEntoncesObtengoUnaListaDeComponenteDtoDeTipoMemoriaYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMemorias = Arrays.asList(
                new ComponenteDto(1L,"Memoria","Memoria1", 1000D),
                new ComponenteDto(2L,"Memoria","Memoria2", 2000D),
                new ComponenteDto(3L,"Memoria","Memoria3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("memoria")).thenReturn(listaMemorias);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("memoria",session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/memoria";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("memoriaLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("memoriaLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("memoriaLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("memoriaLista"))), everyItem(hasProperty("tipoComponente", is("Memoria")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar4UnidadesDeUnaMemoriaAlArmadoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeGpus() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setRams(Arrays
                .asList(
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 3L, 4, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/gpu";

        List<ComponenteDto> memoriasEsperadas =
                Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar1UnidadDeUnaMemoriaEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDeMemoriasParaCargarMas() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setRams(Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D)));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 3L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/memoria";

        List<ComponenteDto> memoriasEsperadas =
                Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcConUnArmadoCon3UnidadesDeMemoria1CuandoPidoCargar2UnidadesMasDeMemoria2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeMemoriasConUnErrorDeLimite() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setRams(Arrays
                .asList(
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar)
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(true);

        this.controlador.agregarComponenteAlArmado("memoria", 1L, 3, session);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 2L, 2, session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/memoria";
        String errorEsperado = "Supero el limite de memoria de su armado";

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
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarGpusEntoncesObtengoUnaListaDeComponenteDtoDeTipoGpuYUnArmadoPcDtoParaCargar() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        List<ComponenteDto> listaGpus = Arrays.asList(
                new ComponenteDto(1L,"Gpu","Gpu1", 1000D),
                new ComponenteDto(2L,"Gpu","Gpu2", 2000D),
                new ComponenteDto(3L,"Gpu","Gpu3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("gpu")).thenReturn(listaGpus);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("gpu", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/gpu";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("gpuLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("gpuLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("gpuLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("gpuLista"))), everyItem(hasProperty("tipoComponente", is("Gpu")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnaGpuAlArmadoEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeAlmacenamientos() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setGpu(new ComponenteDto(1L, "Gpu", "Gpu1", 1000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("gpu", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("gpu", 1L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/almacenamiento";

        ComponenteDto gpuEsperada = new ComponenteDto();
        gpuEsperada.setId(1L);
        gpuEsperada.setTipoComponente("Gpu");
        gpuEsperada.setModelo("Gpu1");
        gpuEsperada.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGpu(), is(gpuEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarAlmacenamientosEntoncesObtengoUnaListaDeComponenteDtoDeTipoAlmacenamientoYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaAlmacenamientos = Arrays.asList(
                new ComponenteDto(1L,"Almacenamiento","Almacenamiento1", 1000D),
                new ComponenteDto(2L,"Almacenamiento","Almacenamiento2", 2000D),
                new ComponenteDto(3L,"Almacenamiento","Almacenamiento3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("almacenamiento")).thenReturn(listaAlmacenamientos);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("almacenamiento", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/almacenamiento";



        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("almacenamientoLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("almacenamientoLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("almacenamientoLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("almacenamientoLista"))), everyItem(hasProperty("tipoComponente", is("Almacenamiento")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar6UnidadesDeUnAlmacenamientoAlArmadoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeFuentes() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setAlmacenamiento(Arrays
                .asList(
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 6, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/fuente";

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
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar4UnidadesDeUnAlmacenamientoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDeAlmacenamientosParaCargarMas() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setAlmacenamiento(Arrays.asList(
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D)
                ));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 4, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/almacenamiento";

        List<ComponenteDto> almacenamientoEsperado =
                Arrays.asList(new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D));
        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCon4UnidadesDeAlmacenamiento1CuandoPidoCargar3UnidadesDeAlmacenamiento2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeAlmacenamientoConUnErrorDeLimite() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setAlmacenamiento(Arrays
                .asList(
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar)
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(true);

        this.controlador.agregarComponenteAlArmado("almacenamiento", 1L, 4, session);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 2, session);


        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/almacenamiento";
        String errorEsperado = "Supero el limite de almacenamiento de su armado";

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
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarFuentesEntoncesObtengoUnaListaDeComponenteDtoDeTipoFuenteYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaFuentes = Arrays.asList(
                new ComponenteDto(1L,"Fuente","Fuente1", 1000D),
                new ComponenteDto(2L,"Fuente","Fuente2", 2000D),
                new ComponenteDto(3L,"Fuente","Fuente3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("fuente")).thenReturn(listaFuentes);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("fuente", session);


        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/fuente";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("fuenteLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("fuenteLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("fuenteLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("fuenteLista"))), everyItem(hasProperty("tipoComponente", is("Fuente")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnaFuenteAlArmadoEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeGabinetes() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setFuente(new ComponenteDto(1L, "Fuente", "Fuente1", 1000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("fuente", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("fuente", 1L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/gabinete";

        ComponenteDto fuenteEsperada = new ComponenteDto();
        fuenteEsperada.setId(1L);
        fuenteEsperada.setTipoComponente("Fuente");
        fuenteEsperada.setModelo("Fuente1");
        fuenteEsperada.setPrecio(1000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getFuente(), is(fuenteEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarGabinetesEntoncesObtengoUnaLaListaDeComponenteDtoDeTipoGabineteYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaGabinetes = Arrays.asList(
                new ComponenteDto(1L,"Gabinete","Gabinete1", 1000D),
                new ComponenteDto(2L,"Gabinete","Gabinete2", 2000D),
                new ComponenteDto(3L,"Gabinete","Gabinete3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("gabinete")).thenReturn(listaGabinetes);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("gabinete", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/gabinete";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("gabineteLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("gabineteLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("gabineteLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("gabineteLista"))), everyItem(hasProperty("tipoComponente", is("Gabinete")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnGabineteAlArmadoEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeMonitores() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setGabinete(new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("gabinete", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("gabinete", 2L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/monitor";

        ComponenteDto gabineteEsperado = new ComponenteDto();
        gabineteEsperado.setId(2L);
        gabineteEsperado.setTipoComponente("Gabinete");
        gabineteEsperado.setModelo("Gabinete2");
        gabineteEsperado.setPrecio(2000D);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGabinete(), is(gabineteEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMonitoresEntoncesObtengoUnaLaListaDeComponenteDtoDeTipoMonitorYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMonitores = Arrays.asList(
                new ComponenteDto(1L,"Monitor","Monitor1", 1000D),
                new ComponenteDto(2L,"Monitor","Monitor2", 2000D),
                new ComponenteDto(3L,"Monitor","Monitor3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("monitor")).thenReturn(listaMonitores);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("monitor", session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/monitor";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("monitorLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("monitorLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("monitorLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("monitorLista"))), everyItem(hasProperty("tipoComponente", is("Monitor")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnMonitorAlArmadoEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDePerifericos() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setMonitor(new ComponenteDto(3L, "Monitor", "Monitor3", 3000D));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("monitor", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("monitor", 3L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/periferico";

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

        List<ComponenteDto> listaPerifericos = Arrays.asList(
                new ComponenteDto(1L,"Periferico","Periferico1", 1000D),
                new ComponenteDto(2L,"Periferico","Periferico2", 2000D),
                new ComponenteDto(3L,"Periferico","Periferico3", 3000D)
        );

        when(servicioMock.obtenerListaDeComponentesDto("periferico")).thenReturn(listaPerifericos);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.cargarComponentes("periferico", session);


        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/periferico";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada)); // valido si la vista es la correcta
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class)); // valido que se haya devuelto un armadoPcDto para que lo llene en el usuario en el proceso de armado
        assertThat(modelAndView.getModel().get("perifericoLista"), instanceOf(List.class)); // valido que se haya devuelto una lista
        assertFalse(((List<?>)modelAndView.getModel().get("perifericoLista")).isEmpty()); // que la lista no venga vacia
        assertThat(((List<?>)(modelAndView.getModel().get("perifericoLista"))), everyItem(instanceOf(ComponenteDto.class)));// valido que esa lista tenga componentesDto
        assertThat(((List<?>)(modelAndView.getModel().get("perifericoLista"))), everyItem(hasProperty("tipoComponente", is("Periferico")))); // valido que en la lista vengan solo Memorias
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoSeleccionoUnPerifericoEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDePerifericosParaCargarMas() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setPerifericos(Arrays.asList(new ComponenteDto(1L, "Periferico", "Periferico1", 1000D)));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.sePuedeAgregarMasUnidades("periferico", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("periferico", 1L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/periferico";

        List<ComponenteDto> perifericosEsperados = Arrays.asList(new ComponenteDto(1L, "Periferico", "Periferico1", 1000D));

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), perifericosEsperados);
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoYaSeSelecciono10PerifericoEntoncesEnElArmadoPcDtoDeLaSesionObtengoLaVistaDeResumen() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        List<ComponenteDto> perifericos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            perifericos.add(new ComponenteDto((long) i, "Periferico", "Periferico" + i, i*1000D));
        }

        ArmadoPcDto armadoCon10Perifericos = new ArmadoPcDto();
        armadoCon10Perifericos.setPerifericos(perifericos);

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoCon10Perifericos);

        when(servicioMock.sePuedeAgregarMasUnidades(eq("periferico"), eq(armadoCon10Perifericos)))
                .thenReturn(false);

        // Ejecución
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("periferico", 1L, 1, session);
        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/resumen";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
    }

// verificar como testear resumen
//    @Test
//    public void dadoQueExisteUnControladorArmaTuPcCuandoObtengoElResumenObtengoLaVistaDeResumenConElArmadoPcDtoDeLaSession(){
//        // Preparacion
//
//        // Ejecucion
//        ModelAndView modelAndView = this.controlador.obtenerResumen(session);
//
//        // Validacion
//        String vistaEsperada = "arma-tu-pc/resumen";
//
//        ComponenteDto procesadorEsperado = new ComponenteDto(1L, "Procesador", "Procesador1", 1000D);
//        ComponenteDto motherboardEsperada = new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D);
//        ComponenteDto coolerEsperado = new ComponenteDto(3L, "Cooler", "Cooler3", 3000D);
//        List<ComponenteDto> memoriasEsperadas = Arrays.asList(
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D),
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D));
//        ComponenteDto gpuEsperada = new ComponenteDto(2L, "Gpu", "Gpu2", 2000D);
//        List<ComponenteDto> almacenamientoEsperado = Arrays.asList(
//                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D),
//                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D),
//                new ComponenteDto(3L, "Almacenamiento", "Almacenamiento3", 3000D));
//        ComponenteDto fuenteEsperada = new ComponenteDto(1L, "Fuente", "Fuente1", 1000D);
//        ComponenteDto gabineteEsperado = new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D);
//        ComponenteDto monitorEsperado = new ComponenteDto(3L, "Monitor", "Monitor3", 3000D);
//        List<ComponenteDto> perifericosEsperados = Arrays.asList(
//                new ComponenteDto(1L, "Periferico", "Periferico1", 1000D),
//                new ComponenteDto(2L, "Periferico", "Periferico2", 2000D));
//
//
//        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getProcesador(), procesadorEsperado);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMotherboard(), motherboardEsperada);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getCooler(), coolerEsperado);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), memoriasEsperadas);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGpu(), gpuEsperada);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), almacenamientoEsperado);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getFuente(), fuenteEsperada);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGabinete(), gabineteEsperado);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMonitor(), monitorEsperado);
//        assertEquals(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), perifericosEsperados);
//
//    }
//
//    @Test
//    public void dadoQueExisteUnControladorArmaTuPcCuandoQuieroObtenerElResumenDeUnArmadoSinProcesadorMotherboardCoolerOGabineteObtengoLaVistaDeResumenConUnMensajeDeError(){
//        // Preparacion
//
//        this.controlador.seleccionarMemoria(1L,2, session);
//        this.controlador.seleccionarGpu(2L, session);
//        this.controlador.seleccionarAlmacenamiento(3L, 3, session);
//        this.controlador.seleccionarFuente(1L, session);
//        this.controlador.seleccionarMonitor(3L, session);
//        this.controlador.seleccionarPeriferico(1L, session);
//        this.controlador.seleccionarPeriferico(2L, session);
//
//
//        // Ejecucion
//        ModelAndView modelAndView = this.controlador.obtenerResumen(session);
//
//        // Validacion
//        String vistaEsperada = "arma-tu-pc/resumen";
//        String errorEsperado = "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado";
//
//        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
//        assertThat(modelAndView.getModel().get("error"), equalTo(errorEsperado));
//    }
}
