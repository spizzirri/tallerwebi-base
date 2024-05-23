package com.tallerwebi.dominio;

import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import com.tallerwebi.dominio.rutina.ServicioRutinaImpl;
import com.tallerwebi.presentacion.DatosRutina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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


}
