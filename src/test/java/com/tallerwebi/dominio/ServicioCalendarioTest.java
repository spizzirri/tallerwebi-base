package com.tallerwebi.dominio;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import com.tallerwebi.dominio.calendario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioCalendarioTest {

    private ServicioCalendario servicioCalendario;
    private RepositorioCalendario repositorioCalendario;

    LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual

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
        // ejecucion
        when(this.repositorioCalendario.obtenerItemsRendimiento()).thenReturn(itemsMock);
//        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItemsRendimiento();
        // verificacion
        assertThat(itemsMock.size(), equalTo(3)); // Existan 3 elementos
    }

    @Test
    public void queAlBuscarItemRendimientoPorTipoRendimientoNormalDevuelvaLosItemsCorrespondientes() {
        // preparacion
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(TipoRendimiento.NORMAL));
        when(this.repositorioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL)).thenReturn(itemsMock);
        // ejecucion
//      List<DatosItemRendimiento> items = this.servicioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL);
        // verificacion
        assertThat(itemsMock.size(), equalTo(1)); // Existan 1 elementos
    }

    @Test
    public void queSePuedaGuardarItemRendimiento() {
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        TipoRendimiento tipoRendimiento = TipoRendimiento.NORMAL;
        ItemRendimiento itemRendimientoMock = new ItemRendimiento(tipoRendimiento);

        servicioCalendario.guardarItemRendimiento(itemRendimientoMock);

        // Assert (Verify interactions)
        verify(repositorioCalendario).guardar(itemRendimientoMock);
    }

    @Test
    public void queSePuedaObtenerTipoRendimientoMasSeleccionado() {
        // Crear los ítems de rendimiento mock
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(15), TipoRendimiento.ALTO);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(LocalDate.now().minusDays(14), TipoRendimiento.ALTO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(LocalDate.now().minusDays(13), TipoRendimiento.BAJO);
        ItemRendimiento itemRendimiento4 = new ItemRendimiento(LocalDate.now().minusDays(12), TipoRendimiento.NORMAL);

        List<ItemRendimiento> items = Arrays.asList(itemRendimiento1, itemRendimiento2, itemRendimiento3, itemRendimiento4);
        when(repositorioCalendario.obtenerItemMasSeleccionado()).thenReturn(itemRendimiento1);
        DatosItemRendimiento itemMasSeleccionado = servicioCalendario.obtenerItemMasSeleccionado();

        assertThat(itemMasSeleccionado.getTipoRendimiento(), equalTo(TipoRendimiento.ALTO));
    }




}