package com.tallerwebi.dominio;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicioProductoEspecificoTest {

    @Mock
    private RepositorioComponente repositorioComponente;

    @InjectMocks
    private ServicioProductoEspecificoImpl servicioProductoEspecifico;

    @Test
    void obtenerComponentePorIdDeberiaLlamarAlRepositorioCorrectamente() {
        // Given
        Long id = 1L;
        Componente componenteEsperado = new Procesador();
        componenteEsperado.setId(id);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(componenteEsperado);

        // When
        Componente resultado = servicioProductoEspecifico.obtenerComponentePorId(id);

        // Then
        assertThat(resultado, is(equalTo(componenteEsperado)));
        assertThat(resultado.getId(), is(equalTo(id)));
        verify(repositorioComponente, times(1)).buscarComponenteConImagenesPorId(id);
    }

    @Test
    void obtenerComponentesAcompararConProcesadorDeberiaRetornarProcesadoresMismaFamilia() {
        Long id = 1L;

        Procesador procesadorPrincipal = new Procesador();
        procesadorPrincipal.setId(1L);
        procesadorPrincipal.setNombre("Intel Core i5-12600K");
        procesadorPrincipal.setFamilia("Intel Core i5");

        Procesador procesadorSimilar1 = new Procesador();
        procesadorSimilar1.setId(2L);
        procesadorSimilar1.setNombre("Intel Core i5-12400");
        procesadorSimilar1.setFamilia("Intel Core i5");

        Procesador procesadorDiferente = new Procesador();
        procesadorDiferente.setId(3L);
        procesadorDiferente.setNombre("Intel Core i7-12700K");
        procesadorDiferente.setFamilia("Intel Core i7");

        List<Componente> componentesEnStock = List.of(procesadorPrincipal, procesadorSimilar1, procesadorDiferente);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(procesadorPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("Procesador")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, is(not(empty())));
        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getId(), is(equalTo(procesadorPrincipal.getId())));
        assertThat(resultado.get(1).getId(), is(equalTo(procesadorSimilar1.getId())));

        assertThat(resultado.stream().map(ComponenteEspecificoDto::getId).collect(Collectors.toList()),
                containsInAnyOrder(procesadorPrincipal.getId(), procesadorSimilar1.getId()));

        verify(repositorioComponente, times(1)).buscarComponenteConImagenesPorId(id);
        verify(repositorioComponente, times(1)).obtenerComponentesPorTipoEnStock("Procesador");
    }

    @Test
    void obtenerComponentesAcompararConPerifericoDeberiaRetornarPerifericosSimilares() {
        Long id = 1L;

        Periferico perifericoPrincipal = new Periferico();
        perifericoPrincipal.setId(1L);
        perifericoPrincipal.setNombre("Logitech G502");
        perifericoPrincipal.setTipoDeConexion("USB");

        Periferico perifericoSimilar = new Periferico();
        perifericoSimilar.setId(2L);
        perifericoSimilar.setNombre("Logitech G403");
        perifericoSimilar.setTipoDeConexion("USB");

        Periferico perifericoDiferente = new Periferico();
        perifericoDiferente.setId(3L);
        perifericoDiferente.setNombre("Razer DeathAdder");
        perifericoDiferente.setTipoDeConexion("USB");

        List<Componente> componentesEnStock = List.of(perifericoPrincipal, perifericoSimilar, perifericoDiferente);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(perifericoPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("Periferico")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getId(), is(equalTo(perifericoPrincipal.getId())));
        assertThat(resultado.get(1).getId(), is(equalTo(perifericoSimilar.getId())));

        assertThat(resultado, everyItem(hasProperty("id", notNullValue())));
    }

    @Test
    void obtenerComponentesAcompararConFuenteDeAlimentacionDeberiaRetornarFuentesMismosWatts() {
        Long id = 1L;

        FuenteDeAlimentacion fuentePrincipal = new FuenteDeAlimentacion();
        fuentePrincipal.setId(1L);
        fuentePrincipal.setWattsNominales("650W");

        FuenteDeAlimentacion fuenteSimilar = new FuenteDeAlimentacion();
        fuenteSimilar.setId(2L);
        fuenteSimilar.setWattsNominales("650W");

        FuenteDeAlimentacion fuenteDiferente = new FuenteDeAlimentacion();
        fuenteDiferente.setId(3L);
        fuenteDiferente.setWattsNominales("750W");

        List<Componente> componentesEnStock = List.of(fuentePrincipal, fuenteSimilar, fuenteDiferente);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(fuentePrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("FuenteDeAlimentacion")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getId(), is(equalTo(fuentePrincipal.getId())));
        assertThat(resultado.get(1).getId(), is(equalTo(fuenteSimilar.getId())));

        assertThat(resultado.stream().map(ComponenteEspecificoDto::getId).collect(Collectors.toList()),
                not(hasItem(fuenteDiferente.getId())));
    }

    @Test
    void obtenerComponentesAcompararConMemoriaRAMDeberiaRetornarMemoriasMismaCapacidadYTecnologia() {
        Long id = 1L;

        MemoriaRAM memoriaPrincipal = new MemoriaRAM();
        memoriaPrincipal.setId(1L);
        memoriaPrincipal.setCapacidad("16GB");
        memoriaPrincipal.setTecnologiaRAM("DDR4");

        MemoriaRAM memoriaSimilar = new MemoriaRAM();
        memoriaSimilar.setId(2L);
        memoriaSimilar.setCapacidad("16GB");
        memoriaSimilar.setTecnologiaRAM("DDR4");

        MemoriaRAM memoriaDiferente = new MemoriaRAM();
        memoriaDiferente.setId(3L);
        memoriaDiferente.setCapacidad("32GB");
        memoriaDiferente.setTecnologiaRAM("DDR4");

        List<Componente> componentesEnStock = List.of(memoriaPrincipal, memoriaSimilar, memoriaDiferente);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(memoriaPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("MemoriaRAM")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado, hasItem(hasProperty("id", equalTo(memoriaPrincipal.getId()))));
        assertThat(resultado, hasItem(hasProperty("id", equalTo(memoriaSimilar.getId()))));
        assertThat(resultado, not(hasItem(hasProperty("id", equalTo(memoriaDiferente.getId())))));
    }

    @Test
    void obtenerComponentesAcompararConPlacaDeVideoDeberiaRetornarPlacasMismaCapacidadRAM() {
        Long id = 1L;

        PlacaDeVideo placaPrincipal = new PlacaDeVideo();
        placaPrincipal.setId(1L);
        placaPrincipal.setCapacidadRAM("8GB");

        PlacaDeVideo placaSimilar = new PlacaDeVideo();
        placaSimilar.setId(2L);
        placaSimilar.setCapacidadRAM("8GB");

        PlacaDeVideo placaDiferente = new PlacaDeVideo();
        placaDiferente.setId(3L);
        placaDiferente.setCapacidadRAM("12GB");

        List<Componente> componentesEnStock = List.of(placaPrincipal, placaSimilar, placaDiferente);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(placaPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("PlacaDeVideo")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado.stream().map(ComponenteEspecificoDto::getId).collect(Collectors.toList()),
                containsInAnyOrder(placaPrincipal.getId(), placaSimilar.getId()));
        assertThat(resultado.stream().map(ComponenteEspecificoDto::getId).collect(Collectors.toList()),
                not(hasItem(placaDiferente.getId())));
    }

    @Test
    void obtenerComponentesAcompararConAlmacenamientoDeberiaRetornarAlmacenamientoMismaCapacidad() {
        Long id = 1L;

        Almacenamiento almacenamientoPrincipal = new Almacenamiento();
        almacenamientoPrincipal.setId(1L);
        almacenamientoPrincipal.setCapacidad("1TB");

        Almacenamiento almacenamientoSimilar = new Almacenamiento();
        almacenamientoSimilar.setId(2L);
        almacenamientoSimilar.setCapacidad("1TB");

        List<Componente> componentesEnStock = List.of(almacenamientoPrincipal, almacenamientoSimilar);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(almacenamientoPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("Almacenamiento")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado, hasItems(
                hasProperty("id", equalTo(almacenamientoPrincipal.getId())),
                hasProperty("id", equalTo(almacenamientoSimilar.getId()))
        ));
    }


    @Test
    void obtenerComponentesAcompararConMotherboardDeberiaRetornarMotherboardMismoChipset() {
        Long id = 1L;

        Motherboard motherboardPrincipal = new Motherboard();
        motherboardPrincipal.setId(1L);
        motherboardPrincipal.setChipsetPrincipal("B550");

        Socket socket = new Socket();
        socket.setNombre("AM4");
        motherboardPrincipal.setSocket(socket);

        List<Componente> componentesEnStock = List.of(motherboardPrincipal);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(motherboardPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("Motherboard")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(1));
        assertThat(resultado.get(0).getId(), is(equalTo(motherboardPrincipal.getId())));
    }

    @Test
    void obtenerComponentesAcompararConMonitorDeberiaRetornarMonitorMismoTipoPanel() {
        Long id = 1L;

        Monitor monitorPrincipal = new Monitor();
        monitorPrincipal.setId(1L);
        monitorPrincipal.setTipoDePanel("IPS");

        Monitor monitorSimilar = new Monitor();
        monitorSimilar.setId(2L);
        monitorSimilar.setTipoDePanel("IPS");

        List<Componente> componentesEnStock = List.of(monitorPrincipal, monitorSimilar);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(monitorPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("Monitor")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado, allOf(
                hasItem(hasProperty("id", equalTo(monitorPrincipal.getId()))),
                hasItem(hasProperty("id", equalTo(monitorSimilar.getId())))
        ));
    }

    @Test
    void obtenerComponentesAcompararConCoolerCPUDeberiaRetornarCoolersMismoTipoDisipacion() {
        Long id = 1L;

        CoolerCPU coolerPrincipal = new CoolerCPU();
        coolerPrincipal.setId(1L);
        coolerPrincipal.setTipoDeDisipacion("Aire");
        coolerPrincipal.setPrecio(50.0);

        CoolerCPU coolerSimilar = new CoolerCPU();
        coolerSimilar.setId(2L);
        coolerSimilar.setTipoDeDisipacion("Aire");
        coolerSimilar.setPrecio(60.0);

        CoolerCPU coolerSinPrecio = new CoolerCPU();
        coolerSinPrecio.setId(3L);
        coolerSinPrecio.setTipoDeDisipacion("Aire");
        coolerSinPrecio.setPrecio(0.0);

        List<Componente> componentesEnStock = List.of(coolerPrincipal, coolerSimilar, coolerSinPrecio);

        when(repositorioComponente.buscarComponenteConImagenesPorId(id)).thenReturn(coolerPrincipal);
        when(repositorioComponente.obtenerComponentesPorTipoEnStock("CoolerCPU")).thenReturn(componentesEnStock);

        List<ComponenteEspecificoDto> resultado = servicioProductoEspecifico.obtenerComponentesAcomparar(id);

        assertThat(resultado, hasSize(2));
        assertThat(resultado, hasItems(
                hasProperty("id", equalTo(coolerPrincipal.getId())),
                hasProperty("id", equalTo(coolerSimilar.getId()))
        ));
        assertThat(resultado, not(hasItem(hasProperty("id", equalTo(coolerSinPrecio.getId())))));
    }

}
