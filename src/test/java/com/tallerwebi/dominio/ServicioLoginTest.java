package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.NoCambiosRestantesException;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.usuario.RepositorioUsuario;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.ServicioLoginImpl;
import com.tallerwebi.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;

public class ServicioLoginTest {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioReto repositorioReto;
    private ServicioCalendario servicioCalendario;
    private ServicioLogin servicioLogin;
    private ServicioReto servicioReto;


    @BeforeEach
    public void init() {
        this.repositorioReto = mock(RepositorioReto.class);
        this.repositorioUsuario = mock(RepositorioUsuario.class);
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.servicioReto = mock(ServicioReto.class);
        this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuario, this.servicioCalendario, this.servicioReto);
    }


    @Test
    public void queAlModificarRachaRetoTerminadoSeSumeUnaRachaPorTerminarloEnTiempo() {
        Usuario usuario = new Usuario();
        usuario.setEmail("hola@sasss.com");
        usuario.setNombre("javier");
        usuario.setApellido("tucci");
        usuario.setRachaDeRetos(5);
        long retoId = 1L;
        when(servicioReto.terminarReto(retoId)).thenReturn(1L); // Simula que ha pasado 1 día

        Usuario resultado = servicioLogin.modificarRachaRetoTerminado(usuario, retoId);

        assertNotNull(resultado);
        assertEquals(6, resultado.getRachaDeRetos()); // La racha debe incrementarse en 1
    }

    @Test
    public void queAlModificarRachaRetoTerminadoLaRachaDelUsuarioSeReseteePorPasarseDeDosDias() {
        long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setEmail("hola@sasss.com");
        usuario.setNombre("javier");
        usuario.setApellido("tucci");
        usuario.setRachaDeRetos(2);
        when(servicioReto.terminarReto(retoId)).thenReturn(2L); // Simula que han pasado 2 días

        Usuario resultado = servicioLogin.modificarRachaRetoTerminado(usuario, retoId);

        assertNotNull(resultado);
        assertEquals(0, resultado.getRachaDeRetos()); // La racha debe resetearse a 0

    }

    @Test
        public void queCambiarRetoFuncionaCorrectamente() {
            // Arrange
            Long retoId = 1L;
            Usuario usuario = new Usuario();
            usuario.setCambioReto(3);

            Reto retoActual = new Reto();
            retoActual.setId(retoId);
            retoActual.setSeleccionado(false);

            Reto nuevoReto = new Reto();
            nuevoReto.setId(2L);
            retoActual.setSeleccionado(false);

            when(servicioReto.obtenerRetoPorId(retoId)).thenReturn(retoActual);
            when(servicioReto.obtenerRetoDisponible()).thenReturn(nuevoReto);

            // Act
            Reto resultado = servicioLogin.cambiarReto(retoId, usuario);

            // Assert
            assertNotNull(resultado, "El nuevo reto no debería ser nulo");
            assertEquals(nuevoReto.getId(), resultado.getId(), "El ID del nuevo reto debería coincidir");
            assertEquals(2, usuario.getCambioReto(), "El número de cambios restantes del usuario debería ser 2");
    }

    @Test
    public void queAlCambiarRetoUsuarioConCambiosDisponiblesSeLogreCorrectamente() {
        // Configuración de prueba
        Long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setCambioReto(1);

        Reto retoActual = new Reto(retoId);
        Reto nuevoReto = new Reto(2L);

        when(servicioReto.obtenerRetoPorId(retoId)).thenReturn(retoActual);
        when(servicioReto.obtenerRetoDisponible()).thenReturn(nuevoReto);

        // Ejecución del método
        Reto resultado = servicioLogin.cambiarReto(retoId, usuario);

        // Verificación
        assertNotNull(resultado);
        assertEquals(nuevoReto, resultado);
        assertEquals(0, usuario.getCambioReto());
    }

    @Test
    public void alCambiarRetoElUsuarioSinCambiosDisponibles() {
        // Configuración de prueba
        Long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setCambioReto(0);

        // Ejecución y verificación
        NoCambiosRestantesException exception = assertThrows(NoCambiosRestantesException.class, () -> {
            servicioLogin.cambiarReto(retoId, usuario);
        });
        assertEquals("No te quedan cambios. Debes terminar los retos.", exception.getMessage());

        verify(servicioReto, never()).obtenerRetoPorId(anyLong());
        verify(servicioReto, never()).obtenerRetoDisponible();
        verify(repositorioUsuario, never()).modificar(usuario);
    }

}
