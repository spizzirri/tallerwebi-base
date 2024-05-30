package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.dominio.rutina.ServicioRutina;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ControladorRutinaTest {

    private Usuario usuarioMock;
    private ServicioRutina servicioRutina;
    private ControladorRutina controladorRutina;

    @BeforeEach
    public void init() {
        this.servicioRutina = mock(ServicioRutina.class);
        this.controladorRutina = new ControladorRutina(servicioRutina);
    }

    @Test
    public void queAlIrALaPantallaDeRutinaMeMuestreLaVistaDeRutina(){
        ModelAndView modelAndView = this.controladorRutina.irRutina();
        assertThat(modelAndView.getViewName(),equalTo("rutina"));
    }

    @Test
    public void QueAlIrALaVistaRutinasMeMuestreRutinasRelacionadasAMiObjetivo(){
        //preparacion
        usuarioMock = new Usuario("Lautaro", Objetivo.PERDIDA_DE_PESO);

        DatosRutina datosRutinaMock1 = new DatosRutina(new Rutina("Rutina de correr",Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock2 = new DatosRutina(new Rutina("Rutina de trotar",Objetivo.PERDIDA_DE_PESO));
        DatosRutina datosRutinaMock3 = new DatosRutina(new Rutina("Rutina de caminar",Objetivo.PERDIDA_DE_PESO));
        List<DatosRutina> datosRutinasMock = new ArrayList<>();
        datosRutinasMock.add(datosRutinaMock1);
        datosRutinasMock.add(datosRutinaMock2);
        datosRutinasMock.add(datosRutinaMock3);

        when(this.servicioRutina.getRutinasPorObjetivoDeUsuario(usuarioMock)).thenReturn(datosRutinasMock);

        //ejecucion
        ModelAndView modelAndView = this.controladorRutina.VerRutinasQueLeInteresanAlUsuario(usuarioMock);

        //verificacion
        List<DatosRutina> rutinasObtenidas = (List<DatosRutina>) modelAndView.getModel().get("rutinas");

        assertThat(modelAndView.getViewName(),equalTo("rutinas"));
        assertThat(rutinasObtenidas.size(),equalTo(3));
    }



}
