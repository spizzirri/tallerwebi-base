package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(TipoRendimiento.NORMAL);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(TipoRendimiento.DESCANSO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(TipoRendimiento.BAJO);
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        this.repositorioCalendario.guardar(itemRendimiento3);

        List<ItemRendimiento> diasObtenidos = this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();

        assertThat(diasObtenidos.size(), equalTo(3));
        assertThat(diasObtenidos.get(0), equalTo(itemRendimiento1));
        assertThat(diasObtenidos.get(1), equalTo(itemRendimiento2));
        assertThat(diasObtenidos.get(2), equalTo(itemRendimiento3));
    }

    @Test
    @Transactional
    public void queSePuedaVaciarElCalendario() {
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(TipoRendimiento.BAJO);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);

        this.repositorioCalendario.vaciarCalendario();
        List<ItemRendimiento> diasRestantes = this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();

        assertThat(diasRestantes.isEmpty(), equalTo(true));
    }

    @Test
    @Transactional
    public void queSePuedanObtenerTodosLosItemRendimientoEnLista() {

        ItemRendimiento itemRendimiento1 = new ItemRendimiento(TipoRendimiento.NORMAL);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        List<ItemRendimiento> diasObtenidos = this.repositorioCalendario.obtenerItemsRendimiento();
        assertThat(diasObtenidos.size(), equalTo(2));
    }


}
