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
import java.util.ArrayList;
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
    public void queSePuedaObtenerRetoDisponible() {
        // Crear un reto de ejemplo
        Reto retoMock = new Reto("Reto de Ejemplo", "Descripción del reto de ejemplo");
        retoMock.setId(1L);
        retoMock.setSeleccionado(false);

        // Configurar el mock para que devuelva el reto de ejemplo y marcarlo como seleccionado
        when(repositorioReto.obtenerRetoDisponible()).thenAnswer(invocation -> {
            retoMock.setSeleccionado(true);
            return retoMock;
        });

        // Llamar al método del servicio
        Reto retoObtenido = servicioReto.obtenerRetoDisponible();

        // Verificar el resultado
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(retoMock.getId(), retoObtenido.getId(), "El ID del reto debería coincidir");
        assertEquals(retoMock.getNombre(), retoObtenido.getNombre(), "El nombre del reto debería coincidir");
        assertEquals(retoMock.getDescripcion(), retoObtenido.getDescripcion(), "La descripción del reto debería coincidir");
        assertTrue(retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");

        // Verificar que se llamó al método del repositorio
        verify(repositorioReto, times(1)).obtenerRetoDisponible();
    }

    @Test
    public void queVerifiqueQueSePuedeObtenerRetoPorId() {
        // Crear un reto simulado
        Reto retoSimulado = new Reto();
        retoSimulado.setId(1L);
        retoSimulado.setDescripcion("Descripción del reto");
        retoSimulado.setSeleccionado(true);

        // Configurar el comportamiento simulado del repositorio
        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoSimulado);

        // Llamar al método del servicio
        Reto retoObtenido = servicioReto.obtenerRetoPorId(1L);

        // Verificar los resultados
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(1L, retoObtenido.getId(), "El ID del reto obtenido debería ser 1");
        assertEquals("Descripción del reto", retoObtenido.getDescripcion(), "La descripción del reto debería ser 'Descripción del reto'");
        assertEquals(true, retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");
    }

    @Test
    public void queSePuedaObtenerRetoEnProceso() {
        // Crear un reto de ejemplo
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setDescripcion("Descripción del reto en proceso");
        retoMock.setSeleccionado(true);
        retoMock.setEnProceso(true);

        // Configurar el mock para que devuelva el reto de ejemplo
        when(repositorioReto.obtenerRetoEnProceso()).thenReturn(retoMock);

        // Llamar al método del servicio
        Reto retoObtenido = servicioReto.obtenerRetoEnProceso();

        // Verificar el resultado
        assertNotNull(retoObtenido, "El reto obtenido no debería ser nulo");
        assertEquals(retoMock.getId(), retoObtenido.getId(), "El ID del reto debería coincidir");
        assertEquals(retoMock.getDescripcion(), retoObtenido.getDescripcion(), "La descripción del reto debería coincidir");
        assertEquals(retoMock.getSeleccionado(), retoObtenido.getSeleccionado(), "El reto debería estar marcado como seleccionado");
        assertEquals(retoMock.getEnProceso(), retoObtenido.getEnProceso(), "El reto debería estar en proceso");

        // Verificar que se llamó al método del repositorio
        verify(repositorioReto, times(1)).obtenerRetoEnProceso();
    }

    @Test
    public void queAlTerminarRetoSeActualiceSeleccionadoYEnProceso() {
        // Arrange
        Reto retoMock = new Reto();
        retoMock.setId(1L);
        retoMock.setSeleccionado(true);
        retoMock.setEnProceso(true);

        // Configurar el comportamiento del repositorio
        when(repositorioReto.obtenerRetoPorId(anyLong())).thenReturn(retoMock);

        // Act
        servicioReto.terminarReto(1L);

        // Assert
        verify(repositorioReto, times(1)).obtenerRetoPorId(1L);
        verify(repositorioReto, times(1)).terminarReto(retoMock);
        assertFalse(retoMock.getSeleccionado(), "El reto debería estar marcado como no seleccionado");
        assertFalse(retoMock.getEnProceso(), "El reto debería estar marcado como no en proceso");
    }

}
