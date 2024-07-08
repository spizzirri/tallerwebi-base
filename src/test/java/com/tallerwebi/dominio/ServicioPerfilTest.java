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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void dadoQueExisteUnLugarEnLaBaseDeDatosQueSePuedaGuardarUnPerfil() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(30);
        perfil.setPeso(70.0);
        perfil.setAltura(175);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("Ganancia Muscular");
        perfil.setCondicionesAlternas("Ninguna");
        perfil.setExperienciaEjercicio("Intermedio");
        perfil.setSuplementos("Proteína");


        doNothing().when(repositorioPerfil).guardarPerfil(perfil);

        // ejecución
        servicioPerfil.guardarPerfil(perfil);

        // verificación
        verify(repositorioPerfil, atLeastOnce()).guardarPerfil(perfil);
    }

    @Test
    public void dadoQueExisteUnPerfilGuardadoQueSePuedaObtenerPerfilPorId() {
        // preparación
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


        when(repositorioPerfil.obtenerPerfilPorId(idPerfil)).thenReturn(perfilEsperado);

        // ejecución
        Perfil perfilObtenido = servicioPerfil.obtenerPerfilPorId(idPerfil);

        // verificación
        assertThat(perfilObtenido, equalTo(perfilEsperado));
    }

    @Test
    public void dadoQueExisteUnPerfilGuardadoQueSePuedaActualizarPerfil() {
        // preparación
        Long idPerfil = 1L;
        Perfil perfilExistente = new Perfil();
        perfilExistente.setIdPerfil(idPerfil);
        perfilExistente.setEdad(30);
        perfilExistente.setGenero("Masculino");

        when(repositorioPerfil.obtenerPerfilPorId(idPerfil)).thenReturn(perfilExistente);

        Perfil perfilActualizado = new Perfil();
        perfilActualizado.setIdPerfil(idPerfil);
        perfilActualizado.setEdad(32);

        // ejecución
        servicioPerfil.actualizarPerfil(idPerfil, perfilActualizado);

        // verificación
        ArgumentCaptor<Perfil> perfilCaptor = ArgumentCaptor.forClass(Perfil.class);
        verify(repositorioPerfil).actualizarPerfil(perfilCaptor.capture());
    }

        @Test
        public void queSePuedaHacerLaRecomendacionPerdidaDePesoPrincipianteJoven() {
            // preparación
            Perfil perfil = new Perfil();
            perfil.setEdad(18);
            perfil.setPeso(65.0);
            perfil.setGenero("Masculino");
            perfil.setObjetivoFitness("PERDIDA_DE_PESO");
            perfil.setExperienciaEjercicio("principiante");
            perfil.setSuplementos("Ninguno");

            // ejecución
            String recomendacion = servicioPerfil.generarRecomendacion(perfil);

            // verificación
            assertEquals("Para perder peso siendo joven y principiante, mantén una dieta balanceada y comienza con ejercicios de baja intensidad. Aumenta gradualmente la intensidad y asegúrate de consumir suficientes nutrientes.", recomendacion);
        }

        @Test
        public void queSePuedaHacerLaRecomendacionGananciaMuscularIntermedio() {
            // preparación
            Perfil perfil = new Perfil();
            perfil.setEdad(25);
            perfil.setPeso(80.0);
            perfil.setGenero("Masculino");
            perfil.setObjetivoFitness("GANANCIA_MUSCULAR");
            perfil.setExperienciaEjercicio("intermedio");
            perfil.setSuplementos("Proteína");

            // ejecución
            String recomendacion = servicioPerfil.generarRecomendacion(perfil);

            // verificación
            assertEquals("Para ganar masa muscular como intermedio en tus 20s o 30s, aumenta tu ingesta calórica con proteínas y varía tus rutinas. Descansa adecuadamente para la recuperación muscular.", recomendacion);
        }

    @Test
    public void queSePuedaHacerLaRecomendacionDefinicionAvanzadoMayor() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(45);
        perfil.setPeso(75.0);
        perfil.setGenero("Femenino");
        perfil.setObjetivoFitness("DEFINICION");
        perfil.setExperienciaEjercicio("avanzado");
        perfil.setSuplementos("Creatina");

        // ejecución
        String recomendacion = servicioPerfil.generarRecomendacion(perfil);

        // verificación
        String expectedSubstring = "Para definir tus músculos como avanzado y mayor de 40, sigue una dieta equilibrada y realiza rutinas intensivas. Mantén la flexibilidad con ejercicios de bajo impacto.";
        assertTrue(recomendacion.contains(expectedSubstring));
    }

    @Test
    public void queSePuedaHacerLaRecomendacionPerdidaDePesoJoven() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(18);
        perfil.setPeso(70.0);
        perfil.setGenero("Femenino");
        perfil.setObjetivoFitness("PERDIDA_DE_PESO");
        perfil.setExperienciaEjercicio("principiante");
        perfil.setSuplementos("Ninguno");

        // ejecución
        String recomendacion = servicioPerfil.generarRecomendacion(perfil);

        // verificación
        assertTrue(recomendacion.contains("Para perder peso siendo joven y principiante"));
    }

    @Test
    public void queSePuedaHacerLaRecomendacionDefinicionAvanzado() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(50);
        perfil.setPeso(80.0);
        perfil.setGenero("Masculino");
        perfil.setObjetivoFitness("DEFINICION");
        perfil.setExperienciaEjercicio("avanzado");
        perfil.setSuplementos("Recuperativos");

        // ejecución
        String recomendacion = servicioPerfil.generarRecomendacion(perfil);

        // verificación
        assertTrue(recomendacion.contains("Para definir tus músculos como avanzado y mayor de 40"));
    }

    @Test
    public void queSePuedaHacerLaRecomendacionGeneralPrincipiante() {
        // preparación
        Perfil perfil = new Perfil();
        perfil.setEdad(25);
        perfil.setPeso(65.0);
        perfil.setGenero("Femenino");
        perfil.setObjetivoFitness("DEFINICION");
        perfil.setExperienciaEjercicio("principiante");
        perfil.setSuplementos("Ninguno");

        // ejecución
        String recomendacion = servicioPerfil.generarRecomendacion(perfil);

        // verificación
        assertTrue(recomendacion.contains("Como principiante, empieza con ejercicios de baja intensidad"));
    }


    }



