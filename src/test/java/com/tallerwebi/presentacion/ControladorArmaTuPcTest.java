package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArmaTuPc;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.dom4j.rule.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ControladorArmaTuPcTest {

    ControladorArmaTuPc controlador;
    HttpSession session;
    ServicioArmaTuPc servicioMock;


    @BeforeEach
    public void init() {
        this.servicioMock = mock(ServicioArmaTuPc.class);
        this.controlador = new ControladorArmaTuPc(this.servicioMock);
        this.session = new MockHttpSession();

    }


    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarComponentesDeTipoProcesadorObtengoUnaListaDeComponenteDtoDeTipoProcesadorYUnArmadoPcDtoParaCargar(){
        // Preparacion(hecha en el init)

        List<ComponenteDto> listaProcesadores = Arrays.asList(
                new ComponenteDto(1L,"Procesador","Procesador1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Procesador","Procesador2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Procesador","Procesador3", 3000D, "imagen.jpg", 5)
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
    public void cuandoAgregoUnComponenteAlArmadoObtengoUnBannerDiciendomeQueElComponenteFueAgregadoExitosamente() throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        // Preparacion
        ComponenteDto componente = new ComponenteDto();
        componente.setId(1L);
        componente.setTipoComponente("Procesador");
        componente.setModelo("Procesador1");

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(mock(ArmadoPcDto.class));

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(componente);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("procesador", 1L, 1, session);
        String vistaObtenida = modelAndView.getViewName();

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/motherboard";
        String agregadoEsperado = "x1 Procesador1 agregado correctamente al armado!";

        assertThat(vistaEsperada, equalTo(vistaObtenida));
        assertThat(modelAndView.getModel().get("agregado"), equalTo(agregadoEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoAgregoUnProcesadorAlArmadoEntoncesEsteSeGuardaEnElArmadoPcDtoDeLaSesionYRedirigeALaVistaDeMotherboards() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("procesador", armadoPcDtoARetornar)).thenReturn(false);


        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("procesador", 1L, 1,session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/motherboard";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getProcesador(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMotherboardsEntoncesObtengoUnaListaDeComponenteDtoDeTipoMotherboardYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMotherboards = Arrays.asList(
                new ComponenteDto(1L,"Motherboard","Motherboard1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Motherboard","Motherboard2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Motherboard","Motherboard3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setMotherboard(new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D, "imagen.jpg", 5));


        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("motherboard", armadoPcDtoARetornar)).thenReturn(false);
        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("motherboard", 2L, 1,session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/cooler";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getMotherboard(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarCoolersEntoncesObtengoUnaListaDeComponenteDtoDeTipoCoolerYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaCoolers = Arrays.asList(
                new ComponenteDto(1L,"Cooler","Cooler1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Cooler","Cooler2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Cooler","Cooler3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setCooler(new ComponenteDto(3L, "Cooler", "Cooler3", 3000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("cooler", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("cooler", 3L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/memoria";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getCooler(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMemoriasEntoncesObtengoUnaListaDeComponenteDtoDeTipoMemoriaYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMemorias = Arrays.asList(
                new ComponenteDto(1L,"Memoria","Memoria1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Memoria","Memoria2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Memoria","Memoria3", 3000D, "imagen.jpg", 5)
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
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D, "imagen.jpg", 5),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D, "imagen.jpg", 5),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D, "imagen.jpg", 5),
                        new ComponenteDto(3L, "Memoria", "Memoria3", 3000D, "imagen.jpg", 5)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 3L, 4, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/gpu";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getRams(), hasSize(4));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar1UnidadDeUnaMemoriaEntoncesEstaSeGuardaEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDeMemoriasParaCargarMas() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setRams(Arrays.asList(new ComponenteDto(3L, "Memoria", "Memoria3", 3000D, "imagen.jpg", 5)));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 3L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/memoria";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)this.session.getAttribute("armadoPcDto")).getRams(), hasSize(1));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcConUnArmadoCon3UnidadesDeMemoria1CuandoPidoCargar2UnidadesMasDeMemoria2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeMemoriasConUnErrorDeLimite() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setRams(Arrays
                .asList(
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                        new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar)
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        when(servicioMock.sePuedeAgregarMasUnidades("memoria", armadoPcDtoARetornar)).thenReturn(true);

        when(servicioMock.obtenerComponenteDtoPorId(any()))
                .thenReturn(mock(ComponenteDto.class));

        this.controlador.agregarComponenteAlArmado("memoria", 1L, 3, session);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("memoria", 2L, 2, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/memoria";
        String errorEsperado = "Supero el limite de memoria de su armado";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getRams(), hasSize(3));
        assertNotNull(modelAndView.getModel().get("errorLimite"));
        assertThat(modelAndView.getModel().get("errorLimite"), equalTo(errorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarGpusEntoncesObtengoUnaListaDeComponenteDtoDeTipoGpuYUnArmadoPcDtoParaCargar() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        List<ComponenteDto> listaGpus = Arrays.asList(
                new ComponenteDto(1L,"Gpu","Gpu1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Gpu","Gpu2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Gpu","Gpu3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setGpu(new ComponenteDto(1L, "Gpu", "Gpu1", 1000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("gpu", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("gpu", 1L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/almacenamiento";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGpu(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarAlmacenamientosEntoncesObtengoUnaListaDeComponenteDtoDeTipoAlmacenamientoYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaAlmacenamientos = Arrays.asList(
                new ComponenteDto(1L,"Almacenamiento","Almacenamiento1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Almacenamiento","Almacenamiento2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Almacenamiento","Almacenamiento3", 3000D, "imagen.jpg", 5)
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
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                        new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 6, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/fuente";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), hasSize(6));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargar4UnidadesDeUnAlmacenamientoEntoncesEstasSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoNuevamenteLaVistaDeAlmacenamientosParaCargarMas() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion

        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setAlmacenamiento(Arrays.asList(
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(2L, "Almacenamiento", "Almacenamiento2", 2000D, "imagen.jpg", 5)
                ));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 4, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/almacenamiento";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), hasSize(4));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCon4UnidadesDeAlmacenamiento1CuandoPidoCargar3UnidadesDeAlmacenamiento2EntoncesEstasUltimasNoSeGuardanEnElArmadoPcDtoDeLaSesionYObtengoLaVistaDeAlmacenamientoConUnErrorDeLimite() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparacion
        ArmadoPcDto armadoPcDtoARetornar = new ArmadoPcDto();
        armadoPcDtoARetornar.setAlmacenamiento(Arrays
                .asList(
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5),
                        new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5)
                )
        );

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar)
                .thenThrow(new LimiteDeComponenteSobrepasadoEnElArmadoException());

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDtoARetornar)).thenReturn(true);

        this.controlador.agregarComponenteAlArmado("almacenamiento", 1L, 4, session);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("almacenamiento", 2L, 2, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/almacenamiento";
        String errorEsperado = "Supero el limite de almacenamiento de su armado";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getAlmacenamiento(), hasSize(4));
        assertNotNull(modelAndView.getModel().get("errorLimite"));
        assertThat(modelAndView.getModel().get("errorLimite"), equalTo(errorEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarFuentesEntoncesObtengoUnaListaDeComponenteDtoDeTipoFuenteYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaFuentes = Arrays.asList(
                new ComponenteDto(1L,"Fuente","Fuente1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Fuente","Fuente2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Fuente","Fuente3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setFuente(new ComponenteDto(1L, "Fuente", "Fuente1", 1000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("fuente", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("fuente", 1L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/gabinete";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getFuente(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarGabinetesEntoncesObtengoUnaLaListaDeComponenteDtoDeTipoGabineteYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaGabinetes = Arrays.asList(
                new ComponenteDto(1L,"Gabinete","Gabinete1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Gabinete","Gabinete2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Gabinete","Gabinete3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setGabinete(new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("gabinete", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("gabinete", 2L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/monitor";


        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getGabinete(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoPidoCargarMonitoresEntoncesObtengoUnaLaListaDeComponenteDtoDeTipoMonitorYUnArmadoPcDtoParaCargar(){
        // Preparacion

        List<ComponenteDto> listaMonitores = Arrays.asList(
                new ComponenteDto(1L,"Monitor","Monitor1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Monitor","Monitor2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Monitor","Monitor3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setMonitor(new ComponenteDto(3L, "Monitor", "Monitor3", 3000D, "imagen.jpg", 5));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("monitor", armadoPcDtoARetornar)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("monitor", 3L, 1, session);


        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/periferico";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getMonitor(), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoCargoPerifericosEntoncesObtengoLaListaDePerifericosYElArmadoPcDtoDeLaSesion(){
        // Preparacion

        List<ComponenteDto> listaPerifericos = Arrays.asList(
                new ComponenteDto(1L,"Periferico","Periferico1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Periferico","Periferico2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Periferico","Periferico3", 3000D, "imagen.jpg", 5)
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
        armadoPcDtoARetornar.setPerifericos(Arrays.asList(new ComponenteDto(1L, "Periferico", "Periferico1", 1000D, "imagen.jpg", 5)));

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoPcDtoARetornar);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades("periferico", armadoPcDtoARetornar)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("periferico", 1L, 1, session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/periferico";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), hasSize(1));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoYaSeSelecciono10PerifericoEntoncesEnElArmadoPcDtoDeLaSesionObtengoLaVistaDeResumen() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        List<ComponenteDto> perifericos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            perifericos.add(new ComponenteDto((long) i, "Periferico", "Periferico" + i, i*1000D, "imagen.jpg", 5));
        }

        ArmadoPcDto armadoCon10Perifericos = new ArmadoPcDto();
        armadoCon10Perifericos.setPerifericos(perifericos);

        when(servicioMock.agregarComponenteAlArmado(any(), any(), any(), any()))
                .thenReturn(armadoCon10Perifericos);

        when(servicioMock.obtenerComponenteDtoPorId(any())).thenReturn(mock(ComponenteDto.class));

        when(servicioMock.sePuedeAgregarMasUnidades(eq("periferico"), eq(armadoCon10Perifericos)))
                .thenReturn(false);

        // Ejecución
        ModelAndView modelAndView = this.controlador.agregarComponenteAlArmado("periferico", 1L, 1, session);
        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/resumen";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(((ArmadoPcDto)session.getAttribute("armadoPcDto")).getPerifericos(), hasSize(10));
    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoObtengoElResumenObtengoLaVistaDeResumenConElArmadoPcDtoDeLaSession(){
        // Preparacion

        ArmadoPcDto armadoTerminado = mock(ArmadoPcDto.class);

        session.setAttribute("armadoPcDto", armadoTerminado);
        when(this.servicioMock.armadoCompleto(armadoTerminado)).thenReturn(true);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.obtenerResumen(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/resumen";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(modelAndView.getModel().get("armadoPcDto"), instanceOf(ArmadoPcDto.class));
        verify(servicioMock, times(1)).armadoCompleto(any());

    }

    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoQuieroObtenerElResumenDeUnArmadoSinProcesadorMotherboardCoolerOGabineteObtengoLaVistaDeResumenConUnMensajeDeError(){
        // Preparacion

        ArmadoPcDto armadoIncompleto = mock(ArmadoPcDto.class);

        session.setAttribute("armadoPcDto", armadoIncompleto);
        when(this.servicioMock.armadoCompleto(armadoIncompleto)).thenReturn(false);

        // Ejecucion
        ModelAndView modelAndView = this.controlador.obtenerResumen(session);

        // Validacion
        String vistaEsperada = "arma-tu-pc/tradicional/resumen";
        String errorEsperado = "Seleccione almenos un motherboard, cpu, cooler y gabinete para obtener su armado";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(modelAndView.getModel().get("armadoPcDto"), nullValue());
        assertThat(modelAndView.getModel().get("errorResumen"), equalTo(errorEsperado));
        verify(servicioMock, times(1)).armadoCompleto(any());
    }


    @Test
    public void dadoQueExisteUnControladorArmaTuPcCuandoQuieroReiniciarElArmadoPcDtoAnteriorEntoncesElArmadoDeLaSesionSeBorraYMeDevuelveLaVistaDeProcesadores(){
        // Preparacion

        ArmadoPcDto armado = mock(ArmadoPcDto.class);

        session.setAttribute("armadoPcDto", armado);

        // Ejecucion

        ModelAndView modelAndView = this.controlador.reiniciarArmado(session);

        // Validacion
        String vistaEsperada = "redirect:/arma-tu-pc/tradicional/procesador";
        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(session.getAttribute("armadoPcDto"), nullValue());
    }

}
