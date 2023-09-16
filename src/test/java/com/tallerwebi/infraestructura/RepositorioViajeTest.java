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
import java.time.format.DateTimeFormatter;
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
    public void queSePuedaBuscarViajesPorFecha(){
        //preparacion

        Viaje viaje = new Viaje(1L,"Buenos Aires", "Tucuman", LocalDateTime.now().toString(), 2, "probando");
        Viaje viaje2 = new Viaje(1L,"Buenos Aires", "Tucuman","20/10/2023 14:05:00", 2, "probando");
        //ejecucion
        repositorio.guardar(viaje);
        repositorio.guardar(viaje2);

        List <Viaje> busqueda = repositorio.buscarPorFecha(viaje2.getFecha_hora());
        //validacion
        assertThat( busqueda , is(hasSize(1)));
    }
}
