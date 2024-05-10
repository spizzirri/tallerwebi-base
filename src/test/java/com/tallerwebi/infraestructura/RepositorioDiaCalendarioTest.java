package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.DiaCalendario;
import com.tallerwebi.dominio.calendario.RepositorioDiaCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class RepositorioDiaCalendarioTest {

    private DiaCalendario diaCalendario;

    @Test
    public void testGuardarDiaCalendario() {
        RepositorioDiaCalendario repositorio = new RepositorioDiaCalendarioImpl(); // Suponiendo que tienes una implementación del repositorio

        DiaCalendario dia = new DiaCalendario(1,1, 1, 2024, TipoRendimiento.NORMAL);

        // Guardar el DiaCalendario en el repositorio
        repositorio.guardar(dia);

        // Buscar el DiaCalendario en el repositorio y verificar que fue guardado correctamente
        DiaCalendario diaGuardado = repositorio.buscar(dia.getId());
        assertNotNull(diaGuardado);
        assertEquals(dia, diaGuardado);
    }
}

