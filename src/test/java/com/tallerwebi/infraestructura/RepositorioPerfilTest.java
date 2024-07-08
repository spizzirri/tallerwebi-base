package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.RepositorioPerfil;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioPerfilTest {

    private RepositorioPerfil repositorioPerfil;

    @Autowired
    private SessionFactory sessionFactory;

    @BeforeEach
    public void init(){
        this.repositorioPerfil = new RepositorioPerfilImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUnPerfilEnLaBaseDeDatosQueSeLogreObtenerPorIdPerfil() {
        // preparación
        Perfil perfilGuardado = dadoQueExisteUnPerfilEnLaBaseDeDatos();

        // ejecución
        Perfil perfilObtenido = repositorioPerfil.obtenerPerfilPorId(perfilGuardado.getIdPerfil());

        // verificación
        assertNotNull(perfilGuardado);
        assertEquals(30, perfilObtenido.getEdad());
        assertEquals(70.0, perfilObtenido.getPeso());
    }

    private Perfil dadoQueExisteUnPerfilEnLaBaseDeDatos() {
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPeso(70.0);
        perfil.setAltura(175);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("Ganancia Muscular");
        perfil.setCondicionesAlternas("Ninguna");
        perfil.setExperienciaEjercicio("Intermedio");
        perfil.setSuplementos("Proteína");

        this.repositorioPerfil.guardarPerfil(perfil);

        return  perfil;
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUnPerfilEnLaBaseDeDatosQueSePuedaActualizarloYVerificarSusCambios() {
        // preparación
        Perfil perfilActualizado = dadoQueExisteUnPerfilEnLaBaseDeDatos();

        perfilActualizado.setPeso(75.0);
        perfilActualizado.setAltura(180);

        // ejecución
        repositorioPerfil.actualizarPerfil(perfilActualizado);

        // verificación
        assertEquals(75.0, perfilActualizado.getPeso());
        assertEquals(180, perfilActualizado.getAltura());
    }

    @Test
    @Transactional
    @Rollback
    public void queSeLogreGuardarUnPerfilEnLaBaseDeDatos() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPeso(70.0);
        perfil.setAltura(175);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("Ganancia Muscular");
        perfil.setCondicionesAlternas("Ninguna");
        perfil.setExperienciaEjercicio("Intermedio");
        perfil.setSuplementos("Proteína");

        // ejecución
        this.repositorioPerfil.guardarPerfil(perfil);

        // verificación
        assertNotNull(perfil.getIdPerfil());
        Session session = sessionFactory.getCurrentSession();
        Perfil perfilGuardado = (Perfil) session.createSQLQuery("SELECT * FROM Perfil WHERE idPerfil = :idPerfil")
                .addEntity(Perfil.class)
                .setParameter("idPerfil", perfil.getIdPerfil())
                .uniqueResult();

        assertNotNull(perfilGuardado);
        assertEquals(30, perfilGuardado.getEdad());
        assertEquals(70.0, perfilGuardado.getPeso());

    }

}
