package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.DatosItemRendimiento;
import com.tallerwebi.dominio.calendario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Locale;

import com.tallerwebi.dominio.calendario.TipoRendimiento;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioCalendarioTest {

    private ServicioCalendario servicioCalendario;
    private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init() {
        this.repositorioCalendario = mock(RepositorioCalendario.class);
        this.servicioCalendario = new ServicioCalendarioImpl(this.repositorioCalendario);
    }

    @Test
    public void queSePuedanObtenerTodosLosItemsRendimiento() {
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        when(this.repositorioCalendario.obtenerItemsRendimiento()).thenReturn(itemsMock);

        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItemsRendimiento();

        assertThat(itemsRendimientos.size(), equalTo(3));
    }

    @Test

    public void queAlBuscarItemRendimientoPorTipoRendimientoNormalDevuelvaLosItemsCorrespondientes() {
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(TipoRendimiento.NORMAL));
        when(this.repositorioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL)).thenReturn(itemsMock);

        List<DatosItemRendimiento> items = this.servicioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL);

        assertThat(items.size(), equalTo(1));
    }

    @Test
    public void queSePuedaGuardarItemRendimiento() {
        ItemRendimiento itemRendimientoMock = new ItemRendimiento(TipoRendimiento.NORMAL);

        this.servicioCalendario.guardarItemRendimiento(itemRendimientoMock);

        verify(this.repositorioCalendario).guardar(itemRendimientoMock);
    }

    public void dadoQueElUsuarioGuardaUnItemRendimientoQueSePuedaGuardarItemRendimiento() {
        // preparacion
        TipoRendimiento tipoRendimiento = TipoRendimiento.NORMAL;
        ItemRendimiento itemRendimientoMock = new ItemRendimiento();
        itemRendimientoMock.setTipoRendimiento(tipoRendimiento);

        when(repositorioCalendario.existeItemRendimientoPorFecha(any(LocalDate.class))).thenReturn(false);

        // ejecucion
        servicioCalendario.guardarItemRendimiento(itemRendimientoMock);

        // verificacion
        verify(repositorioCalendario).guardar(itemRendimientoMock);
        assertEquals(LocalDate.now(), itemRendimientoMock.getFecha());
        assertEquals(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")), itemRendimientoMock.getDiaNombre());

    }

    @Test
    public void queSePuedaObtenerTipoRendimientoMasSeleccionado() {
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(15), TipoRendimiento.ALTO);
        when(this.repositorioCalendario.obtenerItemMasSeleccionado()).thenReturn(itemRendimiento1);

        DatosItemRendimiento itemMasSeleccionado = this.servicioCalendario.obtenerItemMasSeleccionado();

        assertThat(itemMasSeleccionado.getTipoRendimiento(), equalTo(TipoRendimiento.ALTO));
    }

    @Test
    public void queSePuedaActualizarItemRendimiento() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(LocalDate.now(), TipoRendimiento.NORMAL);

        this.servicioCalendario.actualizarItemRendimiento(itemRendimiento);

        verify(this.repositorioCalendario).guardar(itemRendimiento);
    }

    @Test
    public void queSePuedaEliminarItemRendimiento() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(LocalDate.now(), TipoRendimiento.NORMAL);

        this.servicioCalendario.eliminarItemRendimiento(itemRendimiento);

        verify(this.repositorioCalendario).eliminar(itemRendimiento);
    }

    @Test
    public void queSePuedanObtenerOpcionesRendimiento() {
        List<TipoRendimiento> opciones = this.servicioCalendario.obtenerOpcionesRendimiento();

        assertThat(opciones.size(), equalTo(4));
        assertThat(opciones, equalTo(Arrays.asList(TipoRendimiento.ALTO, TipoRendimiento.NORMAL, TipoRendimiento.BAJO, TipoRendimiento.DESCANSO)));
    }

    @Test
    public void queSePuedaVaciarElCalendario() {
        this.servicioCalendario.vaciarCalendario();

        verify(this.repositorioCalendario).vaciarCalendario();
    }

}

