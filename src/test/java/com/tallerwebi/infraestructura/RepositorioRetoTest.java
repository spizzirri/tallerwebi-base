package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import com.tallerwebi.dominio.reto.RepositorioReto;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static javax.management.Query.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertTrue(retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");

        // Verificar que no se puede obtener el mismo reto nuevamente
        Reto otroRetoObtenido = repositorioReto.obtenerReto();
        assertNotEquals(retoObtenido.getId(), otroRetoObtenido.getId(), "No se debería obtener el mismo reto dos veces");

        // Verificar que el reto fue actualizado en la base de datos
        Reto retoActualizado = (Reto) this.sessionFactory.getCurrentSession()
                .createQuery("FROM Reto WHERE id = :id")
                .setParameter("id", retoObtenido.getId())
                .getSingleResult();
        assertThat(retoActualizado.getSeleccionado(), equalTo(true));
    }

    @Test
    @Transactional
    public void queObtenerRetoPorIdDevuelvaElRetoCorrecto() {
        Long retoId = 1l;
        // Obtener el reto por ID
        Reto retoObtenido = repositorioReto.obtenerRetoPorId(retoId);
        // Verificar que el ID y el estado seleccionado sean correctos
        assertThat(retoObtenido.getId(), equalTo( retoId));
        assertThat(retoObtenido.getSeleccionado(), equalTo(true));
    }

    @Test
    @Transactional
    public void queObtenerRetoEnProcesoDevuelvaElRetoCorrecto() {
        // Crear un objeto Reto y establecerlo como seleccionado y en proceso
        Reto retoEnProceso = new Reto();
        retoEnProceso.setNombre("Reto de Ejemplo");
        retoEnProceso.setDescripcion("Descripción del reto de ejemplo");
        retoEnProceso.setImagenUrl("img/reto/burpees.jpg");
        retoEnProceso.setSeleccionado(true);
        retoEnProceso.setEnProceso(true);
        // Guardar el reto en la base de datos (si es necesario)
        sessionFactory.getCurrentSession().save(retoEnProceso);

        // Obtener el reto en proceso
        Reto retoObtenido = repositorioReto.obtenerRetoEnProceso();

        // Verificar que el reto obtenido no sea nulo y esté en proceso
        assertNotNull(retoObtenido, "El reto en proceso no debería ser nulo");
        assertTrue(retoObtenido.getSeleccionado(), "El reto en proceso debería estar seleccionado");
        assertTrue(retoObtenido.getEnProceso(), "El reto en proceso debería estar en proceso");
    }




}
