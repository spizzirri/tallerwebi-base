package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.NoCambiosRestantesException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.usuario.RepositorioUsuario;
import com.tallerwebi.dominio.usuario.ServicioLogin;
import com.tallerwebi.dominio.usuario.ServicioLoginImpl;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.presentacion.DatosItemRendimiento;
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
    private ServicioPerfil servicioPerfil;


    @BeforeEach
    public void init() {
        this.repositorioReto = mock(RepositorioReto.class);
        this.repositorioUsuario = mock(RepositorioUsuario.class);
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.servicioReto = mock(ServicioReto.class);
        this.servicioPerfil = mock(ServicioPerfil.class);
        this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuario, this.servicioCalendario, this.servicioReto, this.servicioPerfil);
    }


    @Test
    public void queAlModificarRachaRetoTerminadoSeSumeUnaRachaPorTerminarloEnTiempo() {
        // preparación
        Usuario usuario = new Usuario();
        usuario.setEmail("hola@sasss.com");
        usuario.setNombre("javier");
        usuario.setApellido("tucci");
        usuario.setRachaDeRetos(5);
        long retoId = 1L;
        when(servicioReto.terminarReto(retoId)).thenReturn(1L); // Simula que ha pasado 1 día

        // ejecución
        Usuario resultado = servicioLogin.modificarRachaRetoTerminado(usuario, retoId);

        // verificación
        assertNotNull(resultado);
        assertEquals(6, resultado.getRachaDeRetos()); // La racha debe incrementarse en 1
    }

    @Test
    public void queAlModificarRachaRetoTerminadoLaRachaDelUsuarioSeReseteePorPasarseDeDosDias() {
        // preparación
        long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setEmail("hola@sasss.com");
        usuario.setNombre("javier");
        usuario.setApellido("tucci");
        usuario.setRachaDeRetos(2);
        when(servicioReto.terminarReto(retoId)).thenReturn(2L); // Simula que han pasado 2 días

        // ejecución
        Usuario resultado = servicioLogin.modificarRachaRetoTerminado(usuario, retoId);

        // verificación
        assertNotNull(resultado);
        assertEquals(0, resultado.getRachaDeRetos()); // La racha debe resetearse a 0
    }

    @Test
    public void queCambiarRetoFuncionaCorrectamente() {
        // preparación
        Long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setCambioReto(3);

        Reto retoActual = new Reto();
        retoActual.setId(retoId);
        retoActual.setSeleccionado(false);

        Reto nuevoReto = new Reto();
        nuevoReto.setId(2L);
        nuevoReto.setSeleccionado(false);

        when(servicioReto.obtenerRetoPorId(retoId)).thenReturn(retoActual);
        when(servicioReto.obtenerRetoDisponible()).thenReturn(nuevoReto);

        // ejecución
        Reto resultado = servicioLogin.cambiarReto(retoId, usuario);

        // verificación
        assertNotNull(resultado, "El nuevo reto no debería ser nulo");
        assertEquals(nuevoReto.getId(), resultado.getId(), "El ID del nuevo reto debería coincidir");
        assertEquals(2, usuario.getCambioReto(), "El número de cambios restantes del usuario debería ser 2");
    }

    @Test
    public void queAlCambiarRetoUsuarioConCambiosDisponiblesSeLogreCorrectamente() {
        // preparación
        Long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setCambioReto(1);

        Reto retoActual = new Reto(retoId);
        Reto nuevoReto = new Reto(2L);

        when(servicioReto.obtenerRetoPorId(retoId)).thenReturn(retoActual);
        when(servicioReto.obtenerRetoDisponible()).thenReturn(nuevoReto);

        // ejecución
        Reto resultado = servicioLogin.cambiarReto(retoId, usuario);

        // verificación
        assertNotNull(resultado);
        assertEquals(nuevoReto, resultado);
        assertEquals(0, usuario.getCambioReto());
    }

    @Test
    public void alCambiarRetoElUsuarioSinCambiosDisponibles() {
        // preparación
        Long retoId = 1L;
        Usuario usuario = new Usuario();
        usuario.setCambioReto(0);

        // ejecución y verificación
        NoCambiosRestantesException exception = assertThrows(NoCambiosRestantesException.class, () -> {
            servicioLogin.cambiarReto(retoId, usuario);
        });
        assertEquals("No te quedan cambios. Debes terminar los retos.", exception.getMessage());

        verify(servicioReto, never()).obtenerRetoPorId(anyLong());
        verify(servicioReto, never()).obtenerRetoDisponible();
        verify(repositorioUsuario, never()).modificar(usuario);
    }

    @Test
    public void queConsultarUsuarioConEmailYPasswordCorrectosDevuelveUsuario() {
        // preparación
        String email = "test@example.com";
        String password = "password";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(password);
        when(repositorioUsuario.buscarUsuario(email, password)).thenReturn(usuario);

        // ejecución
        Usuario resultado = servicioLogin.consultarUsuario(email, password);

        // verificación
        assertNotNull(resultado);
        assertEquals(email, resultado.getEmail());
        assertEquals(password, resultado.getPassword());
    }

    @Test
    public void queRegistrarNuevoUsuarioSinConflictosFuncionaCorrectamente() throws UsuarioExistente {
        // preparación
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("password");
        when(repositorioUsuario.buscarUsuario(anyString(), anyString())).thenReturn(null);

        // ejecución
        servicioLogin.registrar(usuario);

        // verificación
        verify(repositorioUsuario).guardar(usuario);
    }

    @Test
    public void queRegistrarUsuarioExistenteLanzaUsuarioExistenteException() {
        // preparación
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("password");
        when(repositorioUsuario.buscarUsuario(anyString(), anyString())).thenReturn(usuario);

        // ejecución y verificación
        UsuarioExistente exception = assertThrows(UsuarioExistente.class, () -> {
            servicioLogin.registrar(usuario);
        });
        assertNotNull(exception);
        verify(repositorioUsuario, never()).guardar(any(Usuario.class));
    }

    @Test
    public void queObtenerItemMasSeleccionadoDesdeServicioCalendarioDevuelveCorrectamente() {
        // preparación
        DatosItemRendimiento datosItemRendimiento = new DatosItemRendimiento();
        when(servicioCalendario.obtenerItemMasSeleccionado()).thenReturn(datosItemRendimiento);

        // ejecución
        DatosItemRendimiento resultado = servicioLogin.obtenerItemMasSeleccionado();

        // verificación
        assertNotNull(resultado);
    }

    @Test
    public void queObtenerRetoDisponibleDesdeServicioRetoDevuelveCorrectamente() {
        // preparación
        Reto reto = new Reto();
        when(servicioReto.obtenerRetoDisponible()).thenReturn(reto);

        // ejecución
        Reto resultado = servicioLogin.obtenerRetoDisponible();

        // verificación
        assertNotNull(resultado);
    }

    @Test
    public void queObtenerRetoEnProcesoDesdeServicioRetoDevuelveCorrectamente() {
        // preparación
        Reto reto = new Reto();
        when(servicioReto.obtenerRetoEnProceso()).thenReturn(reto);

        // ejecución
        Reto resultado = servicioLogin.obtenerRetoEnProceso();

        // verificación
        assertNotNull(resultado);
    }

    @Test
    public void queCalcularTiempoRestanteDeRetoDevuelveCorrectamente() {
        // preparación
        Long retoId = 1L;
        long tiempoRestante = 1000L;
        when(servicioReto.calcularTiempoRestante(retoId)).thenReturn(tiempoRestante);

        // ejecución
        long resultado = servicioLogin.calcularTiempoRestante(retoId);

        // verificación
        assertEquals(tiempoRestante, resultado);
    }

    @Test
    public void queGuardarPerfilDeUsuarioFuncionaCorrectamente() {
        // preparación
        Usuario usuario = new Usuario();
        Perfil perfil = new Perfil();
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        // ejecución
        servicioLogin.guardarPerfil(usuario, perfil);

        // verificación
        verify(repositorioUsuario).modificar(usuario);
        verify(servicioPerfil).guardarPerfil(perfil);
    }

    @Test
    public void queGuardarObjetivoDeUsuarioFuncionaCorrectamente() {
        // preparación
        Usuario usuario = new Usuario();
        usuario.setObjetivo(Objetivo.PERDIDA_DE_PESO);

        // ejecución
        servicioLogin.guardarObjetivo(usuario, Objetivo.PERDIDA_DE_PESO);

        // verificación
        verify(repositorioUsuario).modificar(usuario);
    }

}
