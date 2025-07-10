//package com.tallerwebi.dominio;
//
//import com.tallerwebi.dominio.entidades.Almacenamiento;
//import com.tallerwebi.dominio.entidades.Componente;
//import com.tallerwebi.dominio.entidades.Motherboard;
//import com.tallerwebi.dominio.entidades.Procesador;
//import com.tallerwebi.dominio.excepcion.*;
//import com.tallerwebi.presentacion.dto.ArmadoPcDto;
//import com.tallerwebi.presentacion.dto.ComponenteDto;
//import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class ServicioArmaTuPcTest {
//
//    private RepositorioComponente repositorioComponenteMock;
//    private ServicioCompatibilidades servicioCompatibilidadesMock;
//    private ServicioPrecios servicioPreciosMock;
//    private ServicioArmaTuPcImpl servicioArmaTuPc;
//
//    @BeforeEach
//    public void init() {
//        repositorioComponenteMock = mock(RepositorioComponente.class);
//        servicioCompatibilidadesMock = mock(ServicioCompatibilidades.class);
//        servicioPreciosMock = mock(ServicioPrecios.class);
//        servicioArmaTuPc = new ServicioArmaTuPcImpl(repositorioComponenteMock, servicioPreciosMock, servicioCompatibilidadesMock);
//    }
//
//    //region Métodos de Ayuda para crear Mocks
//    private Componente crearComponente(Long id, String nombre, String tipo) {
//        Componente componente = new Procesador(); // Clase base para simplicidad
//        componente.setId(id);
//        componente.setNombre(nombre);
//        componente.setStock(10); // Stock por defecto para no fallar tests
//        componente.setPrecio(100.0); // Precio por defecto
//        return componente;
//    }
//
//    private Almacenamiento crearAlmacenamiento(Long id, String nombre, String tipoConexion) {
//        Almacenamiento almacenamiento = new Almacenamiento();
//        almacenamiento.setId(id);
//        almacenamiento.setNombre(nombre);
//        almacenamiento.setTipoDeConexion(tipoConexion);
//        almacenamiento.setStock(10);
//        almacenamiento.setPrecio(100.0);
//        return almacenamiento;
//    }
//
//    private Motherboard crearMotherboard(Long id, String nombre, int slotsRam, int slotsM2, int slotsSata) {
//        Motherboard motherboard = new Motherboard();
//        motherboard.setId(id);
//        motherboard.setNombre(nombre);
//        motherboard.setCantSlotsRAM(slotsRam);
//        motherboard.setCantSlotsM2(slotsM2);
//        motherboard.setCantPuertosSata(slotsSata);
//        motherboard.setStock(10);
//        return motherboard;
//    }
//    //endregion
//
//    //region Tests para obtenerListaDeComponentesCompatiblesDto
//    @Test
//    public void cuandoPidoComponentesCompatiblesYTodosLoSonDevuelveListaCompletaDeDtos() throws ComponenteDeterminateDelArmadoEnNullException {
//        // Preparación
//        String tipoComponente = "motherboard";
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        List<Componente> componentesDelRepo = List.of(
//                crearComponente(1L, "Mother A", "Motherboard"),
//                crearComponente(2L, "Mother B", "Motherboard")
//        );
//        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard")).thenReturn(componentesDelRepo);
//        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(Componente.class), any())).thenReturn(true);
//
//        // Ejecución
//        List<ComponenteDto> resultado = new ArrayList<>(servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto));
//
//        // Verificación
//        assertThat(resultado, hasSize(2));
//        assertThat(resultado.get(0).getId(), equalTo(1L));
//        verify(repositorioComponenteMock).obtenerComponentesPorTipoEnStockOrdenadosPorPrecio("Motherboard");
//        verify(servicioCompatibilidadesMock, times(2)).esCompatibleConElArmado(any(Componente.class), any());
//    }
//
//    @Test
//    public void cuandoPidoComponentesPeroFaltaUnoDeterminanteLanzaExcepcion() throws ComponenteDeterminateDelArmadoEnNullException {
//        // Preparación
//        String tipoComponente = "motherboard";
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        when(repositorioComponenteMock.obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(anyString())).thenReturn(List.of(crearComponente(1L, "Mother A", "Motherboard")));
//        when(servicioCompatibilidadesMock.esCompatibleConElArmado(any(), any())).thenThrow(new ComponenteDeterminateDelArmadoEnNullException("Se necesita un procesador"));
//
//        // Ejecución y Verificación
//        assertThrows(ComponenteDeterminateDelArmadoEnNullException.class, () -> {
//            servicioArmaTuPc.obtenerListaDeComponentesCompatiblesDto(tipoComponente, armadoPcDto);
//        });
//    }
//    //endregion
//
//    //region Tests para agregarComponenteAlArmado
//    @Test
//    public void cuandoAgregoUnProcesadorSeReiniciaElArmadoYSeGestionaElStock() throws Exception {
//        // Preparación
//        ArmadoPcDto armadoInicial = new ArmadoPcDto();
//        armadoInicial.setMotherboard(new ComponenteDto(crearComponente(10L, "Mother Vieja", "Motherboard")));
//        armadoInicial.setMonitor(new ComponenteDto(crearComponente(11L, "Monitor A", "Monitor")));
//
//        Componente nuevoProcesador = crearComponente(1L, "Intel i9", "Procesador");
//        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(nuevoProcesador);
//
//        // Ejecución
//        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(1L, "procesador", 1, armadoInicial);
//
//        // Verificación
//        assertNotNull(armadoResultante.getProcesador());
//        assertNull(armadoResultante.getMotherboard()); // La motherboard vieja debe desaparecer
//        assertNotNull(armadoResultante.getMonitor()); // El monitor debe conservarse
//
//        // Verificación de Stock
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(10L, 1); // Devuelve stock de la mother vieja
//        verify(repositorioComponenteMock, times(1)).descontarStockDeUnComponente(1L, 1); // Descuenta stock del nuevo CPU
//    }
//
//    @Test
//    public void cuandoAgregoGpuSeBorraFuenteYSeGestionaStock() throws Exception {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        armado.setFuente(new ComponenteDto(crearComponente(1L, "Fuente", "FuenteDeAlimentacion")));
//        Componente nuevaGpu = crearComponente(2L, "Placa", "GPU");
//        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(nuevaGpu);
//
//        // Ejecución
//        ArmadoPcDto armadoResultante = servicioArmaTuPc.agregarComponenteAlArmado(2L, "gpu", 1, armado);
//
//        // Verificación
//        assertNotNull(armadoResultante.getGpu());
//        assertNull(armadoResultante.getFuente());
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1); // Devuelve stock de la fuente
//        verify(repositorioComponenteMock, times(1)).descontarStockDeUnComponente(2L, 1); // Descuenta stock de la GPU
//    }
//
//    @Test
//    public void cuandoIntentoAgregarUnComponenteSinStockSuficienteLanzaExcepcion() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        Componente componenteSinStock = crearComponente(1L, "CPU Sin Stock", "Procesador");
//        componenteSinStock.setStock(1);
//        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(componenteSinStock);
//
//        // Ejecución y Verificación
//        assertThrows(ComponenteSinStockPedidoException.class, () -> {
//            servicioArmaTuPc.agregarComponenteAlArmado(1L, "procesador", 2, armadoPcDto);
//        });
//        verify(repositorioComponenteMock, never()).descontarStockDeUnComponente(anyLong(), anyInt());
//    }
//
//    @Test
//    public void cuandoIntentoAgregarMasMemoriasDeLosSlotsDisponiblesLanzaExcepcion() {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 2, 2, 4); // Solo 2 slots de RAM
//        armado.setMotherboard(new ComponenteDto(motherboard));
//        armado.getRams().addAll(List.of(
//                new ComponenteDto(crearComponente(20L, "RAM 1", "MemoriaRAM")),
//                new ComponenteDto(crearComponente(21L, "RAM 2", "MemoriaRAM"))
//        ));
//
//        Componente terceraRam = crearComponente(22L, "RAM 3", "MemoriaRAM");
//        when(repositorioComponenteMock.obtenerComponentePorId(22L)).thenReturn(terceraRam);
//        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
//
//        // Ejecución y Verificación
//        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
//            servicioArmaTuPc.agregarComponenteAlArmado(22L, "memoria", 1, armado);
//        });
//    }
//
//    @Test
//    public void cuandoIntentoAgregarMasAlmacenamientoM2DelPermitidoLanzaExcepcion() {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        Motherboard motherboard = crearMotherboard(10L, "Mother X570", 4, 1, 6); // Solo 1 slot M.2
//        armado.setMotherboard(new ComponenteDto(motherboard));
//        Almacenamiento m2Existente = crearAlmacenamiento(20L, "SSD M.2 1TB", "M2");
//        armado.getAlmacenamiento().add(new ComponenteDto(m2Existente));
//
//        Almacenamiento m2Nuevo = crearAlmacenamiento(21L, "SSD M.2 2TB", "M2");
//
//        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
//        when(repositorioComponenteMock.obtenerComponentePorId(20L)).thenReturn(m2Existente);
//        when(repositorioComponenteMock.obtenerComponentePorId(21L)).thenReturn(m2Nuevo);
//
//        // Ejecución y Verificación
//        assertThrows(LimiteDeComponenteSobrepasadoEnElArmadoException.class, () -> {
//            servicioArmaTuPc.agregarComponenteAlArmado(21L, "almacenamiento", 1, armado);
//        });
//    }
//    //endregion
//
//    //region Tests para quitarComponenteAlArmado
//    @Test
//    public void cuandoQuitoUnCoolerSeAnulanFuenteYGabineteYSeDevuelveStock() throws Exception {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        armado.setCooler(new ComponenteDto(crearComponente(40L, "Cooler Master", "CoolerCPU")));
//        armado.setFuente(new ComponenteDto(crearComponente(41L, "Fuente 850W", "FuenteDeAlimentacion")));
//        armado.setGabinete(new ComponenteDto(crearComponente(42L, "Gabinete ATX", "Gabinete")));
//
//        // Ejecución
//        ArmadoPcDto armadoResultante = servicioArmaTuPc.quitarComponenteAlArmado(40L, "cooler", 1, armado);
//
//        // Verificación
//        assertNull(armadoResultante.getCooler());
//        assertNull(armadoResultante.getFuente());
//        assertNull(armadoResultante.getGabinete());
//
//        // Verificación de Stock
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(40L, 1);
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(41L, 1);
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(42L, 1);
//    }
//
//    @Test
//    public void cuandoQuitoUnProcesadorSeDevuelveElStockDeTodosLosComponentesReseteados() throws Exception {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        armado.setProcesador(new ComponenteDto(crearComponente(1L, "CPU", "Procesador")));
//        armado.setMotherboard(new ComponenteDto(crearComponente(2L, "Mother", "Motherboard")));
//        armado.getRams().add(new ComponenteDto(crearComponente(3L, "RAM", "MemoriaRAM")));
//
//        // Ejecución
//        servicioArmaTuPc.quitarComponenteAlArmado(1L, "procesador", 1, armado);
//
//        // Verificación
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1); // El que se quita
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(2L, 1); // El reseteado
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(3L, 1); // El reseteado
//    }
//
//    @Test
//    public void cuandoIntentoQuitarUnComponenteQueNoEstaEnElArmadoLanzaExcepcion() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto(); // Armado vacío
//
//        // Ejecución y Verificación
//        assertThrows(QuitarComponenteInvalidoException.class, () -> {
//            servicioArmaTuPc.quitarComponenteAlArmado(99L, "procesador", 1, armadoPcDto);
//        });
//    }
//
//    @Test
//    public void cuandoIntentoQuitarMasAlmacenamientoDelExistenteLanzaExcepcion() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        armadoPcDto.getAlmacenamiento().add(new ComponenteDto(crearComponente(50L, "SSD 1TB", "Almacenamiento")));
//
//        // Ejecución y Verificación
//        assertThrows(QuitarStockDemasDeComponenteException.class, () -> {
//            servicioArmaTuPc.quitarComponenteAlArmado(50L, "almacenamiento", 2, armadoPcDto);
//        });
//    }
//    //endregion
//
//    //region Tests para Métodos de Utilería y Flujo
//    @Test
//    public void sePuedeAgregarMasUnidadesDevuelveTrueParaMemoriaSiHaySlotsLibres() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        Motherboard motherboard = crearMotherboard(10L, "Mother B450", 4, 2, 4);
//        armadoPcDto.setMotherboard(new ComponenteDto(motherboard));
//        armadoPcDto.getRams().add(new ComponenteDto());
//        when(repositorioComponenteMock.obtenerComponentePorId(10L)).thenReturn(motherboard);
//
//        // Ejecución y Verificación
//        assertTrue(servicioArmaTuPc.sePuedeAgregarMasUnidades("memoria", armadoPcDto));
//    }
//
//    @Test
//    public void sePuedeAgregarMasUnidadesDevuelveFalseParaComponentesUnicosComoProcesador() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//
//        // Ejecución y Verificación
//        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("procesador", armadoPcDto));
//        assertFalse(servicioArmaTuPc.sePuedeAgregarMasUnidades("gpu", armadoPcDto));
//    }
//
//    @Test
//    public void armadoCompletoDevuelveTrueCuandoTieneLosComponentesEsenciales() {
//        // Preparación
//        ArmadoPcDto armadoPcDto = new ArmadoPcDto();
//        armadoPcDto.setProcesador(new ComponenteDto());
//        armadoPcDto.setMotherboard(new ComponenteDto());
//        armadoPcDto.setCooler(new ComponenteDto());
//        armadoPcDto.setGabinete(new ComponenteDto());
//
//        // Ejecución y Verificación
//        assertTrue(servicioArmaTuPc.armadoCompleto(armadoPcDto));
//    }
//
//    @Test
//    public void alDevolverStockDeArmadoSeLlamaAlRepositorioConLasCantidadesCorrectas() {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        armado.setProcesador(new ComponenteDto(crearComponente(1L, "CPU", "Procesador")));
//        armado.getRams().add(new ComponenteDto(crearComponente(2L, "RAM", "MemoriaRAM")));
//        armado.getRams().add(new ComponenteDto(crearComponente(2L, "RAM", "MemoriaRAM")));
//
//        // Ejecución
//        servicioArmaTuPc.devolverStockDeArmado(armado);
//
//        // Verificación
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(1L, 1);
//        verify(repositorioComponenteMock, times(1)).devolverStockDeUnComponente(2L, 2);
//    }
//
//    @Test
//    public void pasajeAProductoArmadoDtoParaAgregarAlCarritoConvierteCorrectamente() {
//        // Preparación
//        ArmadoPcDto armado = new ArmadoPcDto();
//        Componente cpu = crearComponente(1L, "CPU", "Procesador");
//        Componente ram = crearComponente(2L, "RAM", "MemoriaRAM");
//        armado.setProcesador(new ComponenteDto(cpu));
//        armado.getRams().add(new ComponenteDto(ram));
//        armado.getRams().add(new ComponenteDto(ram));
//
//        when(repositorioComponenteMock.obtenerComponentePorId(1L)).thenReturn(cpu);
//        when(repositorioComponenteMock.obtenerComponentePorId(2L)).thenReturn(ram);
//
//        // Ejecución
//        List<ProductoCarritoArmadoDto> resultado = servicioArmaTuPc.pasajeAProductoArmadoDtoParaAgregarAlCarrito(armado);
//
//        // Verificación
//        assertThat(resultado, hasSize(2));
//        ProductoCarritoArmadoDto dtoCpu = resultado.stream().filter(r -> r.getId().equals(1L)).findFirst().orElse(null);
//        ProductoCarritoArmadoDto dtoRam = resultado.stream().filter(r -> r.getId().equals(2L)).findFirst().orElse(null);
//
//        assertNotNull(dtoCpu);
//        assertNotNull(dtoRam);
//        assertEquals(1, dtoCpu.getCantidad());
//        assertEquals(2, dtoRam.getCantidad());
//    }
//    //endregion
//}