package com.tallerwebi.dominio;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.RepositorioPerfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import com.tallerwebi.dominio.perfil.ServicioPerfilImpl;
import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.dominio.reto.ServicioRetoImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.ExpectedCount.times;

public class ServicioPerfilTest {

    private RepositorioPerfil repositorioPerfil;

    private ServicioPerfil servicioPerfil;

    @BeforeEach
    public void init() {
        this.repositorioPerfil = mock(RepositorioPerfil.class);
        this.servicioPerfil = new ServicioPerfilImpl(this.repositorioPerfil);
    }

    @Test
    public void queSePuedaGuardarUnPerfil() {
        // Crear un perfil de ejemplo
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPeso(70.0);
        perfil.setAltura(175);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("Ganancia Muscular");
        perfil.setCondicionesAlternas("Ninguna");
        perfil.setExperienciaEjercicio("Intermedio");
        perfil.setSuplementos("Proteína");

        // Simular el comportamiento del repositorio
        doNothing().when(repositorioPerfil).guardarPerfil(perfil);

        // Llamar al método del servicio
        servicioPerfil.guardarPerfil(perfil);

        // Verificar que el método del repositorio se llamó una vez
        verify(repositorioPerfil, atLeastOnce()).guardarPerfil(perfil);
    }

    @Test
    public void queSePuedaObtenerPerfilPorId() {
        // Crear un perfil de ejemplo
        Long idPerfil = 1L;
        Perfil perfilEsperado = new Perfil();
        perfilEsperado.setIdPerfil(idPerfil);
        perfilEsperado.setEdad(30);
        perfilEsperado.setPeso(70.0);
        perfilEsperado.setAltura(175);
        perfilEsperado.setGenero("Masculino");
        perfilEsperado.setObjetivoFitness("Ganancia Muscular");
        perfilEsperado.setCondicionesAlternas("Ninguna");
        perfilEsperado.setExperienciaEjercicio("Intermedio");
        perfilEsperado.setSuplementos("Proteína");

        // Simular el comportamiento del repositorio
        when(repositorioPerfil.obtenerPerfilPorId(idPerfil)).thenReturn(perfilEsperado);

        // Llamar al método del servicio
        Perfil perfilObtenido = servicioPerfil.obtenerPerfilPorId(idPerfil);

        // Verificar que el perfil devuelto coincide con el perfil esperado
        assertThat(perfilObtenido, equalTo(perfilEsperado));
    }

    @Test
    public void queSePuedaActualizarPerfil() {
        // Datos de prueba
        Long idPerfil = 1L;
        Perfil perfilExistente = new Perfil();
        perfilExistente.setIdPerfil(idPerfil);
        perfilExistente.setEdad(30);
        perfilExistente.setGenero("Masculino");

        // Simular el comportamiento del repositorio al buscar por id
        when(repositorioPerfil.obtenerPerfilPorId(idPerfil)).thenReturn(perfilExistente);

        // Crear un perfil actualizado
        Perfil perfilActualizado = new Perfil();
        perfilActualizado.setIdPerfil(idPerfil);
        perfilActualizado.setEdad(32); // Actualizar la edad

        // Llamar al método del servicio para actualizar el perfil
        servicioPerfil.actualizarPerfil(idPerfil, perfilActualizado);

        // Capturar el perfil pasado al método actualizarPerfil
        ArgumentCaptor<Perfil> perfilCaptor = ArgumentCaptor.forClass(Perfil.class);
        verify(repositorioPerfil).actualizarPerfil(perfilCaptor.capture());
    }


}
