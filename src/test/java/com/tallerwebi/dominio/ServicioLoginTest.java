package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;

public class ServicioLoginTest {

    private RepositorioUsuario repositorioUsuario;
    private ServicioCalendario servicioCalendario;
    private ServicioLogin servicioLogin;
    private ServicioReto servicioReto;

    @BeforeEach
    public void init() {
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

}
