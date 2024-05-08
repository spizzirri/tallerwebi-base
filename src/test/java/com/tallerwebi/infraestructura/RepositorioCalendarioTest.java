package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DiaCalendario;
import com.tallerwebi.dominio.Calendario;
import com.tallerwebi.dominio.Rendimiento;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class RepositorioCalendarioTest {

    private DiaCalendario diaCalendario;
    private Calendario calendario;

    @Test
    public void queSePuedaGuardarDiasEnElCalendario(){
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1,2,2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2,2,2024, Rendimiento.NORMAL));
        Integer respuestaObtenida = calendario.getCantidadDias();
        Integer respuestaEsperada = 2;
        assertThat(respuestaObtenida,equalTo(respuestaEsperada));
    }

    @Test
    public void queSePuedaImprimirLosDiasCreadosEnElCalendario() {
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2, 2, 2024, Rendimiento.NORMAL));

        String respuestaObtenida = calendario.toString();
        String respuestaEsperada = "Calendario{ [DiaCalendario{dia=1, mes=2, ano=2024}, DiaCalendario{dia=2, mes=2, ano=2024}]}";
        assertThat(respuestaObtenida, equalTo(respuestaEsperada));
    }

    @Test
    public void queNoSePuedaAgregarDosMismosDiasCalendario(){
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(2, 2, 2024, Rendimiento.NORMAL));
        Integer respuestaObtenida = calendario.getCantidadDias();
        Integer respuestaEsperada = 2;
        assertThat(respuestaObtenida,equalTo(respuestaEsperada));
    }

    @Test
    public void queSePuedanOrdenarTodosLosDiasDelCalendarioSegunSuFechaMasCercanaAlAno() {
        Calendario calendario = new Calendario();
        calendario.agregarDia(new DiaCalendario(1, 2, 2024, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(15, 7, 2023, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(5, 9, 2025, Rendimiento.NORMAL));
        calendario.agregarDia(new DiaCalendario(10, 12, 2023, Rendimiento.NORMAL));

        calendario.ordenarDiasPorFechaCercana();

        List<DiaCalendario> diasOrdenados = calendario.getDias();
        DiaCalendario diaMasCercano = diasOrdenados.get(0);
        assertThat(diaMasCercano.getDia(), equalTo(15));
        assertThat(diaMasCercano.getMes(), equalTo(7));
        assertThat(diaMasCercano.getAno(), equalTo(2023));
    }












}
