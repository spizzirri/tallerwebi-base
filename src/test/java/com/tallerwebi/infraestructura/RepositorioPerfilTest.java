package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.RepositorioPerfil;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

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
    public void queSeLogreGuardarPerfilYBuscarloPorIdPerfil() {
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPeso(70.0);
        perfil.setAltura(175);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("Ganancia Muscular");
        perfil.setCondicionesAlternas("Ninguna");
        perfil.setExperienciaEjercicio("Intermedio");
        perfil.setSuplementos("Proteína");

        repositorioPerfil.guardarPerfil(perfil);

        assertNotNull(perfil.getIdPerfil());

        // Obtener el perfil de la base de datos y verificar los atributos
        Perfil perfilGuardado = repositorioPerfil.obtenerPerfilPorId(perfil.getIdPerfil());
        assertNotNull(perfilGuardado);
        assertEquals(30, perfilGuardado.getEdad());
        assertEquals(70.0, perfilGuardado.getPeso());
    }

    @Test
    @Transactional
    @Rollback
    public void queSeLogreActualizarPerfil() {
        // Crear y guardar un perfil inicial
        Perfil perfilInicial = new Perfil();
        perfilInicial.setEdad(30);
        perfilInicial.setPeso(70.0);
        perfilInicial.setAltura(175);
        perfilInicial.setGenero("Masculino");
        perfilInicial.setObjetivoFitness("Ganancia Muscular");
        perfilInicial.setCondicionesAlternas("Ninguna");
        perfilInicial.setExperienciaEjercicio("Intermedio");
        perfilInicial.setSuplementos("Proteína");

        repositorioPerfil.guardarPerfil(perfilInicial);

        // Modificar algunos atributos del perfil
        perfilInicial.setPeso(75.0);
        perfilInicial.setAltura(180);

        // Actualizar el perfil en la base de datos
        repositorioPerfil.actualizarPerfil(perfilInicial);

        // Obtener el perfil actualizado de la base de datos
        Perfil perfilActualizado = repositorioPerfil.obtenerPerfilPorId(perfilInicial.getIdPerfil());

        // Verificar que los cambios se hayan guardado correctamente
        assertEquals(75.0, perfilActualizado.getPeso());
        assertEquals(180, perfilActualizado.getAltura());

    }


}
