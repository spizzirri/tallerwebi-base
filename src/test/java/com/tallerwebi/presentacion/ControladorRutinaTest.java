package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;
import com.tallerwebi.dominio.rutina.ServicioRutinaImpl;
import com.tallerwebi.infraestructura.RepositorioRutinaImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ControladorRutinaTest {

    private Usuario usuarioMock;
    private ServicioRutina servicioRutina;
    private ControladorRutina controladorRutina;

    @BeforeEach
    public void init() {
        this.usuarioMock = mock(Usuario.class);
        this.servicioRutina = mock(ServicioRutina.class);
        this.controladorRutina = new ControladorRutina(this.servicioRutina);
    }

    @Test
    public void queAlIrALaPantallaDeRutinaMeMuestreLaVistaDeRutina(){
        ModelAndView modelAndView = this.controladorRutina.irRutina();
        assertThat(modelAndView.getViewName(),equalTo("rutina"));
    }

    @Test
    public void queAlIrALaPantallaDeRutinaSeMuestreLaVistaConUnaRutinaParaElUsuario(){
        //preparacion
        Usuario usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);
        DatosRutina datosRutinaMock = new DatosRutina(new Rutina("ADELGAZAR",Objetivo.PERDIDA_DE_PESO));

        when(this.servicioRutina.getRutinaParaUsuario(usuarioMock)).thenReturn(datosRutinaMock);

        //ejecucion
        ModelAndView modelAndView = this.controladorRutina.VerUnaRutinaEnLaPantallaRutina(usuarioMock);

        //verificacion
        DatosRutina rutina = (DatosRutina) modelAndView.getModel().get("rutina");

        assertThat(modelAndView.getViewName(),equalTo("rutina"));
        assertThat(rutina.getObjetivo(),equalTo(usuarioMock.getObjetivo()));
    }



}
