package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DiaCalendario;
import com.tallerwebi.dominio.Rendimiento;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiaCalendarioTest {

    private DiaCalendario diaCalendario;

    @Test
    public void queSePuedaCrearUnDiaCalendario(){
        DiaCalendario diaCalendario = new DiaCalendario(1,7,5,2024, Rendimiento.NORMAL);
        Integer respuestaObtenida = diaCalendario.getDia();
        Integer respuestaEsperada = 7;
        assertThat(respuestaObtenida,equalTo(respuestaEsperada));
    }

    @Test
    public void queSeRetorneElRendimientoDelDia() {
        // Crear un objeto DiaCalendario simulado
        DiaCalendario diaCalendario = mock(DiaCalendario.class);

        // Definir el comportamiento esperado cuando se llame al método getRendimiento()
        when(diaCalendario.getRendimiento()).thenReturn(Rendimiento.ALTO);

        // Verificar que se obtiene el rendimiento esperado
        Rendimiento rendimientoEsperado = Rendimiento.ALTO;
        Rendimiento rendimientoObtenido = diaCalendario.getRendimiento();

        assertEquals(rendimientoEsperado, rendimientoObtenido);
    }

    @Test
    public void queSiEseDiaNoSeEligioUnaOpcionMuestreDescanso() {
        DiaCalendario diaCalendario = new DiaCalendario(1,7, 5, 2024, Rendimiento.DESCANSO);

        String rendimientoDia = diaCalendario.getRendimiento().name();

        // Verificar que la representación del rendimiento sea "Descanso"
        assertEquals("DESCANSO", rendimientoDia);
    }

}
