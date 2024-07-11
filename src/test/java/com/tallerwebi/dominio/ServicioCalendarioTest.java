package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import com.tallerwebi.dominio.calendario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

import com.tallerwebi.dominio.calendario.TipoRendimiento;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioCalendarioTest {

    private ServicioCalendario servicioCalendario;
    private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init() {
        this.repositorioCalendario = mock(RepositorioCalendario.class);
        this.servicioCalendario = new ServicioCalendarioImpl(this.repositorioCalendario);
    }

    public List<ItemRendimiento> dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatos() {
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));

        return itemsMock;
    }

    @Test
    public void dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatosQueSePuedanObtenerTodosLosItemsRendimiento() {
        // preparacion
        List<ItemRendimiento> itemsMock = dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatos();

        when(this.repositorioCalendario.obtenerItemsRendimiento()).thenReturn(itemsMock);

        // ejecucion
        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItemsRendimiento();

        // verificacion
        assertThat(itemsRendimientos.size(), equalTo(3));
    }

    @Test
    public void dadoQueExisteElEspacioQueSePuedaGuardarItemRendimientoEnLaBaseDeDatos() {
        // preparacion
        ItemRendimiento itemRendimientoMock = new ItemRendimiento(TipoRendimiento.NORMAL);

        // ejecucion
        this.servicioCalendario.guardarItemRendimiento(itemRendimientoMock);

        // verificacion
        verify(this.repositorioCalendario).guardar(itemRendimientoMock);
    }


    @Test
    public void queSePuedaObtenerTipoRendimientoMasSeleccionado() {
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(15), TipoRendimiento.ALTO);
        when(this.repositorioCalendario.obtenerItemMasSeleccionado()).thenReturn(itemRendimiento1);

        DatosItemRendimiento itemMasSeleccionado = this.servicioCalendario.obtenerItemMasSeleccionado();

        assertThat(itemMasSeleccionado.getTipoRendimiento(), equalTo(TipoRendimiento.ALTO));
    }

    @Test
    public void dadoQueNoExistenItemsRendimientoGuardadosEnLaBaseDeDatosQueSeRetorneUnaListaVacia() {
        // preparación
        when(this.repositorioCalendario.obtenerItemsRendimiento()).thenReturn(Collections.emptyList());

        // ejecución
        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItemsRendimiento();

        // verificación
        assertTrue(itemsRendimientos.isEmpty());
    }

    @Test
    public void dadoQueElUsuarioIntentaGuardarUnItemRendimientoDuplicadoEntoncesSeLanzaUnaExcepcion() {
        // preparación
        ItemRendimiento itemRendimientoMock = new ItemRendimiento(TipoRendimiento.NORMAL);
        when(repositorioCalendario.existeItemRendimientoPorFecha(any(LocalDate.class))).thenReturn(true);

        // ejecución y verificación
        assertThrows(ItemRendimientoDuplicadoException.class, () -> {
            servicioCalendario.guardarItemRendimiento(itemRendimientoMock);
        });
    }

    @Test
    public void dadoQueNoExistenItemsRendimientoQueSeRetorneNullAlObtenerItemMasSeleccionado() {
        // preparación
        when(this.repositorioCalendario.obtenerItemMasSeleccionado()).thenReturn(null);

        // ejecución
        DatosItemRendimiento itemMasSeleccionado = this.servicioCalendario.obtenerItemMasSeleccionado();

        // verificación
        assertNull(itemMasSeleccionado);
    }

}

