package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.DiaCalendario;
import com.tallerwebi.dominio.calendario.RepositorioDiaCalendario;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioDiaCalendarioTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioDiaCalendario repositorioDiaCalendario;

    @BeforeEach
    public void init(){
    this.repositorioDiaCalendario = new RepositorioDiaCalendarioImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    public void queSePuedaGuardarUnDiaCalendario(){
        LocalDate fechaEsperada = LocalDate.of(2024, 5, 10);
        DiaCalendario diaCalendario = new DiaCalendario(1L, fechaEsperada ,TipoRendimiento.DESCANSO);
        this.repositorioDiaCalendario.guardar(diaCalendario);
        DiaCalendario diaObtenido = (DiaCalendario) this.sessionFactory.getCurrentSession()
                .createQuery("FROM DiaCalendario Where id = 1L")
                .getSingleResult();
        assertThat(diaObtenido, equalTo(diaCalendario));
    }





}
