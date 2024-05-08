package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DiaCalendario;
import com.tallerwebi.dominio.Calendario;
import com.tallerwebi.dominio.Rendimiento;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class CalendarioTest {

    private DiaCalendario diaCalendario;
    private Calendario calendario;

    @Test
    public void queSePuedaGuardarDiasEnElCalendario(){
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1,1,2,2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2,2,2,2024, Rendimiento.NORMAL));
        Integer respuestaObtenida = calendario.getCantidadDias();
        Integer respuestaEsperada = 2;
        assertThat(respuestaObtenida,equalTo(respuestaEsperada));
    }

    @Test
    public void queSePuedaImprimirLosDiasCreadosEnElCalendario() {
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1,1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2,2, 2, 2024, Rendimiento.NORMAL));

        String respuestaObtenida = calendario.toString();
        String respuestaEsperada = "Calendario{dias=[DiaCalendario{id=1, dia=1, mes=2, ano=2024, rendimiento=DESCANSO}, DiaCalendario{id=2, dia=2, mes=2, ano=2024, rendimiento=DESCANSO}]}";
        assertThat(respuestaObtenida, equalTo(respuestaEsperada));
    }

    @Test
    public void queNoSePuedaAgregarDosMismosDiasCalendario(){
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1,1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2,1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(3,2, 2, 2024, Rendimiento.NORMAL));
        Integer respuestaObtenida = calendario.getCantidadDias();
        Integer respuestaEsperada = 2;
        assertThat(respuestaObtenida,equalTo(respuestaEsperada));
    }

    @Test
    public void queSePuedanOrdenarTodosLosDiasDelCalendarioEnOrdenCronologico() {
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1,1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2,15, 7, 2023, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(3,5, 9, 2025, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(4,10, 12, 2023, Rendimiento.NORMAL));

        calendario.ordenarDiasPorFechaCronologica();

        List<DiaCalendario> diasOrdenados = calendario.getDias();
        DiaCalendario diaMasCercano = diasOrdenados.get(calendario.getCantidadDias() - 1);
        assertThat(diasOrdenados.size(),equalTo(4));
        assertThat(diaMasCercano.getDia(), equalTo(15));
        assertThat(diaMasCercano.getMes(), equalTo(7));
        assertThat(diaMasCercano.getAno(), equalTo(2023));
    }

}
