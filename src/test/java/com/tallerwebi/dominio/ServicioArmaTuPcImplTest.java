package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Procesador;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.dominio.excepcion.QuitarComponenteInvalidoException;
import com.tallerwebi.dominio.excepcion.QuitarStockDemasDeComponenteException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServicioArmaTuPcImplTest {

    private ServicioArmaTuPc servicio;
    private RepositorioComponente repositorioComponenteMock;

    @BeforeEach
    public void init() {
        this.repositorioComponenteMock = mock(RepositorioComponente.class);
        this.servicio = new ServicioArmaTuPcImpl(repositorioComponenteMock);
    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoObtenerListaDeComponentesDtoPorUnTipoObtengoLosComponentesDeEsteTipo() {

        // Preparacion

        List<Componente> listaARetornar = new ArrayList<Componente>();
        listaARetornar.add(new Procesador());
        listaARetornar.add(new Procesador());
        listaARetornar.add(new Procesador());

        when(repositorioComponenteMock.obtenerComponentesPorTipo(anyString()))
                .thenReturn(listaARetornar);

        // Ejecucion

        List<ComponenteDto> componentesObtenidos =  this.servicio.obtenerListaDeComponentesDto(Procesador.class.getSimpleName());

        // Validacion

        assertThat(componentesObtenidos, everyItem(hasProperty("tipoComponente", equalTo(Procesador.class.getSimpleName()))));
    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoLePidoUnComponentePorIdObtengoEseComponente(){
        Procesador procesadorBuscado = new Procesador();
        procesadorBuscado.setId(1L);
        //Preparacion
        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong()))
                .thenReturn(procesadorBuscado);
        //Ejecucion
        Componente componenteObtenido = this.servicio.obtenerComponentePorId(1L);

        //Validacion
        Componente componenteEsperado = new Procesador();
        componenteEsperado.setId(1L);

        assertEquals(componenteObtenido, componenteEsperado);
    };

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoAgregarUnComponenteDtoAUnArmadoEntoncesObtengoUnArmadoDtoConElComponenteDtoCargado() throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        //Preparacion

        ArmadoPcDto armadoPcDtoACargarComponente = new ArmadoPcDto();
        Procesador procesadorACargar = new Procesador();
        procesadorACargar.setId(1L);

        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong()))
                .thenReturn(procesadorACargar);

        //Ejecucion

        ArmadoPcDto armadoObtenido = this.servicio.agregarComponenteAlArmado(1L, "Procesador", 1, armadoPcDtoACargarComponente);

        //Validacion

        ArmadoPcDto armadoEsperado = new ArmadoPcDto();
        armadoEsperado.setProcesador(new ComponenteDto(1L,"Procesador","Procesador1", 1000D, "imagen.jpg", 5));

        assertEquals(armadoObtenido.getProcesador(), armadoPcDtoACargarComponente.getProcesador());

    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoAgregarUnComponenteDtoSuperandoElMaximoPermitidoEnElArmadoEntoncesObtengoUnaException() throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        //Preparacion

        ArmadoPcDto armadoPcDtoACargarComponente = new ArmadoPcDto();
        armadoPcDtoACargarComponente.setRams(Arrays.asList(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                                                            new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                                                            new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)
                                                        )
        );

        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(any());

        //Ejecucion

        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
            this.servicio.agregarComponenteAlArmado(1L, "memoria", 2, armadoPcDtoACargarComponente);
        });

        //Validacion (hecha en la ejecucion)

    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPCImplConUnArmadoConLoMinimoNecesarioCuandoPreguntoSiEstaCompletoEntoncesObtengoVerdadero(){
        // Preparacion
        ArmadoPcDto armadoPcDtoAConLoMinimo = new ArmadoPcDto();
        armadoPcDtoAConLoMinimo.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D, "imagen.jpg", 5));
        armadoPcDtoAConLoMinimo.setMotherboard(new ComponenteDto(2L, "Motherboard", "Motherboard2", 2000D, "imagen.jpg", 5));
        armadoPcDtoAConLoMinimo.setCooler(new ComponenteDto(3L, "Cooler", "Cooler3", 3000D, "imagen.jpg", 5));
        armadoPcDtoAConLoMinimo.setGabinete(new ComponenteDto(2L, "Gabinete", "Gabinete2", 2000D, "imagen.jpg", 5));

        // Ejecucion

        Boolean estaCompleto = this.servicio.armadoCompleto(armadoPcDtoAConLoMinimo);

        // Validacion
        assertTrue(estaCompleto);
    };

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplConUnArmadoDtoNuevoCuandoPreguntoSiSePuedeAgregarMasMemoriasObtengoUnVerdadero(){
        // Preparacion

        ArmadoPcDto armadoPcDtoNuevo = new ArmadoPcDto();

        // Ejecucion

        Boolean sePuedeAgregar = this.servicio.sePuedeAgregarMasUnidades("memoria", armadoPcDtoNuevo);

        // Validacion

        assertTrue(sePuedeAgregar);
    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplConUnArmadoDtoCon4MemoriasCuandoPreguntoSiSePuedeAgregarMasMemoriasObtengoUnFalso(){
        // Preparacion

        ArmadoPcDto armadoPcDtoNuevo = new ArmadoPcDto();
        armadoPcDtoNuevo.setRams(Arrays.asList(
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)
                )
        );

        // Ejecucion

        Boolean sePuedeAgregar = this.servicio.sePuedeAgregarMasUnidades("memoria", armadoPcDtoNuevo);

        // Validacion

        assertFalse(sePuedeAgregar);
    }

    // test de quitar componente

    @Test
    public void cuandoQuitoUnComponenteProcesadorDelArmadoDtoConUnProcesadorObtengoElArmadoSinElProcesador() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {

        // Preparacion

        ArmadoPcDto armadoConProcesador = new ArmadoPcDto();
        armadoConProcesador.setProcesador(new ComponenteDto(1L, "Procesador", "Procesador1", 1000D, "imagen.jpg", 5));

        // Ejecucion

        ArmadoPcDto armadoObtenido = this.servicio.quitarComponenteAlArmado(1L, "Procesador", 1, armadoConProcesador);

        // Validacion

        assertThat(armadoObtenido.getProcesador(), is(nullValue()));
    }

    @Test
    public void cuandoQuitoUnaUnidadDeComponenteMemoriaConId1DelArmadoDtoConDosMemoriasDiferentesObtengoElArmadoCon1MemoriaYEsaTieneElId2() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {

        // Preparacion

        ArmadoPcDto armadoConMemorias = new ArmadoPcDto();
        List<ComponenteDto> listaDeMemorias = new ArrayList<>(Arrays.asList(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L, "Memoria", "Memoria2", 2000D, "imagen.jpg", 5)));
        armadoConMemorias.setRams(listaDeMemorias);

        // Ejecucion

        ArmadoPcDto armadoObtenido = this.servicio.quitarComponenteAlArmado(1L, "Memoria", 1, armadoConMemorias);

        // Validacion

        assertThat(armadoObtenido.getRams(), hasSize(1));
        assertTrue(armadoObtenido.getRams().contains(new ComponenteDto(2L, "Memoria", "Memoria2", 2000D, "imagen.jpg", 5)));
        assertFalse(armadoObtenido.getRams().contains(new ComponenteDto(1L, "Memoria", "Memoria1", 1000D, "imagen.jpg", 5)));
    }

    @Test
    public void cuandoQuito3UnidadesDeComponenteAlmacenamientoConId1DelArmadoDtoCon2AlmacenamientosDeId1ObtengoUnQuitarComponenteInvalidoException(){

        // Preparacion

        ArmadoPcDto armadoConAlmacenamientos = new ArmadoPcDto();
        List<ComponenteDto> listaDeAlmacenamientos = new ArrayList<>(Arrays.asList(new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(1L, "Almacenamiento", "Almacenamiento1", 2000D, "imagen.jpg", 5)));
        armadoConAlmacenamientos.setRams(listaDeAlmacenamientos);

        // Ejecucion

        assertThrows(QuitarStockDemasDeComponenteException.class, () -> {
            this.servicio.quitarComponenteAlArmado(1L, "Almacenamiento", 3, armadoConAlmacenamientos);
        });

        // Validacion (hecha en la ejecucion)
    }

    @Test
    public void cuandoQuitoUnProcesadorQueNoTengoEnElArmadoObtengoUnQuitarComponenteInvalidoException(){

        // Preparacion

        ArmadoPcDto armadoSinProcesador = new ArmadoPcDto();

        // Ejecucion

        assertThrows(QuitarComponenteInvalidoException.class, () -> {
            this.servicio.quitarComponenteAlArmado(1L, "Procesador", 1, armadoSinProcesador);
        });

        // Validacion (hecha en la ejecucion)
    }

}
