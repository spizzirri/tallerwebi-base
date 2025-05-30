package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ArmadoPc;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.excepcion.LimiteDeComponenteSobrepasadoEnElArmadoException;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServicioArmaTuPcImplTest {

    private ServicioArmaTuPc servicio;
    private RepositorioComponente repositorioComponenteMock;
    // private RepositorioArmado repositorioArmadoMock // hace falta?

    @BeforeEach
    public void init() {
        this.repositorioComponenteMock = mock(RepositorioComponente.class);
        this.servicio = new ServicioArmaTuPcImpl(repositorioComponenteMock);
    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoObtenerListaDeComponentesDtoPorUnTipoObtengoLosComponentesDeEsteTipo() {

        // Preparacion

        when(repositorioComponenteMock.obtenerComponentesPorTipo(anyString()))
                .thenReturn(Arrays.asList(
                new Componente(1L,"Procesador1", 1000D, 5),
                new Componente(2L,"Procesador2", 2000D, 5),
                new Componente(3L,"Procesador3", 3000D, 5),
                new Componente(4L,"Procesador4", 4000D, 5),
                new Componente(5L,"Procesador5", 5000D, 5),
                new Componente(7L,"Procesador6", 6000D, 5)
            )
           );

        // Ejecucion

        List<ComponenteDto> componentesObtenidos =  this.servicio.obtenerListaDeComponentesDto("procesador");

        // Validacion

        List<ComponenteDto> listaEsperada = Arrays.asList(
                new ComponenteDto(1L,"Procesador","Procesador1", 1000D, "imagen.jpg", 5),
                new ComponenteDto(2L,"Procesador","Procesador2", 2000D, "imagen.jpg", 5),
                new ComponenteDto(3L,"Procesador","Procesador3", 3000D, "imagen.jpg", 5),
                new ComponenteDto(4L,"Procesador","Procesador4", 4000D, "imagen.jpg", 5),
                new ComponenteDto(5L,"Procesador","Procesador5", 5000D, "imagen.jpg", 5),
                new ComponenteDto(7L,"Procesador","Procesador6", 6000D, "imagen.jpg", 5)
        );

        assertEquals(componentesObtenidos, listaEsperada);

    }

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoLePidoUnComponentePorIdObtengoEseComponente(){

        //Preparacion
        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(
                new Componente(1L,"Procesador1", 1000D,5)
        );
        //Ejecucion
        Componente componenteObtenido = this.servicio.obtenerComponentePorId(1L);

        //Validacion
        Componente componenteEsperado = new Componente(1L,"Procesador1", 1000D, 5);

        assertEquals(componenteObtenido, componenteEsperado);
    };

    @Test
    public void dadoQueExisteUnServicioArmaTuPcImplCuandoPidoAgregarUnComponenteDtoAUnArmadoEntoncesObtengoUnArmadoDtoConElComponenteDtoCargado() throws LimiteDeComponenteSobrepasadoEnElArmadoException {

        //Preparacion

        ArmadoPcDto armadoPcDtoACargarComponente = new ArmadoPcDto();

        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(new Componente(1L,"Procesador1", 1000D, 5));

        //Ejecucion

        ArmadoPcDto armadoObtenido = this.servicio.agregarComponenteAlArmado(1L, "procesador", 1, armadoPcDtoACargarComponente);

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

        when(this.repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(new Componente(1L,"Memoria1", 1000D, 5));

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

}
