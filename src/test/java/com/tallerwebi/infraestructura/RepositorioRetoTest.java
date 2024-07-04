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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import com.tallerwebi.dominio.reto.RepositorioReto;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static javax.management.Query.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class) //Esta anotación principal se encarga de garantizar que el contexto para la db se instancie correctamente
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class}) //
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
    @Rollback
    public void queSePuedaGuardarUnRetoEnLaBaseDeDatosYVerificarQueEsteGuardadoConSusAtributosIguales() {
        // preparacion
        LocalDate fecha = LocalDate.now();
        Reto reto = new Reto("nombre","descripcion", "descripcion", fecha);

        // ejecucion
        repositorioReto.guardarReto(reto);

        Session session = sessionFactory.getCurrentSession();
        Query<Reto> queryActualizado = session.createQuery("FROM Reto WHERE id = :id", Reto.class);
        queryActualizado.setParameter("id", reto.getId());
        Reto retoGuardado = queryActualizado.getSingleResult();

        // verificacion
        assertThat(reto,equalTo(retoGuardado));
        assertNotNull(retoGuardado);
        assertEquals("nombre", retoGuardado.getNombre());
        assertEquals("descripcion", retoGuardado.getDescripcion());
    }


    @Test
    @Transactional
    @Rollback
    public void queAlObtenerRetoDisponibleSeObtengaUnRetoConSuAtributoSeleccionadoEnFalse() {
        Reto reto = repositorioReto.obtenerRetoDisponible();
        assertNotNull(reto, "El método obtenerRetoDisponible no debe devolver null.");
        assertFalse(reto.getSeleccionado(), "El reto devuelto debe tener seleccionado en false.");
    }

    private Reto dadoQueExistenRetosEnLaBaseDeDatos() {
        LocalDate fecha = LocalDate.now();
        Reto retoGuardado = new Reto("nombre","descripcion", "descripcion", fecha);
        this.repositorioReto.guardarReto(retoGuardado);

        return  retoGuardado;
    }

    @Test
    @Transactional
    @Rollback
    public void queAlActualizarUnRetoConSeleccionadoEnFalseYEnProcesoEnFalseSePuedanCambiarSusAtributosYQuedenGuardadosDadoQueYaHayRetosCargadosEnLaBaseDeDatos() {
        // preparacion
        Reto reto = dadoQueExistenRetosEnLaBaseDeDatos();

        Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE seleccionado = false ORDER BY rand()";
        Query<Reto> query = session.createQuery(hql, Reto.class).setMaxResults(1);
        reto = query.uniqueResult();

        reto.setSeleccionado(true);
        reto.setEnProceso(true);

        // ejecucion
        repositorioReto.actualizarReto(reto);

        Query<Reto> queryActualizado = session.createQuery("FROM Reto WHERE id = :id", Reto.class);
        queryActualizado.setParameter("id", reto.getId());
        Reto retoEnLaBd = queryActualizado.getSingleResult();

        // verificacion
        assertNotNull(retoEnLaBd, "El reto guardado no debe ser null.");
        assertTrue(retoEnLaBd.getSeleccionado(), "El reto guardado debe ser seleccionado.");
        assertTrue(retoEnLaBd.getEnProceso(), "El reto guardado debe estar en true enProceso.");
    }


    @Test
    @Transactional
    @Rollback
    public void dadoQueExistenRetosEnLaBaseDeDatosSePuedaObetenerUnoEspecificoPorId() {
        // preparacion
        Reto reto = dadoQueExistenRetosEnLaBaseDeDatos();
        // ejecucion
        Reto retoObtenido = repositorioReto.obtenerRetoPorId(reto.getId());
        // verificacion
        assertThat(retoObtenido.getId(), equalTo(reto.getId()));
    }

    private Reto dadoQueExistenRetosEnProcesoEnLaBaseDeDatos() {
        LocalDate fecha = LocalDate.now();
        Reto retoEnProceso = new Reto("nombre","descripcion", "descripcion", fecha);
        retoEnProceso.setSeleccionado(true);
        retoEnProceso.setEnProceso(true);
        this.repositorioReto.guardarReto(retoEnProceso);

        return  retoEnProceso;
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExistenRetosEnProcesoEnLaBaseDeDatosQueAlObtenerUnRetoEnProcesoDevuelvaElRetoAdecuado() {
        // preparacion
        Reto retoEnProceso = dadoQueExistenRetosEnProcesoEnLaBaseDeDatos();

        // ejecucion
        Reto retoObtenido = repositorioReto.obtenerRetoEnProceso();

        // verificacion
        assertThat(retoEnProceso,equalTo(retoObtenido));
        assertNotNull(retoObtenido, "El reto en proceso no debería ser nulo");
        assertTrue(retoObtenido.getSeleccionado(), "El reto en proceso debería estar seleccionado");
        assertTrue(retoObtenido.getEnProceso(), "El reto en proceso debería estar en proceso");
    }

    private Reto dadoQueExistenRetosEnProcesoEnLaBaseDeDatosParaTerminar() {
        LocalDate fecha = LocalDate.now();
        Reto retoEnProceso = new Reto("nombre","descripcion", "descripcion", fecha);
        retoEnProceso.setEnProceso(false);
        retoEnProceso.setSeleccionado(true);
        retoEnProceso.setFechaInicio(null);
        this.repositorioReto.guardarReto(retoEnProceso);

        return  retoEnProceso;
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExistenRetosEnProcesoEnLaBaseDeDatosParaTerminarQueAlTerminarRetoSePuedaActualizarSuAtributosFechaYEnProceso() {
        // preparacion
        Reto retoEnProceso = dadoQueExistenRetosEnProcesoEnLaBaseDeDatosParaTerminar();

        // ejecucion
        Reto retoEnLaBd = repositorioReto.obtenerRetoPorId(retoEnProceso.getId());

        // verificacion
        assertThat(retoEnProceso,equalTo(retoEnLaBd));
        assertNotNull(retoEnLaBd, "El reto guardado no debe ser null.");
        assertTrue(retoEnLaBd.getSeleccionado(), "El reto guardado debe ser seleccionado.");
        assertFalse(retoEnLaBd.getEnProceso(), "El reto guardado debe estar en true enProceso.");
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExistenRetosEnLaBaseDeDatosYRetosEnProcesoQueSePuedanObtenerTodosLosRetosDeLaBaseDeDatos() {
        // preparación
        dadoQueExistenRetosEnLaBaseDeDatos();
        dadoQueExistenRetosEnProcesoEnLaBaseDeDatos();

        // ejecución
        List<Reto> todosLosRetos = repositorioReto.obtenerTodosLosRetos();

        // verificación
        assertThat(todosLosRetos.size(),equalTo(2));
        assertNotNull(todosLosRetos, "La lista de retos no debería ser nula");
        assertFalse(todosLosRetos.isEmpty(), "La lista de retos no debería estar vacía");
    }



}
