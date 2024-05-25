package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.RutinaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoTieneLaRutinaBuscadaException;
import com.tallerwebi.dominio.excepcion.UsuarioSinRutinasException;
import com.tallerwebi.dominio.objetivo.GrupoMuscularObjetivo;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.*;
import com.tallerwebi.presentacion.DatosRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ServicioRutinaTest {

    private ServicioRutina servicioRutina;
    private RepositorioRutina repositorioRutina;

    @BeforeEach
    public void init(){
        this.repositorioRutina = mock(RepositorioRutina.class);
        this.servicioRutina = new ServicioRutinaImpl(this.repositorioRutina);
    }

    @Test
    public void QueSePuedanObtenerTodasLasRutinas(){
        //p
        List<Rutina> rutinasMock = new ArrayList<>();
        rutinasMock.add(new Rutina("Rutina volumen 1",Objetivo.GANANCIA_MUSCULAR));
        rutinasMock.add(new Rutina("Rutina perdida de peso 1",Objetivo.PERDIDA_DE_PESO));
        rutinasMock.add(new Rutina("Rutina de definicion 1",Objetivo.DEFINICION));
        when(this.repositorioRutina.getRutinas()).thenReturn(rutinasMock);


        //e
        List<DatosRutina> rutinas = this.servicioRutina.getRutinas();

        //v
        assertThat(rutinas.size(),equalTo(3));
    }

    @Test
    public void QueSeObtengaUnaRutinaSegunObjetivoDeUsuario(){
        //p
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        Rutina rutinaMock = new Rutina("ADELGAZAR",Objetivo.PERDIDA_DE_PESO);

        when(this.repositorioRutina.getRutinaParaUsuario(usuarioMock)).thenReturn(rutinaMock);

        //e
        DatosRutina rutina = this.servicioRutina.getRutinaParaUsuario(usuarioMock);


        //v
        assertThat(rutina.getObjetivo(),equalTo(usuarioMock.getObjetivo()));
    }

    @Transactional
    @Rollback
    @Test
    public void QueArrojeExcepcionSiElUsuarioNoTieneNingunaRutinaAsignada() throws UsuarioSinRutinasException {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);

        //e
        when(this.repositorioRutina.getRutinasDeUsuario(usuario)).thenThrow(new UsuarioSinRutinasException());

        //v
        assertThrowsExactly(UsuarioSinRutinasException.class, () ->
                this.repositorioRutina.getRutinasDeUsuario(usuario)
        );
    }

    @Transactional
    @Rollback
    @Test
    public void QueSePuedaValidarElObjetivoDeLaRutinaYLaDelUsuarioAlAgregarUnaRutina()  {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = new Rutina("Rutina de volumen",Objetivo.GANANCIA_MUSCULAR);

        //e
        //when(this.repositorioRutina.buscarRutinaEnUsuario(rutina,usuario)).thenReturn(rutina);

        //v
        assertTrue(this.servicioRutina.validarObjetivosDeUsuarioYRutina(usuario,rutina));

    }

    @Transactional
    @Rollback
    @Test
    public void QueSePuedaPedirYValidarRutinasConElObjetivoQueTieneElUsuario()  {
        //p
        Usuario usuario = new Usuario("Lautaro", Objetivo.GANANCIA_MUSCULAR);
        Rutina rutina = new Rutina("Rutina de volumen",Objetivo.GANANCIA_MUSCULAR);

        //e
        //when(this.repositorioRutina.buscarRutinaEnUsuario(rutina,usuario)).thenReturn(rutina);

        //v
        assertTrue(this.servicioRutina.validarObjetivosDeUsuarioYRutina(usuario,rutina));

    }



}
