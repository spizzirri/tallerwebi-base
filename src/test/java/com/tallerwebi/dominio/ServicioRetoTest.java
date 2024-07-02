package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.ServicioCalendarioImpl;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.reto.ServicioRetoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioRetoTest {

    private RepositorioReto repositorioReto;

    private ServicioReto servicioReto;

    @BeforeEach
    public void init() {
        this.repositorioReto = mock(RepositorioReto.class);
        this.servicioReto = new ServicioRetoImpl(this.repositorioReto);
    }

    @Test
    public void dadoQueExistenRetosGuardadosEnLaBaseDeDatosQueSePuedaTraerUnRetoDisponible() {
        // preparación
        Reto retoMock = new Reto("Reto de Ejemplo", "Descripción del reto de ejemplo");
        retoMock.setId(1L);
        retoMock.setSeleccionado(false);

        when(repositorioReto.obtenerRetoDisponible()).thenAnswer(invocation -> {
            retoMock.setSeleccionado(true);
            return retoMock;
        });

        //ejecucion
        Reto retoObtenido = servicioReto.obtenerRetoDisponible();

        //verificacion
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(retoMock.getId(), retoObtenido.getId(), "El ID del reto debería coincidir");
        assertEquals(retoMock.getNombre(), retoObtenido.getNombre(), "El nombre del reto debería coincidir");
        assertEquals(retoMock.getDescripcion(), retoObtenido.getDescripcion(), "La descripción del reto debería coincidir");
        assertTrue(retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");
        verify(repositorioReto, times(1)).obtenerRetoDisponible();
    }

    @Test
    public void dadoQueExistenRetosGuardadosEnLaBaseDeDatosQueSePuedaTraerUnRetoEspecificoPorId() {
        //preparacion
        Reto retoSimulado = new Reto();
        retoSimulado.setId(1L);
        retoSimulado.setDescripcion("Descripción del reto");
        retoSimulado.setSeleccionado(true);

        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoSimulado);

        //ejecucion
        Reto retoObtenido = servicioReto.obtenerRetoPorId(1L);

        //verificacion
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(1L, retoObtenido.getId(), "El ID del reto obtenido debería ser 1");
        assertEquals("Descripción del reto", retoObtenido.getDescripcion(), "La descripción del reto debería ser 'Descripción del reto'");
        assertTrue(retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");
    }

    @Test
    public void dadoQueExisteUnRetoEnProcesoEnLaBaseDeDatosQueSeLogreObtener() {
        //preparacion
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setDescripcion("Descripción del reto en proceso");
        retoMock.setSeleccionado(true);
        retoMock.setEnProceso(true);

        when(repositorioReto.obtenerRetoEnProceso()).thenReturn(retoMock);

        //ejecucion
        Reto retoObtenido = servicioReto.obtenerRetoEnProceso();

        //verificacion
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(retoMock.getId(), retoObtenido.getId(), "El ID del reto debería coincidir");
        assertEquals(retoMock.getDescripcion(), retoObtenido.getDescripcion(), "La descripción del reto debería coincidir");
        assertEquals(retoMock.getSeleccionado(), retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");
        assertEquals(retoMock.getEnProceso(), retoObtenido.getEnProceso(), "El reto debería estar en proceso");
        verify(repositorioReto, times(1)).obtenerRetoEnProceso();
    }

    @Test
    public void dadoQueExisteUnRetoEnProcesoTerminarRetoDevuelvaDiasTranscurridosYActualiceElReto() {
        //preparacion
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setSeleccionado(true);
        retoMock.setEnProceso(true);
        retoMock.setFechaInicio(LocalDate.now().minusDays(3));

        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoMock);

        //ejecucion
        long diasPasados = servicioReto.terminarReto(1L);

        //verificacion
        verify(repositorioReto, times(1)).obtenerRetoPorId(1L);
        verify(repositorioReto, times(1)).actualizarReto(retoMock);
        assertEquals(3, diasPasados, "Los días transcurridos deberían ser 3");
        assertTrue(retoMock.getSeleccionado(), "El reto debería estar marcado como no seleccionado");
        assertFalse(retoMock.getEnProceso(), "El reto debería estar marcado como no en proceso");
        assertNull(retoMock.getFechaInicio(), "El reto debería tener fechaInicio en null después de terminarlo");
    }

    @Test
    public void queSePuedaCambiarReto() {
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setSeleccionado(false);

        doNothing().when(repositorioReto).actualizarReto(any(Reto.class));

        Reto retoCambiado = servicioReto.cambiarReto(retoMock);

        assertNotNull(retoCambiado, "El reto cambiado no debería ser nulo");
        assertTrue(retoCambiado.getSeleccionado(), "El reto debería estar marcado como seleccionado");
        verify(repositorioReto, times(1)).actualizarReto(retoMock);
    }

    @Test
    public void queSePuedaEmpezarRetoActualizado() {
        // preparación
        Reto retoMock = new Reto("Reto de Ejemplo", "Descripción del reto de ejemplo");
        retoMock.setId(1L);
        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoMock);

        // ejecución
        servicioReto.empezarRetoActualizado(1L);

        // verificación
        assertTrue(retoMock.getSeleccionado(), "El reto debería estar marcado como seleccionado");
        assertTrue(retoMock.getEnProceso(), "El reto debería estar en proceso");
        assertNotNull(retoMock.getFechaInicio(), "La fecha de inicio no debería ser nula");
        verify(repositorioReto, times(1)).actualizarReto(retoMock);
    }

    @Test
    public void queSePuedaCalcularTiempoRestante() {
        // preparación
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setFechaInicio(LocalDate.now().minusDays(1)); // Reto iniciado hace 1 día
        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoMock);

        // ejecución
        Long tiempoRestante = servicioReto.calcularTiempoRestante(1L);

        // verificación
        assertNotNull(tiempoRestante, "El tiempo restante no debería ser nulo");
        assertTrue(tiempoRestante > 0, "El tiempo restante debería ser positivo");
        // Suponiendo que el reto dura 2 días
        assertEquals(1440, tiempoRestante, "El tiempo restante debería ser 1440 minutos (1 día)");
    }

}
