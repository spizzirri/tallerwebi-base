package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.ServicioItemRendimiento;
import com.tallerwebi.dominio.calendario.ServicioItemRendimientoImpl;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class ServicioCalendarioTest {

    private ServicioItemRendimiento servicioItemRendimiento;
    private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init(){
        this.repositorioCalendario = mock(RepositorioCalendario.class);
        this.servicioItemRendimiento = new ServicioItemRendimientoImpl(this.repositorioCalendario);
    }

//    @Test
//    public void queSePuedanObtenerTodosLosItems(){
//        // preparacion
//        List<Item> itemsMock = new ArrayList<>();
//        itemsMock.add(new Item(1L, TipoItem.ESPADA));
//        itemsMock.add(new Item(2L,TipoItem.ESCUDO));
//        itemsMock.add(new Item(3L,TipoItem.ESCUDO));
//        when(this.repositorioInventario.get()).thenReturn(itemsMock);
//
//        // ejecucion
//        List<DatosItem> items = this.servicioInventario.get();
//
//        // verificacion
//        assertThat(items.size(), equalTo(3)); // Existan 3 elementos
//    }
//
//    @Test
//    public void queAlBuscarItemsPorTipoItemESPADADevuelvaLosItemsCorrespondientes(){
//        // preparacion
//        List<Item> itemsMock = new ArrayList<>();
//        itemsMock.add(new Item(1L,TipoItem.ESPADA));
//        when(this.repositorioInventario.getByTipoItem(TipoItem.ESPADA)).thenReturn(itemsMock);
//
//        // ejecucion
//        List<DatosItem> items = this.servicioInventario.getByTipoItem(TipoItem.ESPADA);
//
//        // verificacion
//        assertThat(items.size(), equalTo(1)); // Existan 1 elementos
//    }
//}

}
