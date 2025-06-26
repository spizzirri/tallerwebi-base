package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Procesador;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioArmaTuPcImplTest {

    private RepositorioComponente repositorioComponenteMock;
    private ServicioCompatibilidades servicioCompatibilidadesMock;
    private ServicioArmaTuPcImpl servicioArmaTuPc;

    @BeforeEach
    public void init() {
        repositorioComponenteMock = mock(RepositorioComponente.class);
        servicioCompatibilidadesMock = mock(ServicioCompatibilidades.class);
        servicioArmaTuPc = new ServicioArmaTuPcImpl(repositorioComponenteMock, servicioCompatibilidadesMock);
    }

    // Método helper para crear componentes de prueba
    private Componente crearComponente(Long id, String nombre, String tipo) {
        Componente componente = new Procesador();
        componente.setId(id);
        componente.setNombre(nombre);
        return componente;
    }

    // Tests para obtenerListaDeComponentesCompatiblesDto
    @Test
    public void cuandoPidoComponentesCompatiblesYTodosLoSonDevuelveListaCompletaDeDtos() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        List<Componente> componentesDelRepo = List.of(
                crearComponente(1L, "Mother A", "Motherboard"),
                crearComponente(2L, "Mother B", "Motherboard")
        );

        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard")).thenReturn(componentesDelRepo);
        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(Componente.class), any())).thenReturn(true);

        // Ejecución
        List<ComponenteDto> resultado = servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);

        // Verificación
        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getId(), equalTo(1L));
        assertThat(resultado.get(1).getId(), equalTo(2L));
        verify(repositorioComponenteMock, times(1)).obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard");
        verify(servicioCompatibilidadesMock, times(2)).esCompatibleConElArmado(any(Componente.class), any());
    }

    @Test
    public void cuandoPidoComponentesPeroNingunoEsCompatibleDevuelveListaVacia() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        List<Componente> componentesDelRepo = List.of(crearComponente(1L, "Mother A", "Motherboard"));

        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard")).thenReturn(componentesDelRepo);
        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(Componente.class), any())).thenReturn(false);

        // Ejecución
        List<ComponenteDto> resultado = servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);

        // Verificación
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void cuandoPidoComponentesPeroFaltaUnoDeterminanteLanzaExcepcion() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        List<Componente> componentesDelRepo = List.of(crearComponente(1L, "Mother A", "Motherboard"));

        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard")).thenReturn(componentesDelRepo);
        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(Componente.class), any()))
                .thenThrow(new ComponenteDeterminateDelArmadoEnNullException("Se necesita un procesador"));

        // Ejecución y Verificación
        assertThrows(ComponenteDeterminateDelArmadoEnNullException.class, () -> {
            servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);
        });
    }

    // Tests para obtenerListaDeComponentesCompatiblesFiltradosDto
    @Test
    public void cuandoPidoComponentesFiltradosYCompatiblesDevuelveLaListaCorrecta() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "procesador";
        String filtro = "Intel";
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        List<Componente> componentesFiltradosRepo = List.of(crearComponente(1L, "Intel i5", "Procesador"));

        when(repositorioComponenteMock.obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio("Procesador", filtro))
                .thenReturn(componentesFiltradosRepo);
        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(Componente.class), any())).thenReturn(true);

        // Ejecución
        List<ComponenteDto> resultado = servicioArmaTuPc.obtenerListaDeComponentesCompatiblesFiltradosDto(tipoComponente, filtro, armadoPcDto);

        // Verificación
        assertThat(resultado, hasSize(1));
        assertThat(resultado.get(0).getModelo(), equalTo("Intel i5"));
        verify(repositorioComponenteMock, times(1))
                .obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio("Procesador", filtro);
    }

    // Tests para agregarComponenteAlArmado
    @Test
    public void cuandoAgregoUnProcesadorSeReiniciaElArmadoPeroSeConservanPerifericosYMonitor() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        ArmadoPcDto armadoPcDtoInicial = new ArmadoPcDto();
        armadoPcDtoInicial.setMotherboard(new ComponenteDto(crearComponente(10L, "Mother Vieja", "Motherboard")));
        armadoPcDtoInicial.setMonitor(new ComponenteDto(crearComponente(11L, "Monitor A", "Monitor")));

        Componente nuevoProcesador = crearComponente(1L, "Intel i9", "Procesador");
        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(nuevoProcesador);

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(1L, "procesador", 1, armadoPcDtoInicial);

        // Verificación
        assertNotNull(armadoResultante.getProcesador());
        assertEquals(1L, armadoResultante.getProcesador().getId());
        assertNull(armadoResultante.getMotherboard()); // La motherboard vieja debe desaparecer
        assertNotNull(armadoResultante.getMonitor()); // El monitor debe conservarse
        assertEquals(11L, armadoResultante.getMonitor().getId());
    }

    @Test
    public void cuandoAgregoUnaMemoriaRAMSeAgregaALaLista() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setProcesador(new ComponenteDto(crearComponente(1L, "Intel i5", "Procesador")));

        Componente nuevaRam = crearComponente(20L, "Corsair Vengeance", "MemoriaRAM");
        when(repositorioComponenteMock.obtenerComponentePorId(20L)).thenReturn(nuevaRam);

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(20L, "memoria", 2, armadoPcDto);

        // Verificación
        assertThat(armadoResultante.getRams(), hasSize(2));
        assertEquals(20L, armadoResultante.getRams().get(0).getId());
    }

    @Test
    public void cuandoIntentoAgregarUnaQuintaMemoriaRAMLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        List<ComponenteDto> rams = List.of(
                new ComponenteDto(crearComponente(20L, "RAM 1", "MemoriaRAM")),
                new ComponenteDto(crearComponente(21L, "RAM 2", "MemoriaRAM")),
                new ComponenteDto(crearComponente(22L, "RAM 3", "MemoriaRAM")),
                new ComponenteDto(crearComponente(23L, "RAM 4", "MemoriaRAM"))
        );
        armadoPcDto.setRams(rams);

        when(repositorioComponenteMock.obtenerComponentePorId(24L)).thenReturn(crearComponente(24L, "RAM 5", "MemoriaRAM"));

        // Ejecución y Verificación
        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
            servicioArmaTuPc.agregarComponenteAlArmado(24L, "memoria", 1, armadoPcDto);
        });
    }

    // Tests para quitarComponenteAlArmado
    @Test
    public void cuandoQuitoUnComponenteExitosamenteEsteDesapareceDelArmado() throws Exception {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setGpu(new ComponenteDto(crearComponente(30L, "RTX 3080", "PlacaDeVideo")));

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(30L, "gpu", 1, armadoPcDto);

        // Verificación
        assertNull(armadoResultante.getGpu());
    }

    @Test
    public void cuandoQuitoUnCoolerSeAnulanTambienLaFuenteYElGabinete() throws Exception {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setCooler(new ComponenteDto(crearComponente(40L, "Cooler Master", "CoolerCPU")));
        armadoPcDto.setFuente(new ComponenteDto(crearComponente(41L, "Fuente 850W", "FuenteDeAlimentacion")));
        armadoPcDto.setGabinete(new ComponenteDto(crearComponente(42L, "Gabinete ATX", "Gabinete")));

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(40L, "cooler", 1, armadoPcDto);

        // Verificación
        assertNull(armadoResultante.getCooler());
        assertNull(armadoResultante.getFuente());
        assertNull(armadoResultante.getGabinete());
    }

    @Test
    public void cuandoIntentoQuitarUnComponenteQueNoEstaEnElArmadoLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto(); // Armado vacío

        // Ejecución y Verificación
        assertThrows(QuitarComponenteInvalidoException.class, () -> {
            servicioArmaTuPc.quitarComponenteAlArmado(99L, "procesador", 1, armadoPcDto);
        });
    }

    @Test
    public void cuandoIntentoQuitarMasAlmacenamientoDelExistenteLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearComponente(50L, "SSD 1TB", "Almacenamiento")));

        // Ejecución y Verificación
        assertThrows(QuitarStockDemasDeComponenteException.class, () -> {
            // Intenta quitar 2, pero solo hay 1
            servicioArmaTuPc.quitarComponenteAlArmado(50L, "almacenamiento", 2, armadoPcDto);
        });
    }

    // Tests para sePuedeAgregarMasUnidades
    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaMemoriaCuandoHayMenosDeCuatro() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.getRams().add(new ComponenteDto());
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("memoria", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaMemoriaCuandoHayCuatro() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setRams(List.of(new ComponenteDto(), new ComponenteDto(), new ComponenteDto(), new ComponenteDto()));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("memoria", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaComponentesUnicosComoProcesador() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("procesador", armadoPcDto));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("gpu", armadoPcDto));
    }

    // Tests para armadoCompleto
    @Test
    public void armadoCompletoDevuelveTrueCuandoTieneLosComponentesEsenciales() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setProcesador(new ComponenteDto());
        armadoPcDto.setMotherboard(new ComponenteDto());
        armadoPcDto.setCooler(new ComponenteDto());
        armadoPcDto.setGabinete(new ComponenteDto());

        assertTrue(servicioArmaTuPc.armadoCompleto(armadoPcDto));
    }

    @Test
    public void armadoCompletoDevuelveFalseCuandoFaltaAlMenosUnComponenteEsencial() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setProcesador(new ComponenteDto());
        armadoPcDto.setMotherboard(new ComponenteDto());
        armadoPcDto.setCooler(new ComponenteDto());
        // Falta el gabinete

        assertFalse(servicioArmaTuPc.armadoCompleto(armadoPcDto));
    }


}


//package com.tallerwebi.dominio;
//
//import com.tallerwebi.dominio.entidades.ArmadoPc;
//import com.tallerwebi.dominio.entidades.Componente;
//import com.tallerwebi.dominio.entidades.Procesador;
//import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
//import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
//import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
//import com.tallerwebi.presentacion.dto.ArmadoPcDto;
//import com.tallerwebi.presentacion.dto.ComponenteDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//public class ServicioArmaTuPcImplTest {
//
//    private ServicioArmaTuPc servicio;
//    private RepositorioComponente repositorioComponenteMock;
//
////    @BeforeEach
////    public void init() {
////        this.repositorioComponenteMock = mock(RepositorioComponente.class);
////        this.servicio = new ServicioArmaTuPcImpl(repositorioComponenteMock);
////    }
//
////    @Test
////    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoObtenerListaDeComponentesDtoPorUnTipoObtengoLosComponentesDeEsteTipo() {
////
////        // Preparacion
////
////        List<Componente> listaARetornar = new ArrayList<Componente>();
////        listaARetornar.add(new Procesador());
////        listaARetornar.add(new Procesador());
////        listaARetornar.add(new Procesador());
////
////        when(repositorioComponenteMock.obtenerComponentesPorTipo(anyString()))
////                .thenReturn(listaARetornar);
////
////        // Ejecucion
////
////        List<ComponenteDto> componentesObtenidos =  this.servicio.obtenerListaDeComponentesDto(Procesador.class.getSimpleName());
////
////        // Validacion
////
////        assertThat(componentesObtenidos, everyItem(hasProperty("tipoComponente", equalTo(Procesador.class.getSimpleName()))));
////    }
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPcImplCuandoLePidoUnComponentePorIdObtengoEseComponente(){
//        Procesador procesadorBuscado = new Procesador();
//        procesadorBuscado.setId(1L);
//        //Preparacion
//        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong()))
//                .thenReturn(procesadorBuscado);
//        //Ejecucion
//        Componente componenteObtenido = this.servicio.obtenerComponentePorId(1L);
//
//        //Validacion
//        Componente componenteEsperado = new Procesador();
//        componenteEsperado.setId(1L);
//
//        assertEquals(componenteObtenido, componenteEsperado);
//    };
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoAgregarUnComponenteDtoAUnArmadoEntoncesObtengoUnArmadoDtoConElComponenteDtoCargado() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
//
//        //Preparacion
//
//        ArmadoPcDto armadoPcDtoACargarComponente = new ArmadoPcDto();
//        Procesador procesadorACargar = new Procesador();
//        procesadorACargar.setId(1L);
//
//        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong()))
//                .thenReturn(procesadorACargar);
//
//        //Ejecucion
//
//        ArmadoPcDto armadoObtenido = this.servicio.agregarComponenteAlArmado(1L, "Procesador", 1, armadoPcDtoACargarComponente);
//
//        //Validacion
//
//        ArmadoPcDto armadoEsperado = new ArmadoPcDto();
//        armadoEsperado.setProcesador(new ComponenteDto(1L,"Procesador","Procesador1", 1000D, "imagen.jpg", 5));
//
//        assertEquals(armadoObtenido.getProcesador(), armadoPcDtoACargarComponente.getProcesador());
//
//    }
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoAgregarUnComponenteDtoSuperandoElMaximoPermitidoEnElArmadoEntoncesObtengoUnaException() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
//
//        //Preparacion
//
//        ArmadoPcDto armadoPcDtoACargarComponente = new ArmadoPcDto();
//        armadoPcDtoACargarComponente.setRams(Arrays.asList(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                                                            new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                                                            new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)
//                                                        )
//        );
//
//        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(any());
//
//        //Ejecucion
//
//        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
//            this.servicio.agregarComponenteAlArmado(1L, "memoria", 2, armadoPcDtoACargarComponente);
//        });
//
//        //Validacion (hecha en la ejecucion)
//
//    }
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPCImplConUnArmadoConLoMinimoNecesarioCuandoPreguntoSiEstaCompletoEntoncesObtengoVerdadero(){
//        // Preparacion
//        ArmadoPcDto armadoPcDtoAConLoMinimo = new ArmadoPcDto();
//        armadoPcDtoAConLoMinimo.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D, "imagen.jpg", 5));
//        armadoPcDtoAConLoMinimo.setMotherboard(new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D, "imagen.jpg", 5));
//        armadoPcDtoAConLoMinimo.setCooler(new ComponenteDto(3L, "Cooler", "Cooler3", 3000D, "imagen.jpg", 5));
//        armadoPcDtoAConLoMinimo.setGabinete(new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D, "imagen.jpg", 5));
//
//        // Ejecucion
//
//        Boolean estaCompleto = this.servicio.armadoCompleto(armadoPcDtoAConLoMinimo);
//
//        // Validacion
//        assertTrue(estaCompleto);
//    };
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPcImplConUnArmadoDtoNuevoCuandoPreguntoSiSePuedeAgregarMasMemoriasObtengoUnVerdadero(){
//        // Preparacion
//
//        ArmadoPcDto armadoPcDtoNuevo = new ArmadoPcDto();
//
//        // Ejecucion
//
//        Boolean sePuedeAgregar = this.servicio.sePuedeAgregarMasUnidades("memoria", armadoPcDtoNuevo);
//
//        // Validacion
//
//        assertTrue(sePuedeAgregar);
//    }
//
//    @Test
//    public void dadoQueExisteUnServicioArmaTuPcImplConUnArmadoDtoCon4MemoriasCuandoPreguntoSiSePuedeAgregarMasMemoriasObtengoUnFalso(){
//        // Preparacion
//
//        ArmadoPcDto armadoPcDtoNuevo = new ArmadoPcDto();
//        armadoPcDtoNuevo.setRams(Arrays.asList(
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)
//                )
//        );
//
//        // Ejecucion
//
//        Boolean sePuedeAgregar = this.servicio.sePuedeAgregarMasUnidades("memoria", armadoPcDtoNuevo);
//
//        // Validacion
//
//        assertFalse(sePuedeAgregar);
//    }
//
//    // test de quitar componente
//
//    @Test
//    public void cuandoQuitoUnComponenteProcesadorDelArmadoDtoConUnProcesadorObtengoElArmadoSinElProcesador() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
//
//        // Preparacion
//
//        ArmadoPcDto armadoConProcesador = new ArmadoPcDto();
//        armadoConProcesador.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D, "imagen.jpg", 5));
//
//        // Ejecucion
//
//        ArmadoPcDto armadoObtenido = this.servicio.quitarComponenteAlArmado(1L, "Procesador", 1, armadoConProcesador);
//
//        // Validacion
//
//        assertThat(armadoObtenido.getProcesador(), is(nullValue()));
//    }
//
//    @Test
//    public void cuandoQuitoUnaUnidadDeComponenteMemoriaConId1DelArmadoDtoConDosMemoriasDiferentesObtengoElArmadoCon1MemoriaYEsaTieneElId2() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
//
//        // Preparacion
//
//        ArmadoPcDto armadoConMemorias = new ArmadoPcDto();
//        List<ComponenteDto> listaDeMemorias = new ArrayList<>(Arrays.asList(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
//                new ComponenteDto(2L, "Memoria", "Memoria2", 2000D, "imagen.jpg", 5)));
//        armadoConMemorias.setRams(listaDeMemorias);
//
//        // Ejecucion
//
//        ArmadoPcDto armadoObtenido = this.servicio.quitarComponenteAlArmado(1L, "Memoria", 1, armadoConMemorias);
//
//        // Validacion
//
//        assertThat(armadoObtenido.getRams(), hasSize(1));
//        assertTrue(armadoObtenido.getRams().contains(new ComponenteDto(2L, "Memoria", "Memoria2", 2000D, "imagen.jpg", 5)));
//        assertFalse(armadoObtenido.getRams().contains(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)));
//    }
//
//    @Test
//    public void cuandoQuito3UnidadesDeComponenteAlmacenamientoConId1DelArmadoDtoCon2AlmacenamientosDeId1ObtengoUnQuitarComponenteInvalidoException(){
//
//        // Preparacion
//
//        ArmadoPcDto armadoConAlmacenamientos = new ArmadoPcDto();
//        List<ComponenteDto> listaDeAlmacenamientos = new ArrayList<>(Arrays.asList(new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5),
//                new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 2000D, "imagen.jpg", 5)));
//        armadoConAlmacenamientos.setRams(listaDeAlmacenamientos);
//
//        // Ejecucion
//
//        assertThrows(QuitarStockDemasDeComponenteException.class, () -> {
//            this.servicio.quitarComponenteAlArmado(1L, "Almacenamiento", 3, armadoConAlmacenamientos);
//        });
//
//        // Validacion (hecha en la ejecucion)
//    }
//
//    @Test
//    public void cuandoQuitoUnProcesadorQueNoTengoEnElArmadoObtengoUnQuitarComponenteInvalidoException(){
//
//        // Preparacion
//
//        ArmadoPcDto armadoSinProcesador = new ArmadoPcDto();
//
//        // Ejecucion
//
//        assertThrows(QuitarComponenteInvalidoException.class, () -> {
//            this.servicio.quitarComponenteAlArmado(1L, "Procesador", 1, armadoSinProcesador);
//        });
//
//        // Validacion (hecha en la ejecucion)
//    }
//
//}
