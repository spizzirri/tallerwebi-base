package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.Viaje;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes={SpringWebTestConfig.class, HibernateTestConfig.class})

public class RepositorioViajeTest {

    @Autowired
    private RepositorioViaje repositorio;

    @Transactional
    @Rollback
    @Test
    public void queSePuedaCrearUnViajeYseGuarde(){
        //preparacion

         Viaje viaje = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando");
        //ejecucion
        repositorio.guardar(viaje);

        Viaje busqueda = repositorio.buscarPorId(viaje.getId());
        //validacion
        assertThat(busqueda , is(notNullValue()));
    }

    @Transactional
    @Rollback
    @Test
    public void queSePuedaBuscarViajesPorDestino(){
        //preparacion
        Viaje viaje = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando");
        Viaje viaje2 = new Viaje(1L,"Buenos Aires", "Bariloche", LocalDateTime.now().toString(), 3, "probando");
        Viaje viaje3 = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 4, "probando");

        //ejecucion
        repositorio.guardar(viaje);
        repositorio.guardar(viaje2);
        repositorio.guardar(viaje3);

        List<Viaje> viajesBuscados = repositorio.buscarPorDestino(viaje.getDestino());

        //validacion
        assertThat(viajesBuscados , is(notNullValue()));
        for(Viaje viajeListado : viajesBuscados){
            assertThat(viajeListado.getDestino(), equalTo(viaje.getDestino()));
        }

    }
    @Transactional
    @Rollback
    @Test
    public void queSePuedaModificarElDestinoDeUnViaje(){
        //preparacion
        Viaje viaje = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando");

        //ejecucion
        repositorio.guardar(viaje);

        String nuevoDestino = "Salta";
        viaje.setDestino(nuevoDestino);
        repositorio.actualizar(viaje);

        Viaje viajeModificado = repositorio.buscarPorId(viaje.getId());

        //validacion
        assertThat(viajeModificado , is(notNullValue()));
        assertThat(viajeModificado.getDestino(), equalTo(nuevoDestino));


    }

    @Transactional
    @Rollback
    @Test
    public void queSePuedanListarLosViajes(){

        Viaje viaje1 = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando1");
        Viaje viaje2 = new Viaje(2L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando2");
        Viaje viaje3 = new Viaje(3L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando3");
        //ejecucion
        repositorio.guardar(viaje1);
        repositorio.guardar(viaje2);
        repositorio.guardar(viaje3);

        List<Viaje> busqueda = repositorio.listarViajes();
        //validacion
        assertThat(busqueda , is(notNullValue()));
        assertThat(busqueda.size(), equalTo(3));
    }

    @Transactional
    @Rollback
    @Test
    public void queSePuedaBuscarViajePorOrigen(){

        Viaje viaje1 = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando1");
        Viaje viaje2 = new Viaje(2L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando2");
        Viaje viaje3 = new Viaje(3L,"Entre Rios", "Tucuman", LocalDateTime.now().toString(), 2, "probando3");
        //ejecucion
        repositorio.guardar(viaje1);
        repositorio.guardar(viaje2);
        repositorio.guardar(viaje3);

         List<Viaje> busqueda = repositorio.buscarPorOrigen(viaje3.getOrigen());
        //validacion
        assertThat(busqueda , is(notNullValue()));
        assertThat(busqueda.size(), equalTo(1));

        busqueda = repositorio.buscarPorOrigen(viaje2.getOrigen());

        assertThat(busqueda , is(notNullValue()));
        assertThat(busqueda.size(), equalTo(2));
    }
}
