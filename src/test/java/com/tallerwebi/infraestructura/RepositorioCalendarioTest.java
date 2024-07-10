package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioCalendarioTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioCalendario repositorioCalendario;

    @BeforeEach
    public void init(){
    this.repositorioCalendario = new RepositorioCalendarioImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    public void queSePuedaGuardarUnItemRendimiento(){
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);
        this.sessionFactory.getCurrentSession().refresh(itemRendimiento);
        Long idGuardado = itemRendimiento.getId();
        ItemRendimiento diaObtenido = (ItemRendimiento) this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento Where id = :id")
                .setParameter("id", idGuardado)
                .getSingleResult();
        assertThat(diaObtenido.getId(), equalTo(itemRendimiento.getId()));
    }

    @Test
    @Transactional
    public void queAlGuardarMasDeUnItemRendimientoElMismoDiaRecibaError() {
        // Crear y guardar un ItemRendimiento con una fecha específica
        LocalDate fechaEspecifica = LocalDate.of(2024, Month.MAY, 15);
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(fechaEspecifica, TipoRendimiento.DESCANSO);
        repositorioCalendario.guardar(itemRendimiento1);

        // Intentar guardar otro ItemRendimiento en la misma fecha (debería lanzar una excepción)
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(fechaEspecifica, TipoRendimiento.DESCANSO);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repositorioCalendario.guardar(itemRendimiento2);
        });
        // Verificar el mensaje de la excepción
        assertEquals("Ya existe un ItemRendimiento para esta fecha.", exception.getMessage());
    }

    @Test
    @Transactional
    public void queSePuedaActualizarUnItemRendimiento() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);

        itemRendimiento.setTipoRendimiento(TipoRendimiento.ALTO);
        this.repositorioCalendario.actualizar(itemRendimiento);
        Long idGuardado = itemRendimiento.getId();
        ItemRendimiento diaObtenido = (ItemRendimiento) this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento Where id = :id")
                .setParameter("id", idGuardado)
                .getSingleResult();
        assertThat(diaObtenido.getTipoRendimiento(), equalTo(TipoRendimiento.ALTO));
    }

    @Test
    @Transactional
    public void queSePuedanObtenerTodosLosItemRendimiento() {
        // Crear los objetos ItemRendimiento con diferentes fechas
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(2), TipoRendimiento.NORMAL);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(LocalDate.now().minusDays(1), TipoRendimiento.DESCANSO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(LocalDate.now(), TipoRendimiento.BAJO);

        // Guardar los objetos en el repositorio
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        this.repositorioCalendario.guardar(itemRendimiento3);

        // Limpiar la sesión actual para asegurar que se reflejen los cambios
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // Obtener todos los ItemRendimiento de la base de datos
        List<ItemRendimiento> diasObtenidos = sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();

        // Verificar que se obtuvieron los tres objetos guardados
        assertThat(diasObtenidos.size(), equalTo(3));
        assertThat(diasObtenidos.get(0), equalTo(itemRendimiento1));
        assertThat(diasObtenidos.get(1), equalTo(itemRendimiento2));
        assertThat(diasObtenidos.get(2), equalTo(itemRendimiento3));
    }


    @Test
    @Transactional
    void queSePuedaObtenerTipoRendimientoMasSeleccionado() {
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(15), TipoRendimiento.ALTO);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(LocalDate.now().minusDays(14), TipoRendimiento.ALTO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(LocalDate.now().minusDays(13), TipoRendimiento.BAJO);
        ItemRendimiento itemRendimiento4 = new ItemRendimiento(LocalDate.now().minusDays(12), TipoRendimiento.NORMAL);

        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        this.repositorioCalendario.guardar(itemRendimiento3);
        this.repositorioCalendario.guardar(itemRendimiento4);
        // Verificar que el tipo de rendimiento más seleccionado sea el esperado
        ItemRendimiento itemMasSeleccionado = repositorioCalendario.obtenerItemMasSeleccionado();
        assertEquals(TipoRendimiento.ALTO, itemMasSeleccionado.getTipoRendimiento());
    }


    @Test
    @Transactional

    public void queSePuedaVaciarElCalendario() {
        // Crear dos ItemRendimiento con fechas diferentes
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(TipoRendimiento.BAJO);
        itemRendimiento1.setFecha(LocalDate.now());
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(TipoRendimiento.DESCANSO);
        itemRendimiento2.setFecha(LocalDate.now().plusDays(1));

        // Guardar los items en el repositorio
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);

        // Vaciar el calendario
        this.repositorioCalendario.vaciarCalendario();

        // Obtener todos los ItemRendimiento del repositorio para verificar que esté vacío
        List<ItemRendimiento> diasRestantes = this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();

        // Verificar que el calendario está vacío
        assertThat(diasRestantes.isEmpty(), equalTo(true));
    }


    @Test
    @Transactional
    public void queSePuedanObtenerTodosLosItemRendimientoEnLista() {
        // Crear dos ItemRendimiento con fechas diferentes
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(TipoRendimiento.NORMAL);
        itemRendimiento1.setFecha(LocalDate.now());
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(TipoRendimiento.DESCANSO);
        itemRendimiento2.setFecha(LocalDate.now().plusDays(1));

        // Guardar los items en el repositorio
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);

        // Obtener todos los ItemRendimiento del repositorio
        List<ItemRendimiento> diasObtenidos = this.repositorioCalendario.obtenerItemsRendimiento();

        // Verificar que se obtuvieron los dos items guardados
        assertThat(diasObtenidos.size(), equalTo(2));
    }



    @Test
    @Transactional
    public void queSePuedaVerificarSiExisteItemRendimientoPorFecha() {
        LocalDate fechaEspecifica = LocalDate.of(2024, Month.MAY, 15);
        ItemRendimiento itemRendimiento = new ItemRendimiento(fechaEspecifica, TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);

        boolean existe = this.repositorioCalendario.existeItemRendimientoPorFecha(fechaEspecifica);
        assertThat(existe, equalTo(true));

        boolean noExiste = this.repositorioCalendario.existeItemRendimientoPorFecha(LocalDate.of(2024, Month.JUNE, 15));
        assertThat(noExiste, equalTo(false));
    }

    @Test
    @Transactional
    public void queSePuedanObtenerItemsPorTipoRendimiento() {
        // Crear los objetos ItemRendimiento con diferentes fechas
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(2), TipoRendimiento.NORMAL);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(LocalDate.now().minusDays(1), TipoRendimiento.DESCANSO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(LocalDate.now(), TipoRendimiento.NORMAL);

        // Guardar los objetos en el repositorio
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        this.repositorioCalendario.guardar(itemRendimiento3);

        // Obtener y verificar los items con TipoRendimiento.NORMAL
        List<ItemRendimiento> itemsNormales = this.repositorioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.NORMAL);
        System.out.println("Items normales: " + itemsNormales);

        assertThat(itemsNormales.size(), equalTo(2));
        assertThat(itemsNormales.get(0), equalTo(itemRendimiento1));
        assertThat(itemsNormales.get(1), equalTo(itemRendimiento3));

        // Obtener y verificar los items con TipoRendimiento.DESCANSO
        List<ItemRendimiento> itemsDescanso = this.repositorioCalendario.obtenerItemsPorTipoRendimiento(TipoRendimiento.DESCANSO);
        System.out.println("Items de descanso: " + itemsDescanso);

        assertThat(itemsDescanso.size(), equalTo(1));
        assertThat(itemsDescanso.get(0), equalTo(itemRendimiento2));
    }



    @Test
    @Transactional
    public void queSePuedaEliminarUnItemRendimiento() {
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.BAJO);
        this.repositorioCalendario.guardar(itemRendimiento);
        Long idGuardado = itemRendimiento.getId();

        this.repositorioCalendario.eliminar(itemRendimiento);
        ItemRendimiento itemEliminado = this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento WHERE id = :id", ItemRendimiento.class)
                .setParameter("id", idGuardado)
                .uniqueResult();

        assertThat(itemEliminado, equalTo(null));
    }



}
