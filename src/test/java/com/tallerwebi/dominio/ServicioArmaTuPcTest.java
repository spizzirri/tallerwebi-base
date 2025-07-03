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

public class ServicioArmaTuPcTest {

    private RepositorioComponente repositorioComponenteMock;
    private ServicioCompatibilidades servicioCompatibilidadesMock;
    private ServicioPrecios servicioPreciosMock;
    private ServicioArmaTuPcImpl servicioArmaTuPc;

    @BeforeEach
    public void init() {
        repositorioComponenteMock = mock(RepositorioComponente.class);
        servicioCompatibilidadesMock = mock(ServicioCompatibilidades.class);
        servicioPreciosMock = mock(ServicioPrecios.class);
        servicioArmaTuPc = new ServicioArmaTuPcImpl(repositorioComponenteMock, servicioPreciosMock, servicioCompatibilidadesMock);
    }

    // con este metodo hago componentes mas facil
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
    public void cuandoAgregoUnaMotherboardSeReiniciaElArmadoPeroConservaProcesadorMonitorYPerifericos() throws Exception {
        ArmadoPcDto armadoPcDtoInicial = new ArmadoPcDto();
        armadoPcDtoInicial.setProcesador(new ComponenteDto(crearComponente(1L, "Procesador Viejo", "Procesador")));
        armadoPcDtoInicial.setMonitor(new ComponenteDto(crearComponente(2L, "Monitor A", "Monitor")));
        armadoPcDtoInicial.getPerifericos().add(new ComponenteDto(crearComponente(3L, "Mouse", "Periferico")));

        Componente nuevaMother = crearComponente(10L, "Mother Nueva", "Motherboard");
        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(nuevaMother);

        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(10L, "motherboard", 1, armadoPcDtoInicial);

        assertNotNull(armadoResultante.getMotherboard());
        assertEquals(10L, armadoResultante.getMotherboard().getId());
        assertNotNull(armadoResultante.getProcesador());
        assertEquals(1L, armadoResultante.getProcesador().getId());
        assertNotNull(armadoResultante.getMonitor());
        assertEquals(2L, armadoResultante.getMonitor().getId());
        assertEquals(1, armadoResultante.getPerifericos().size());
    }

    @Test
    public void cuandoAgregoUnCoolerSeAnulanFuenteYGabinete() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));
        armado.setGabinete(new ComponenteDto(crearComponente(2L, "Gabinete", "Gabinete")));

        when(repositorioComponenteMock.obtenerComponentePorId(3L)).thenReturn(crearComponente(3L, "Cooler", "Cooler"));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(3L, "cooler", 1, armado);

        assertNotNull(armadoResultante.getCooler());
        assertNull(armadoResultante.getFuente());
        assertNull(armadoResultante.getGabinete());
    }

    @Test
    public void cuandoAgregoMemoriasSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));

        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(crearComponente(2L, "RAM", "Memoria"));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(2L, "memoria", 2, armado);

        assertEquals(2, armadoResultante.getRams().size());
        assertNull(armadoResultante.getFuente());
    }

    @Test
    public void cuandoAgregoGpuSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));

        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(crearComponente(2L, "Placa", "GPU"));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(2L, "gpu", 1, armado);

        assertNotNull(armadoResultante.getGpu());
        assertNull(armadoResultante.getFuente());
    }

    @Test
    public void cuandoAgregoAlmacenamientoSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));

        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(crearComponente(2L, "SSD", "Almacenamiento"));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(2L, "almacenamiento", 1, armado);

        assertEquals(1, armadoResultante.getAlmacenamiento().size());
        assertNull(armadoResultante.getFuente());
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
    public void cuandoQuitoProcesadorSeReiniciaArmadoYConservaPerifericosYMonitor() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setProcesador(new ComponenteDto(crearComponente(1L, "Proc", "Procesador")));
        armado.setMonitor(new ComponenteDto(crearComponente(2L, "Monitor", "Monitor")));
        armado.getPerifericos().add(new ComponenteDto(crearComponente(3L, "Teclado", "Periferico")));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(1L, "procesador", 1, armado);

        assertNull(armadoResultante.getProcesador());
        assertNotNull(armadoResultante.getMonitor());
        assertEquals(1, armadoResultante.getPerifericos().size());
    }

    @Test
    public void cuandoQuitoMotherboardSeReiniciaPeroConservaProcesadorMonitorYPerifericos() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setMotherboard(new ComponenteDto(crearComponente(1L, "Mother", "Motherboard")));
        armado.setProcesador(new ComponenteDto(crearComponente(2L, "Proc", "Procesador")));
        armado.setMonitor(new ComponenteDto(crearComponente(3L, "Monitor", "Monitor")));
        armado.getPerifericos().add(new ComponenteDto(crearComponente(4L, "Teclado", "Periferico")));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(1L, "motherboard", 1, armado);

        assertNull(armadoResultante.getMotherboard());
        assertNotNull(armadoResultante.getProcesador());
        assertNotNull(armadoResultante.getMonitor());
        assertEquals(1, armadoResultante.getPerifericos().size());
    }

    @Test
    public void cuandoQuitoMemoriaSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));
        armado.getRams().add(new ComponenteDto(crearComponente(2L, "RAM", "Memoria")));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(2L, "memoria", 1, armado);

        assertTrue(armadoResultante.getRams().isEmpty());
        assertNull(armadoResultante.getFuente());
    }

    @Test
    public void cuandoQuitoGpuSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.setGpu(new ComponenteDto(crearComponente(2L, "GPU", "GPU")));
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(2L, "gpu", 1, armado);

        assertNull(armadoResultante.getGpu());
        assertNull(armadoResultante.getFuente());
    }

    @Test
    public void cuandoQuitoAlmacenamientoSeBorraFuente() throws Exception {
        ArmadoPcDto armado = new ArmadoPcDto();
        armado.getAlmacenamiento().add(new ComponenteDto(crearComponente(2L, "SSD", "Almacenamiento")));
        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "Fuente")));

        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(2L, "almacenamiento", 1, armado);

        assertTrue(armadoResultante.getAlmacenamiento().isEmpty());
        assertNull(armadoResultante.getFuente());
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
        armadoPcDto.setRams(List.of(
                new ComponenteDto(), new ComponenteDto(), new ComponenteDto(), new ComponenteDto() // 4 unidades
        ));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("memoria", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaAlmacenamientoCuandoHayMenosDeSeis() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setAlmacenamiento(List.of(
                new ComponenteDto(), new ComponenteDto(), new ComponenteDto(),
                new ComponenteDto(), new ComponenteDto() // 5 unidades
        ));
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaAlmacenamientoCuandoHaySeis() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setAlmacenamiento(List.of(
                new ComponenteDto(), new ComponenteDto(), new ComponenteDto(),
                new ComponenteDto(), new ComponenteDto(), new ComponenteDto() // 6 unidades
        ));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaPerifericosCuandoHayMenosDeDiez() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        // 9 periféricos
        for (int i = 0; i < 9; i++) {
            armadoPcDto.getPerifericos().add(new ComponenteDto());
        }
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("periferico", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaPerifericosCuandoHayDiez() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        // 10 periféricos
        for (int i = 0; i < 10; i++) {
            armadoPcDto.getPerifericos().add(new ComponenteDto());
        }
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("periferico", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaComponentesUnicosComoProcesador() {
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("procesador", armadoPcDto));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("gpu", armadoPcDto));
    }

    // Tests para armadoCompleto
    // POSIBLEMENTE ESTOS CAMBIEN SEGUN AVANCE EL FLUJO DEL ARMADO PASO A PASO
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

    @Test
    public void cuandoAgregoUnComponenteSeFormateaSuPrecioEnPesos() throws LimiteDeComponenteSobrepasadoEnElArmadoException {
        // Preparación
        Long idComponente = 1L;
        String tipoComponente = "procesador";
        ArmadoPcDto armadoPcDtoInicial = new ArmadoPcDto();
        Componente componenteAAgregar = crearComponente(idComponente, "Intel i5", "Procesador");
        componenteAAgregar.setPrecio(250.0);
        String precioFormateadoEsperado = "$ 250.000,00";

        when(repositorioComponenteMock.obtenerComponentePorId(idComponente)).thenReturn(componenteAAgregar);
        when(servicioPreciosMock.conversionDolarAPeso(250.0)).thenReturn(precioFormateadoEsperado);

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(idComponente, tipoComponente, 1, armadoPcDtoInicial);

        // Verificación
        // Verificar que el servicio de precios fue llamado con el precio correcto en dólares
        verify(servicioPreciosMock, times(1)).conversionDolarAPeso(250.0);

        // Verificar que el DTO del componente agregado tiene el precio formateado
        ComponenteDto componenteAgregadoDto = armadoResultante.getProcesador();
        assertNotNull(componenteAgregadoDto);
        assertEquals(precioFormateadoEsperado, componenteAgregadoDto.getPrecioFormateado());
    }



}
