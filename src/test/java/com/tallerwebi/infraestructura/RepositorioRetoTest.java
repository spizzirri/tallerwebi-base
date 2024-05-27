package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import com.tallerwebi.dominio.reto.RepositorioReto;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioRetoTest {

    private RepositorioReto repositorioReto;

    @Autowired
    private SessionFactory sessionFactory;

    @BeforeEach
    public void init(){
        this.repositorioReto = new RepositorioRetoImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    public void deberiaObtenerReto() {
        // Ejecutar el método para obtener un reto
        Reto reto = repositorioReto.obtenerReto();

        // Verificar que el reto no sea nulo
        assertNotNull(reto, "El reto obtenido no debería ser nulo");
    }

    @Test
    @Transactional
    public void queAlEmpezarUnRetoSeActualiceSuCondicionDeSeleccionado() {
        Reto retoObtenido = repositorioReto.obtenerReto();
        repositorioReto.empezarReto(retoObtenido.getId());
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertTrue(retoObtenido.isSeleccionado(), "El reto debería estar marcado como seleccionado");

        // Verificar que no se puede obtener el mismo reto nuevamente
        Reto otroRetoObtenido = repositorioReto.obtenerReto();
        assertNotEquals(retoObtenido.getId(), otroRetoObtenido.getId(), "No se debería obtener el mismo reto dos veces");

        // Verificar que el reto fue actualizado en la base de datos
        Reto retoActualizado = (Reto) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Reto WHERE id = :id")
                .setParameter("id", retoObtenido.getId())
                .getSingleResult();
        assertThat(retoActualizado.isSeleccionado(), equalTo(true));
    }


}
