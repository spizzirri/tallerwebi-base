package com.tallerwebi.dominio;


import com.tallerwebi.infraestructura.ServicioViajeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;

public class ServicioViajeTest {

    private RepositorioViaje repositorioViaje;
    private ServicioViaje servicioViaje;
    public static final Integer CANTIDAD_VIAJES = 4;

    @BeforeEach
    public void init(){
        this.repositorioViaje = mock(RepositorioViaje.class);
        this.servicioViaje = new ServicioViajeImpl(this.repositorioViaje);
    }
    @Test
    public void queSeObtenganTodosLosViajes(){
        List <Viaje> viajes = generarViajes(CANTIDAD_VIAJES);
        when(this.repositorioViaje.listarViajes()).thenReturn(viajes);

        List <Viaje> viajesObtenidos = this.servicioViaje.obtenerViajes();

        assertThat(viajesObtenidos,not(empty()));
        assertThat(viajesObtenidos.size(), is(CANTIDAD_VIAJES) );

    }

    @Test
    public void queSePuedanBuscarViajesPorDestino(){
        List <Viaje> viajesEsperados = generarViajes(CANTIDAD_VIAJES);
        when(this.repositorioViaje.buscarPorDestino(anyString())).thenReturn(viajesEsperados);

        List <Viaje> viajesObtenidos = this.servicioViaje.obtenerViajesPorDestino("Tucuman");

        assertThat(viajesObtenidos,not(empty()));
        assertThat(viajesObtenidos.size(), is(CANTIDAD_VIAJES) );

        for (Viaje viaje: viajesObtenidos) {
            assertThat(viaje.getDestino(),equalTo("Tucuman"));
        }

    }

    @Test
    public void queSePuedanBuscarViajesPorOrigen(){
        List <Viaje> viajesEsperados = generarViajes(CANTIDAD_VIAJES);
        when(this.repositorioViaje.buscarPorOrigen(anyString())).thenReturn(viajesEsperados);

        List <Viaje> viajesObtenidos = this.servicioViaje.obtenerViajesPorOrigen("Buenos Aires");

        assertThat(viajesObtenidos,not(empty()));
        assertThat(viajesObtenidos.size(), is(CANTIDAD_VIAJES) );

        for (Viaje viaje: viajesObtenidos) {
            assertThat(viaje.getOrigen(),equalTo("Buenos Aires"));
        }

    }

    @Test
    public void queSePuedanBuscarViajesPorFecha(){
        List <Viaje> viajesEsperados = new ArrayList();
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));
        when(this.repositorioViaje.buscarPorFecha(anyString())).thenReturn(viajesEsperados);

        List <Viaje> viajesObtenidos = this.servicioViaje.obtenerViajesPorFecha(LocalDateTime.now().withSecond(0).withNano(0).toString());

        assertThat(viajesObtenidos,not(empty()));
        assertThat(viajesObtenidos.size(), is(3) );

        for (Viaje viaje: viajesObtenidos) {
            assertThat(viaje.getFecha_hora(),equalTo(viajesEsperados.get(0).getFecha_hora()));
        }
    }

    @Test
    public void queSePuedanBuscarViajesPorMultiplesFiltros(){

        List <Viaje> viajesEsperados = new ArrayList();
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));
        viajesEsperados.add(new  Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().withSecond(0).withNano(0).toString(), 2, "probando", 1L));

        when(this.repositorioViaje.buscarPorOrigenDestinoYfecha(anyString(),anyString(),anyString())).thenReturn(viajesEsperados);

        List <Viaje> viajesObtenidos = this.servicioViaje.obtenerViajesPorFiltroMultiple("Buenos Aires","Tucuman",LocalDateTime.now().withSecond(0).withNano(0).toString());

        assertThat(viajesObtenidos,not(empty()));
        assertThat(viajesObtenidos.size(), is(3) );

        for (Viaje viaje: viajesObtenidos) {
            assertThat(viaje.getOrigen(),equalTo(viajesEsperados.get(0).getOrigen()));
            assertThat(viaje.getDestino(),equalTo(viajesEsperados.get(0).getDestino()));
            assertThat(viaje.getFecha_hora(),equalTo(viajesEsperados.get(0).getFecha_hora()));
        }
    }

    private List<Viaje> generarViajes(int cantidadDeseada) {
        List <Viaje>   viajes = new ArrayList<>();
        for( int i=1; i<=cantidadDeseada; i++){
            viajes.add(crearViaje((long)i));
        }
        return viajes;
    }

    private Viaje crearViaje(Long id) {
        Usuario usuario = new Usuario(id);
        return new  Viaje(id,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando", usuario.getId());
    }
}
