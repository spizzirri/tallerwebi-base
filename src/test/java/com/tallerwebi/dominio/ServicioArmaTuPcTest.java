package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.dto.ArmadoPcDto;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
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

    //region Métodos de Ayuda para crear Mocks
    private Componente crearComponente(Long id, String nombre, String tipo) {
        Componente componente;

        switch (tipo) {
            case "Procesador":
                componente = new Procesador();
                break;
            case "Motherboard":
                componente = new Motherboard();
                break;
            case "FuenteDeAlimentacion":
                componente = new FuenteDeAlimentacion();
                break;
            case "CoolerCPU":
                componente = new CoolerCPU();
                break;
            case "MemoriaRAM":
                componente = new MemoriaRAM();
                break;
            case "PlacaDeVideo":
                componente = new PlacaDeVideo();
                break;
            case "Gabinete":
                componente = new Gabinete();
                break;
            case "Monitor":
                componente = new Monitor();
                break;
            case "Periferico":
                componente = new Periferico();
                break;
            case "Almacenamiento":
                componente = new Almacenamiento();
                break;
            default:
                throw new IllegalArgumentException("Tipo de componente no reconocido: " + tipo);
        }

        componente.setId(id);
        componente.setNombre(nombre);
        componente.setStock(10);
        componente.setPrecio(100.0);
        return componente;
    }

    // Sobrecarga para Almacenamiento
    private Almacenamiento crearAlmacenamiento(Long id, String nombre, String tipoConexion, Integer stock) {
        Almacenamiento almacenamiento = new Almacenamiento();
        almacenamiento.setId(id);
        almacenamiento.setNombre(nombre);
        almacenamiento.setTipoDeConexion(tipoConexion);
        almacenamiento.setStock(stock);
        almacenamiento.setPrecio(100.0);
        return almacenamiento;
    }

    // Sobrecarga para Motherboard con slots
    private Motherboard crearMotherboard(Long id, String nombre, int slotsRam, int slotsM2, int slotsSata) {
        Motherboard motherboard = new Motherboard();
        motherboard.setId(id);
        motherboard.setNombre(nombre);
        motherboard.setCantSlotsRAM(slotsRam);
        motherboard.setCantSlotsM2(slotsM2);
        motherboard.setCantPuertosSata(slotsSata);
        return motherboard;
    }

    //endregion

    //region Tests para obtenerListaDeComponentesCompatiblesDto
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
        List<ComponenteDto> resultado = new ArrayList<>(servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto));

        // Verificación
        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getId(), equalTo(1L));
        verify(repositorioComponenteMock).obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard");
        verify(servicioCompatibilidadesMock, times(2)).esCompatibleConElArmado(any(Componente.class), any());
    }

    @Test
    public void cuandoPidoComponentesPeroFaltaUnoDeterminanteLanzaExcepcion() throws ComponenteDeterminateDelArmadoEnNullException {
        // Preparación
        String tipoComponente = "motherboard";
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(anyString())).thenReturn(List.of(crearComponente(1L, "Mother A", "Motherboard")));
        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(), any())).thenThrow(new ComponenteDeterminateDelArmadoEnNullException("Se necesita un procesador"));

        // Ejecución y Verificación
        assertThrows(ComponenteDeterminateDelArmadoEnNullException.class, () -> {
            servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);
        });
    }
    //endregion

    //region Tests para agregarComponenteAlArmado


    @Test
    public void cuandoAgregoUnProcesadorSeReiniciaElArmadoYSeGestionaElStock() throws Exception {
        // Preparación
        ArmadoPcDto armadoInicial = new ArmadoPcDto();
        armadoInicial.setMotherboard(new ComponenteDto(crearComponente(10L, "Mother Vieja", "Motherboard")));
        armadoInicial.setMonitor(new ComponenteDto(crearComponente(11L, "Monitor A", "Monitor")));

        Componente nuevoProcesador = crearComponente(1L, "Intel i9", "Procesador");
        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(nuevoProcesador);

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(1L, "procesador", 1, armadoInicial);

        // Verificación
        assertNotNull(armadoResultante.getProcesador());
        assertNull(armadoResultante.getMotherboard()); // La motherboard vieja debe desaparecer
        assertNotNull(armadoResultante.getMonitor()); // El monitor debe conservarse

        // Verificación de Stock
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(10L, 1); // Devuelve stock de la mother vieja
        verify(repositorioComponenteMock, times(1)).descontarStockDeUnComponente(1L, 1); // Descuenta stock del nuevo CPU
    }

    @Test
    public void cuandoAgregoGpuSeBorraFuenteYSeGestionaStock() throws Exception {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Componente fuente = crearComponente(1L, "Fuente", "FuenteDeAlimentacion");
        Componente gpu = crearComponente(2L, "Placa", "PlacaDeVideo");

        armado.setFuente(new ComponenteDto(fuente));
        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(gpu);

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(2L, "gpu", 1, armado);

        // Verificación
        assertNotNull(armadoResultante.getGpu());
        assertNull(armadoResultante.getFuente());
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1);
        verify(repositorioComponenteMock, times(1)).descontarStockDeUnComponente(2L, 1);
    }

    @Test
    public void cuandoIntentoAgregarUnComponenteSinStockSuficienteLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        Componente componenteSinStock = crearComponente(1L, "CPU Sin Stock", "Procesador");
        componenteSinStock.setStock(1);
        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(componenteSinStock);

        // Ejecución y Verificación
        assertThrows(ComponenteSinStockPedidoException.class, () -> {
            servicioArmaTuPc.agregarComponenteAlArmado(1L, "procesador", 2, armadoPcDto);
        });
        verify(repositorioComponenteMock, never()).descontarStockDeUnComponente(anyLong(), anyInt());
    }

    @Test
    public void cuandoIntentoAgregarMasMemoriasDeLosSlotsDisponiblesLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 2, 2, 4); // 2 slots RAM
        armado.setMotherboard(new ComponenteDto(motherboard));

        armado.getRams().addAll(List.of(
                new ComponenteDto(crearComponente(20L, "RAM 1", "MemoriaRAM")),
                new ComponenteDto(crearComponente(21L, "RAM 2", "MemoriaRAM"))
        ));

        Componente terceraRam = crearComponente(22L, "RAM 3", "MemoriaRAM");
        when(repositorioComponenteMock.obtenerComponentePorId(22L)).thenReturn(terceraRam);
        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);

        // Ejecución y Verificación
        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
            servicioArmaTuPc.agregarComponenteAlArmado(22L, "memoria", 1, armado);
        });
    }

    @Test
    public void cuandoIntentoAgregarMasAlmacenamientoM2DelPermitidoLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother X570", 4, 1, 1); // Solo 1 slot M.2
        armado.setMotherboard(new ComponenteDto(motherboard));

        Almacenamiento m2Existente = crearAlmacenamiento(20L, "SSD M.2 1TB", "M2", 10);
        Almacenamiento m2Nuevo = crearAlmacenamiento(21L, "SSD M.2 2TB", "M2", 10);

        armado.getAlmacenamiento().add(new ComponenteDto(m2Existente));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
        when(repositorioComponenteMock.obtenerComponentePorId(20L)).thenReturn(m2Existente);
        when(repositorioComponenteMock.obtenerComponentePorId(21L)).thenReturn(m2Nuevo);

        // Ejecución y Verificación
        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
            servicioArmaTuPc.agregarComponenteAlArmado(21L, "almacenamiento", 1, armado);
        });
    }

    @Test
    public void cuandoIntentoAgregarMasAlmacenamientoSATADelPermitidoLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 4, 2, 1); // Solo 2 slots SATA
        armado.setMotherboard(new ComponenteDto(motherboard));

        Almacenamiento sataExistente1 = crearAlmacenamiento(30L, "HDD SATA 1TB", "SATA", 10);
        Almacenamiento sataExistente2 = crearAlmacenamiento(31L, "HDD SATA 2TB", "SATA", 10);
        Almacenamiento sataNuevo = crearAlmacenamiento(32L, "SSD SATA 512GB", "SATA", 10);

        armado.getAlmacenamiento().add(new ComponenteDto(sataExistente1));
        armado.getAlmacenamiento().add(new ComponenteDto(sataExistente2));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
        when(repositorioComponenteMock.obtenerComponentePorId(30L)).thenReturn(sataExistente1);
        when(repositorioComponenteMock.obtenerComponentePorId(31L)).thenReturn(sataExistente2);
        when(repositorioComponenteMock.obtenerComponentePorId(32L)).thenReturn(sataNuevo);

        // Ejecución y Verificación
        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
            servicioArmaTuPc.agregarComponenteAlArmado(32L, "almacenamiento", 1, armado);
        });
    }

    @Test
    public void cuandoAgregoAlmacenamientoSATADentroDelLimiteNoLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 4, 1, 3); // 3 slots SATA disponibles
        armado.setMotherboard(new ComponenteDto(motherboard));

        Almacenamiento sataExistente1 = crearAlmacenamiento(30L, "HDD SATA 1TB", "SATA", 10);
        Almacenamiento sataExistente2 = crearAlmacenamiento(31L, "HDD SATA 2TB", "SATA", 10);
        Almacenamiento sataNuevo = crearAlmacenamiento(32L, "SSD SATA 512GB", "SATA", 10);

        armado.getAlmacenamiento().add(new ComponenteDto(sataExistente1));
        armado.getAlmacenamiento().add(new ComponenteDto(sataExistente2));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
        when(repositorioComponenteMock.obtenerComponentePorId(30L)).thenReturn(sataExistente1);
        when(repositorioComponenteMock.obtenerComponentePorId(31L)).thenReturn(sataExistente2);
        when(repositorioComponenteMock.obtenerComponentePorId(32L)).thenReturn(sataNuevo);

        // Ejecución y Verificación
        assertDoesNotThrow(() -> {
            servicioArmaTuPc.agregarComponenteAlArmado(32L, "almacenamiento", 1, armado);
        });
        assertEquals(3, armado.getAlmacenamiento().size());
    }

    @Test
    public void cuandoAgregoAlmacenamientoM2DentroDelLimiteNoLanzaExcepcion() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother X570", 4, 2, 2); // 2 slots M.2 disponibles
        armado.setMotherboard(new ComponenteDto(motherboard));

        Almacenamiento m2Existente = crearAlmacenamiento(20L, "SSD M.2 1TB", "M2", 10);
        Almacenamiento m2Nuevo = crearAlmacenamiento(21L, "SSD M.2 2TB", "M2", 10);

        armado.getAlmacenamiento().add(new ComponenteDto(m2Existente));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
        when(repositorioComponenteMock.obtenerComponentePorId(20L)).thenReturn(m2Existente);
        when(repositorioComponenteMock.obtenerComponentePorId(21L)).thenReturn(m2Nuevo);

        // Ejecución y Verificación
        assertDoesNotThrow(() -> {
            servicioArmaTuPc.agregarComponenteAlArmado(21L, "almacenamiento", 1, armado);
        });
        assertEquals(2, armado.getAlmacenamiento().size());
    }



    //endregion

    //region Tests para quitarComponenteAlArmado
    @Test
    public void cuandoQuitoUnCoolerSeAnulanFuenteYGabineteYSeDevuelveStock() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Componente cooler = crearComponente(40L, "Cooler Master", "CoolerCPU");
        Componente fuente = crearComponente(41L, "Fuente 850W", "FuenteDeAlimentacion");
        Componente gabinete = crearComponente(42L, "Gabinete ATX", "Gabinete");

        armado.setCooler(new ComponenteDto(cooler));
        armado.setFuente(new ComponenteDto(fuente));
        armado.setGabinete(new ComponenteDto(gabinete));

        // Ejecución
        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(40L, "cooler", 1, armado);

        // Verificación
        assertNull(armadoResultante.getCooler());
        assertNull(armadoResultante.getFuente());
        assertNull(armadoResultante.getGabinete());

        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(40L, 1);
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(41L, 1);
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(42L, 1);
    }

    @Test
    public void cuandoQuitoUnProcesadorSeDevuelveElStockDeTodosLosComponentesReseteados() throws QuitarComponenteInvalidoException, QuitarStockDemasDeComponenteException {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Componente procesador = crearComponente(1L, "CPU", "Procesador");
        Componente mother = crearComponente(2L, "Mother", "Motherboard");
        Componente ram = crearComponente(3L, "RAM", "MemoriaRAM");

        armado.setProcesador(new ComponenteDto(procesador));
        armado.setMotherboard(new ComponenteDto(mother));
        armado.getRams().add(new ComponenteDto(ram));

        // Ejecución
        servicioArmaTuPc.quitarComponenteAlArmado(1L, "procesador", 1, armado);

        // Verificación
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1); // El que se quita
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(2L, 1); // El reseteado
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(3L, 1); // El reseteado
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
            servicioArmaTuPc.quitarComponenteAlArmado(50L, "almacenamiento", 2, armadoPcDto);
        });
    }
    //endregion

    //region Tests para Métodos de Utilería y Flujo
    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaMemoriaSiHaySlotsLibres() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 4, 2, 4); // 4 slots RAM
        armadoPcDto.setMotherboard(new ComponenteDto(motherboard));
        armadoPcDto.getRams().add(new ComponenteDto(crearComponente(20L, "RAM", "MemoriaRAM")));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);

        // Ejecución y Verificación
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("memoria", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaAlmacenamientoSiHaySlotsLibres() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother con slots", 4, 2, 1); // 2 SATA + 1 M2 = 3 slots total
        armadoPcDto.setMotherboard(new ComponenteDto(motherboard));

        // Actualmente hay 2 unidades de almacenamiento usadas
        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearAlmacenamiento(20L, "HDD", "SATA", 10)));
        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearAlmacenamiento(21L, "SSD M2", "M2", 10)));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);

        // Ejecución y Verificación
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaAlmacenamientoSiNoHaySlotsDisponibles() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        Motherboard motherboard = crearMotherboard(10L, "Mother sin espacio", 4, 1, 1); // 1 SATA + 1 M2 = 2 slots total
        armadoPcDto.setMotherboard(new ComponenteDto(motherboard));

        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearAlmacenamiento(20L, "HDD", "SATA", 10)));
        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearAlmacenamiento(21L, "SSD M2", "M2", 10)));

        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);

        // Ejecución y Verificación
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("almacenamiento", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveTrueParaPerifericoSiHayMenosDe10() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        for (int i = 0; i < 9; i++) {
            armadoPcDto.getPerifericos().add(new ComponenteDto(crearComponente((long) i, "Periférico " + i, "Periferico")));
        }

        // Ejecución y Verificación
        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("periferico", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaPerifericoSiYaHay10() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        for (int i = 0; i < 10; i++) {
            armadoPcDto.getPerifericos().add(new ComponenteDto(crearComponente((long) i, "Periférico " + i, "Periferico")));
        }

        // Ejecución y Verificación
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("periferico", armadoPcDto));
    }

    @Test
    public void sePuedeAgregarMasUnidadesDevuelveFalseParaComponentesUnicosComoProcesador() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();

        // Ejecución y Verificación
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("procesador", armadoPcDto));
        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("gpu", armadoPcDto));
    }

    @Test
    public void armadoCompletoDevuelveTrueCuandoTieneLosComponentesEsenciales() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setProcesador(new ComponenteDto());
        armadoPcDto.setMotherboard(new ComponenteDto());
        armadoPcDto.setCooler(new ComponenteDto());
        armadoPcDto.setGabinete(new ComponenteDto());

        // Ejecución y Verificación
        assertTrue(servicioArmaTuPc.armadoCompleto(armadoPcDto));
    }

    @Test
    public void armadoCompletoDevuelveFalseCuandoTieneLosComponentesEsenciales() {
        // Preparación
        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
        armadoPcDto.setGabinete(new ComponenteDto());

        // Ejecución y Verificación
        assertFalse(servicioArmaTuPc.armadoCompleto(armadoPcDto));
    }

    @Test
    public void alDevolverStockDeArmadoSeLlamaAlRepositorioConLasCantidadesCorrectas() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Componente procesador = crearComponente(1L, "CPU", "Procesador");
        Componente ram = crearComponente(2L, "RAM", "MemoriaRAM");

        armado.setProcesador(new ComponenteDto(procesador));
        armado.getRams().add(new ComponenteDto(ram));
        armado.getRams().add(new ComponenteDto(ram));

        // Ejecución
        servicioArmaTuPc.devolverStockDeArmado(armado);

        // Verificación
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1);
        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(2L, 2);
    }

    @Test
    public void pasajeAProductoArmadoDtoParaAgregarAlCarritoConvierteCorrectamente() {
        // Preparación
        ArmadoPcDto armado = new ArmadoPcDto();
        Componente cpu = crearComponente(1L, "CPU", "Procesador");
        Componente ram = crearComponente(2L, "RAM", "MemoriaRAM");
        armado.setProcesador(new ComponenteDto(cpu));
        armado.getRams().add(new ComponenteDto(ram));
        armado.getRams().add(new ComponenteDto(ram));

        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(cpu);
        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(ram);

        // Ejecución
        List<ProductoCarritoArmadoDto> resultado = servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armado);

        // Verificación
        assertThat(resultado, hasSize(2));
        ProductoCarritoArmadoDto dtoCpu = resultado.stream().filter(r -> r.getId().equals(1L)).findFirst().orElse(null);
        ProductoCarritoArmadoDto dtoRam = resultado.stream().filter(r -> r.getId().equals(2L)).findFirst().orElse(null);

        assertNotNull(dtoCpu);
        assertNotNull(dtoRam);
        assertEquals(1, dtoCpu.getCantidad());
        assertEquals(2, dtoRam.getCantidad());
    }
    //endregion
}