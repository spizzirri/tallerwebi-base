package com.tallerwebi.dominio;

import com.tallerwebi.dominio.mascotas.Mascota;
import com.tallerwebi.dominio.mascotas.RepositorioMascota;
import com.tallerwebi.dominio.mascotas.ServicioMascota;
import com.tallerwebi.dominio.mascotas.ServicioMascotaImpl;

import com.tallerwebi.infraestructura.RepositorioMascotaImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicioMascotaTest {
    public static final int CANTIDAD_MASCOTAS = 4;
    public static final long NUMERO_UNO = 1L;

    ServicioMascota servicioMascota;
    RepositorioMascota repositorioMascota;

    @BeforeEach
    public void init(){
        this.repositorioMascota = mock(RepositorioMascota.class);
        this.servicioMascota  = new ServicioMascotaImpl(this.repositorioMascota);
    }

    @Test
    public void cuandoListoLasMascotasObtengoCuatroMascotas(){
        // Listar todas las mascotas
        // Obtener una mascota en particular
        // Guardar una mascota

        // preparacion
        List<Mascota> mascotas = generarMascotas(4);
        when(this.repositorioMascota.obtener()).thenReturn(mascotas);

        // ejecucion
        List<Mascota> mascotasObtenidas = this.servicioMascota.obtener();

        // verificacion
        // La lista no debe estar vacia
        // La cantidad tiene que ser 4

        assertThat(mascotasObtenidas, not(empty()));
        assertThat(mascotasObtenidas.size(), is(CANTIDAD_MASCOTAS));
    }

    @Test
    public void cuandoObtengoUnaMascotaPorSuIdObtengoLaMascota(){
        // preparacion
        when(this.repositorioMascota.obtener(NUMERO_UNO)).thenReturn(crearMascota(NUMERO_UNO));

        // ejecucion
        Mascota mascotaObtenida = this.servicioMascota.obtener(NUMERO_UNO);

        // verificacion
        assertThat(mascotaObtenida.getId(), is(NUMERO_UNO));
    }

    private static Mascota crearMascota(Long id) {
        return new Mascota(id);
    }

    private static List<Mascota> generarMascotas(Integer cantidad) {
        List<Mascota> mascotas = new ArrayList<>();
        for(int i = 0; i < cantidad; i++){
            mascotas.add(crearMascota((long)i));
        }
        return mascotas;
    }
}
