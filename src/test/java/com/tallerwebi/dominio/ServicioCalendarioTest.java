package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.*;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void init(){
        this.repositorioCalendario = mock(RepositorioCalendario.class);
        this.servicioCalendario = new ServicioCalendarioImpl(this.repositorioCalendario);
    }

    @Test
    public void queSePuedanObtenerTodosLosItemsRendimiento(){
        // ejecucion
        List<DatosItemRendimiento> itemsRendimientos = this.servicioCalendario.obtenerItems();

        // verificacion
        assertThat(itemsRendimientos.size(), equalTo(3)); // Existan 3 elementos
    }

    @Test
    public void queAlBuscarItemRendimientoPorTipoRendimientoNormalDevuelvaLosItemsCorrespondientes(){
        // preparacion
        LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual
        List<ItemRendimiento> itemsMock = new ArrayList<>();
        itemsMock.add(new ItemRendimiento(fechaActual,TipoRendimiento.NORMAL));
        when(this.repositorioCalendario.buscarPorTipoRendimiento(TipoRendimiento.NORMAL)).thenReturn(itemsMock);

        // ejecucion
        List<DatosItemRendimiento> items = this.servicioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL);

        // verificacion
        assertThat(items.size(), equalTo(1)); // Existan 1 elementos
    }
}


