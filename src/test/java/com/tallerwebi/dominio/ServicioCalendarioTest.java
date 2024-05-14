package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicioCalendarioTest {

    private ServicioCalendario servicioCalendario;
    private RepositorioCalendario repositorioCalendario;
    LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual
    @BeforeEach
    public void init(){
        this.repositorioCalendario = mock(RepositorioCalendario.class);
        this.servicioCalendario = new ServicioCalendarioImpl(this.repositorioCalendario);
    }

    @Test
    public void queSePuedanObtenerTodosLosItemsRendimiento(){
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
        itemsMock.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
        // ejecucion
        when(this.repositorioCalendario.obtenerItemsRendimiento()).thenReturn(itemsMock);
//        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItemsRendimiento();
        // verificacion
        assertThat(itemsRendimientos.size(), equalTo(3)); // Existan 3 elementos
    }

    @Test
    public void queAlBuscarItemRendimientoPorTipoRendimientoNormalDevuelvaLosItemsCorrespondientes(){
        // preparacion
        LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(fechaActual,TipoRendimiento.NORMAL));
        when(this.repositorioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL)).thenReturn(itemsMock);

        // ejecucion
        List<DatosItemRendimiento> items = this.servicioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL);

        // verificacion
        assertThat(items.size(), equalTo(1)); // Existan 1 elementos
    }


    @Test
    public void testGuardarItemRendimiento() {
        LocalDate fecha = LocalDate.of(2024, 5, 15);
        TipoRendimiento tipoRendimiento = TipoRendimiento.NORMAL;
        ItemRendimiento itemRendimiento = new ItemRendimiento(1L,fecha, tipoRendimiento);

        // Mock the repository to verify method calls
        RepositorioCalendario mockRepositorio = Mockito.mock(RepositorioCalendario.class);
        this.servicioCalendario.setRepositorioCalendario(mockRepositorio);

        // Call the method to save the item
        List<DatosItemRendimiento> itemsObtenidos = this.servicioCalendario.guardarItemRendimiento(itemRendimiento);

        // Verify that the repository's guardar method was called
        Mockito.verify(mockRepositorio).guardar(itemRendimiento);

        // Verify that the repository's obtenerItemsRendimiento method was called
        Mockito.verify(mockRepositorio).obtenerItemsRendimiento();

        // Assert the size of the returned list (should be 1 after saving)
        assertThat(itemsObtenidos.size(), equalTo(1));

        // Assert the content of the first item (assuming conversion works as expected)
        DatosItemRendimiento primerItem = itemsObtenidos.get(0);
        assertThat(primerItem.getFecha(), equalTo(fecha));
        assertThat(primerItem.getTipoRendimiento(), equalTo(tipoRendimiento));
    }


}