package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.ComponenteDeterminateDelArmadoEnNullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioCompatibilidadesTest {

    private ServicioMotherboard servicioMotherboardMock;
    private ServicioCooler servicioCoolerMock;
    private ServicioPlacaDeVideo servicioPlacaDeVideoMock;
    private ServicioFuente servicioFuenteMock;
    private RepositorioComponente repositorioComponenteMock;


    private ServicioCompatibilidadesImpl servicioCompatibilidades;

    private Procesador procesador;
    private Motherboard motherboard;
    private CoolerCPU cooler;
    private MemoriaRAM ram;
    private PlacaDeVideo gpu;
    private Almacenamiento almacenamiento;
    private FuenteDeAlimentacion fuente;
    private Monitor monitor;

    @BeforeEach
    public void init() {

        servicioMotherboardMock = mock(ServicioMotherboard.class);
        servicioCoolerMock = mock(ServicioCooler.class);
        servicioPlacaDeVideoMock = mock(ServicioPlacaDeVideo.class);
        servicioFuenteMock = mock(ServicioFuente.class);

        repositorioComponenteMock = mock(RepositorioComponente.class);

        servicioCompatibilidades = new ServicioCompatibilidadesImpl(servicioMotherboardMock, servicioCoolerMock, servicioPlacaDeVideoMock, servicioFuenteMock, repositorioComponenteMock);




        // Inicialización de componentes de prueba
        procesador = crearProcesador(1L, "AMD Ryzen 5");
        motherboard = crearMotherboard(2L, "ASUS B550M");
        cooler = crearCooler(3L, "Cooler Stock");
        ram = crearRam(4L, "Kingston Fury");
        gpu = crearGpu(5L, "NVIDIA RTX 3060");
        almacenamiento = crearAlmacenamiento(6L, "SSD 1TB");
        fuente = crearFuente(7L, "Fuente 650W");
        monitor = crearMonitor(8L, "Monitor 24 pulgadas");
    }

    // Tests para Motherboard
    @Test
    public void cuandoSeVerificaUnaMotherboardSinTenerUnProcesadorLanzaExcepcion() {
        // Preparación
        ArmadoPc armadoSinProcesador = new ArmadoPc();

        // Ejecución y Verificación
        ComponenteDeterminateDelArmadoEnNullException exception = assertThrows(
                ComponenteDeterminateDelArmadoEnNullException.class,
                () -> servicioCompatibilidades.esCompatibleConElArmado(motherboard, armadoSinProcesador)
        );
        assertEquals("Debe seleccionar un Procesador para poder elegir una Motherboard.", exception.getMessage());
    }

    @Test
    public void cuandoSeVerificaUnaMotherboardCompatibleConElProcesadorDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoConProcesador = new ArmadoPc();
        armadoConProcesador.setProcesador(procesador);
        when(repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(procesador);
        when(servicioMotherboardMock.verificarCompatibilidadDeMotherboardConProcesador(motherboard, procesador)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(motherboard, armadoConProcesador);

        // Verificación
        assertTrue(esCompatible);
        verify(servicioMotherboardMock, times(1)).verificarCompatibilidadDeMotherboardConProcesador(motherboard, procesador);
    }

    // Tests para CoolerCPU
    @Test
    public void cuandoSeVerificaUnCoolerSinTenerMotherboardLanzaExcepcion() {
        // Preparación
        ArmadoPc armadoConProcesador = new ArmadoPc();
        armadoConProcesador.setProcesador(procesador);

        // Ejecución y Verificación
        assertThrows(ComponenteDeterminateDelArmadoEnNullException.class,
                () -> servicioCompatibilidades.esCompatibleConElArmado(cooler, armadoConProcesador));
    }

    @Test
    public void cuandoSeVerificaUnCoolerYEsElIncluidoConElProcesadorDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoCompleto = new ArmadoPc();
        armadoCompleto.setProcesador(procesador);
        armadoCompleto.setMotherboard(motherboard);

        when(repositorioComponenteMock.obtenerComponentePorId(anyLong()))
                .thenReturn(procesador)
                .thenReturn(motherboard);
        when(servicioCoolerMock.verificarCoolerIncluido(cooler, procesador)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(cooler, armadoCompleto);

        // Verificación
        assertTrue(esCompatible);
        // Verifica que la segunda condición del OR (||) no se ejecute por cortocircuito
        verify(servicioCoolerMock, never()).verificarCompatibilidadDeCoolerConMotherboard(any(), any());
    }

    @Test
    public void cuandoSeVerificaUnCoolerYEsCompatibleConLaMotherboardDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoCompleto = new ArmadoPc();
        armadoCompleto.setProcesador(procesador);
        armadoCompleto.setMotherboard(motherboard);

        when(repositorioComponenteMock.obtenerComponentePorId(anyLong()))
                .thenReturn(procesador)
                .thenReturn(motherboard);
        when(servicioCoolerMock.verificarCoolerIncluido(cooler, procesador)).thenReturn(false);
        when(servicioCoolerMock.verificarCompatibilidadDeCoolerConMotherboard(motherboard, cooler)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(cooler, armadoCompleto);

        // Verificación
        assertTrue(esCompatible);
    }

    // Tests para MemoriaRAM
    @Test
    public void cuandoSeVerificaUnaRAMYEsCompatibleConLaMotherDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoConMother = new ArmadoPc();
        armadoConMother.setMotherboard(motherboard);
        when(repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(motherboard);
        when(servicioMotherboardMock.verificarCompatibilidadDeMotherboardConMemoriaRAM(motherboard, ram)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(ram, armadoConMother);

        // Verificación
        assertTrue(esCompatible);
    }

    // Tests para PlacaDeVideo
    @Test
    public void cuandoSeVerificaUnaPlacaDeVideoYElProcesadorNoTieneGraficosIntegradosDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoConProcesador = new ArmadoPc();
        armadoConProcesador.setProcesador(procesador);
        when(repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(procesador);
        when(servicioPlacaDeVideoMock.verificarGraficosIntegrados(gpu, procesador)).thenReturn(false);
        when(servicioPlacaDeVideoMock.verificarPrecioMayorACero(gpu)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(gpu, armadoConProcesador);

        // Verificación
        assertTrue(esCompatible);
    }

    // Tests para Almacenamiento
    @Test
    public void cuandoSeVerificaAlmacenamientoSinMotherboardLanzaExcepcion() {
        ArmadoPc armadoVacio = new ArmadoPc();
        assertThrows(ComponenteDeterminateDelArmadoEnNullException.class,
                () -> servicioCompatibilidades.esCompatibleConElArmado(almacenamiento, armadoVacio));
    }

    @Test
    public void cuandoSeVerificaAlmacenamientoYEnUnArmadoConMotherDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoConMother = new ArmadoPc();
        armadoConMother.setMotherboard(motherboard);

        when(repositorioComponenteMock.obtenerComponentePorId(anyLong())).thenReturn(motherboard);
        when(servicioMotherboardMock.verificarCompatibilidadDeMotherboardConTipoDeConexionDeAlmacenamiento(motherboard, almacenamiento)).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(almacenamiento, armadoConMother);

        // Verificación
        assertTrue(esCompatible);
    }

    // Tests para FuenteDeAlimentacion
    @Test
    public void cuandoSeVerificaUnaFuenteCompatibleConLosWattsDelArmadoDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoPc = new ArmadoPc();
        armadoPc.setProcesador(procesador);
        armadoPc.setPlacaDeVideo(gpu);

        when(repositorioComponenteMock.obtenerComponentePorId(anyLong()))
                .thenReturn(procesador)
                .thenReturn(gpu);

        when(servicioFuenteMock.verificarCompatibilidadDeFuenteConWatsDelArmado(eq(fuente), any(ArmadoPc.class))).thenReturn(true);

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(fuente, armadoPc);

        // Verificación
        assertTrue(esCompatible);
        verify(servicioFuenteMock, times(1)).verificarCompatibilidadDeFuenteConWatsDelArmado(eq(fuente), any(ArmadoPc.class));
    }

    // Test para el caso Default del Switch
    @Test
    public void cuandoSeVerificaUnComponenteDefaultComoMonitorSiempreDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoPc = new ArmadoPc();

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(monitor, armadoPc);

        // Verificación
        assertTrue(esCompatible);
        // Verificamos que no se llama a ningún servicio de compatibilidad específico
        verify(servicioMotherboardMock, never()).verificarCompatibilidadDeMotherboardConProcesador(any(), any());
        verify(servicioCoolerMock, never()).verificarCoolerIncluido(any(), any());
    }

    @Test
    public void cuandoSeVerificaUnProcesadorSiempreDevuelveTrue() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        ArmadoPc armadoPc = new ArmadoPc();

        // Ejecución
        Boolean esCompatible = servicioCompatibilidades.esCompatibleConElArmado(procesador, armadoPc);

        // Verificación
        assertTrue(esCompatible);
    }

    // Test para la recuperacion de entidades en listas de componentes.
    @Test
    public void seCompletaLaListaDeAlmacenamientoAntesDeVerificarCompatibilidadDeFuente() throws ComponenteDeterminateDelArmadoEnNullException {
        // Arrange
        FuenteDeAlimentacion fuente = new FuenteDeAlimentacion();
        fuente.setId(10L);

        Almacenamiento almacen1 = new Almacenamiento();
        almacen1.setId(1L);
        Almacenamiento almacen2 = new Almacenamiento();
        almacen2.setId(2L);

        ArmadoPc armado = new ArmadoPc();
        armado.setAlmacenamiento(new ArrayList<>(List.of(almacen1, almacen2)));

        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(almacen1);
        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(almacen2);
        when(servicioFuenteMock.verificarCompatibilidadDeFuenteConWatsDelArmado(eq(fuente), any())).thenReturn(true);

        // Act
        Boolean compatible = servicioCompatibilidades.esCompatibleConElArmado(fuente, armado);

        // Assert
        assertTrue(compatible);
        verify(repositorioComponenteMock, times(1)).obtenerComponentePorId(1L);
        verify(repositorioComponenteMock, times(1)).obtenerComponentePorId(2L);
    }

    @Test
    public void cuandoSeObtienenLosWattsDelArmadoDevuelveElValorCorrecto() {
        // Preparación
        ArmadoPc armadoPc = new ArmadoPc();
        armadoPc.setProcesador(procesador);
        armadoPc.setPlacaDeVideo(gpu);

        when(servicioFuenteMock.obtenerWatsTotales(armadoPc)).thenReturn(450);

        // Ejecución
        Integer watts = servicioCompatibilidades.obtenerWattsDeArmado(armadoPc);

        // Verificación
        assertEquals(450, watts);
        verify(servicioFuenteMock, times(1)).obtenerWatsTotales(armadoPc);
    }

    // Métodos helper para crear entidades de prueba
    private Procesador crearProcesador(Long id, String nombre) {
        Procesador p = new Procesador();
        p.setId(id);
        p.setNombre(nombre);
        return p;
    }

    private Motherboard crearMotherboard(Long id, String nombre) {
        Motherboard m = new Motherboard();
        m.setId(id);
        m.setNombre(nombre);
        return m;
    }

    private CoolerCPU crearCooler(Long id, String nombre) {
        CoolerCPU c = new CoolerCPU();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private MemoriaRAM crearRam(Long id, String nombre) {
        MemoriaRAM r = new MemoriaRAM();
        r.setId(id);
        r.setNombre(nombre);
        return r;
    }

    private PlacaDeVideo crearGpu(Long id, String nombre) {
        PlacaDeVideo g = new PlacaDeVideo();
        g.setId(id);
        g.setNombre(nombre);
        return g;
    }

    private Almacenamiento crearAlmacenamiento(Long id, String nombre) {
        Almacenamiento a = new Almacenamiento();
        a.setId(id);
        a.setNombre(nombre);
        return a;
    }

    private FuenteDeAlimentacion crearFuente(Long id, String nombre) {
        FuenteDeAlimentacion f = new FuenteDeAlimentacion();
        f.setId(id);
        f.setNombre(nombre);
        return f;
    }

    private Monitor crearMonitor(Long id, String nombre) {
        Monitor m = new Monitor();
        m.setId(id);
        m.setNombre(nombre);
        return m;
    }
}
