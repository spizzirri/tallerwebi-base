package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ComponenteEspecificoDto;
import com.tallerwebi.presentacion.ControladorComponenteEspecifico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ControladorComponenteEspecificoTest {

    private ControladorComponenteEspecifico controladorComponenteEspecifico;
    private ServicioProductoEspecifico servicioProductoEspecifico;

    @BeforeEach
    public void init() {
        this.controladorComponenteEspecifico = new ControladorComponenteEspecifico(servicioProductoEspecifico);
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoPidoUnComponenteConID1MeDevuelveLaVistaDeUnComponenteEspecifico() {

        ModelAndView modelAndView = this.controladorComponenteEspecifico.mostrarComponenteEspecifico(1L);

        String vistaEsperada = "productoEspecifico";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoPidoUnComponenteConID1MeDevuelveLaVistaDeUnComponenteEspecificoYElModeloDeEseComponenteEspecifico() {

        ModelAndView modelAndView = this.controladorComponenteEspecifico.mostrarComponenteEspecifico(1L);

        String vistaEsperada = "productoEspecifico";
        ComponenteEspecificoDto componenteEsperado = new ComponenteEspecificoDto();
        componenteEsperado.setId(1L);
        componenteEsperado.setNombre("ComponenteEspecifico");

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(modelAndView.getModel().get("componenteEspecificoDto"), equalTo(componenteEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoEstoyEnLaVistaDeUnComponenteConID1PuedoVerificarQueElOffCanvasDeTerminosLlegueEnElModelo() {

        ModelAndView modelAndView = this.controladorComponenteEspecifico.mostrarComponenteEspecifico(1L);

        assertThat(modelAndView.getModel().get("terminos"), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoEstoyEnLaVistaDeUnComponenteConID1PuedoVerificarQueElOffCanvasDeCuotasLlegueEnElModelo() {

        ModelAndView modelAndView = this.controladorComponenteEspecifico.mostrarComponenteEspecifico(1L);

        assertThat(modelAndView.getModel().get("cuotas"), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoEstoyEnLaVistaDeUnComponenteConID1PuedoAgregarloAlCarritoYSeQuedaGuardadoYaQueMantieneLaSesion() {

        HttpSession sessionMock = mock(HttpSession.class);

        ComponenteEspecificoDto componente = new ComponenteEspecificoDto();
        componente.setId(1L);
        componente.setNombre("Componente");

        String resultado = this.controladorComponenteEspecifico.guardarComponenteEspecifico(1L, componente, sessionMock);

        verify(sessionMock).setAttribute("componenteEspecificoGuardado", componente);

        assertThat("redirect:/productoEspecifico/1", equalTo(resultado));
    }

}
