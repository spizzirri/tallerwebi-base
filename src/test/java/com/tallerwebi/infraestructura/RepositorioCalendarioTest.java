package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.excepcion.ItemRendimientoNoEncontradoException;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
    public void queSePuedaGuardarUnDiaCalendario(){
        LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual
        ItemRendimiento itemRendimiento = new ItemRendimiento(1L, fechaActual, TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);
        ItemRendimiento diaObtenido = (ItemRendimiento) this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento Where id = 1L")
                .getSingleResult();
        assertThat(diaObtenido, equalTo(itemRendimiento));
    }

    @Test
    @Transactional
    public void queSePuedaActualizarUnDiaCalendario() {
        LocalDate fechaActual = LocalDate.now();
        ItemRendimiento itemRendimiento = new ItemRendimiento(2L, fechaActual ,TipoRendimiento.DESCANSO);
        this.repositorioCalendario.guardar(itemRendimiento);

        itemRendimiento.setTipoRendimiento(TipoRendimiento.ALTO);
        this.repositorioCalendario.actualizar(itemRendimiento);

        ItemRendimiento diaObtenido = (ItemRendimiento) this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento Where id = 2L")
                .getSingleResult();
        assertThat(diaObtenido.getTipoRendimiento(), equalTo(TipoRendimiento.ALTO));
    }





}
