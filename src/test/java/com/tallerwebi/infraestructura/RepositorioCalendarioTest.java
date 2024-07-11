package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
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
    @Rollback
    public void dadoQueExisteElEspacioQueSePuedaGuardarItemRendimientoEnLaBaseDeDatos(){
        // preparacion
        ItemRendimiento itemRendimiento = new ItemRendimiento(TipoRendimiento.DESCANSO);

        // ejecucion
        this.repositorioCalendario.guardar(itemRendimiento);

        // verificacion
        this.sessionFactory.getCurrentSession().refresh(itemRendimiento);
        Long idGuardado = itemRendimiento.getId();
        ItemRendimiento diaObtenido = (ItemRendimiento) this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento Where id = :id")
                .setParameter("id", idGuardado)
                .getSingleResult();
        assertThat(diaObtenido.getId(), equalTo(itemRendimiento.getId()));
    }

    public void dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatos() {
        // Crear los objetos ItemRendimiento con diferentes fechas
        ItemRendimiento itemRendimiento1 = new ItemRendimiento(LocalDate.now().minusDays(2), TipoRendimiento.NORMAL);
        ItemRendimiento itemRendimiento2 = new ItemRendimiento(LocalDate.now().minusDays(1), TipoRendimiento.DESCANSO);
        ItemRendimiento itemRendimiento3 = new ItemRendimiento(LocalDate.now(), TipoRendimiento.BAJO);
        ItemRendimiento itemRendimiento4 = new ItemRendimiento(LocalDate.now().minusDays(12), TipoRendimiento.NORMAL);

        // Guardar los objetos en el repositorio
        this.repositorioCalendario.guardar(itemRendimiento1);
        this.repositorioCalendario.guardar(itemRendimiento2);
        this.repositorioCalendario.guardar(itemRendimiento3);
        this.repositorioCalendario.guardar(itemRendimiento4);

    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatosQueSePuedanObtenerTodosLosItemRendimiento() {
        // preparacion
        dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatos();

        // ejecucion
        List<ItemRendimiento> diasObtenidos = repositorioCalendario.obtenerItemsRendimiento();

        // Verificar que se obtuvieron los tres objetos guardados
        assertThat(diasObtenidos.size(), equalTo(4));
    }

    @Test
    @Transactional
    @Rollback
    void dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatosQueSePuedaObtenerTipoRendimientoMasSeleccionado() {
        // preparacion
        dadoQueExistenItemsRendimientoGuardadosEnLaBaseDeDatos();

        // ejecucion
        ItemRendimiento itemMasSeleccionado = repositorioCalendario.obtenerItemMasSeleccionado();

        // verificacion
        assertEquals(TipoRendimiento.NORMAL, itemMasSeleccionado.getTipoRendimiento());
    }

    public LocalDate dadoQueExisteUnItemRendimientoGuardado() {
        LocalDate fechaEspecifica = LocalDate.of(2024, Month.MAY, 15);
        ItemRendimiento itemRendimiento = new ItemRendimiento(fechaEspecifica, TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);

        return fechaEspecifica;
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUnItemRendimientoGuardadoQueSePuedaVerificarSiExisteItemRendimientoPorFecha() {
        // preparacion
        LocalDate fechaEspecifica = dadoQueExisteUnItemRendimientoGuardado();

        // ejecucion
        boolean existe = this.repositorioCalendario.existeItemRendimientoPorFecha(fechaEspecifica);
        assertThat(existe, equalTo(true));

        // verificacion
        boolean noExiste = this.repositorioCalendario.existeItemRendimientoPorFecha(LocalDate.of(2024, Month.JUNE, 15));
        assertThat(noExiste, equalTo(false));
    }

}
