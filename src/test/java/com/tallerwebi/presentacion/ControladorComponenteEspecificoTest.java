package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDolar;
import com.tallerwebi.dominio.ServicioProductoEspecifico;
import com.tallerwebi.dominio.entidades.Componente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

public class ControladorComponenteEspecificoTest {

    private ControladorComponenteEspecifico controladorComponenteEspecifico;
    private ServicioProductoEspecifico servicioProductoEspecifico;
    private ServicioDolar servicioDolar;

    @BeforeEach
    public void init() {
        servicioProductoEspecifico = mock(ServicioProductoEspecifico.class);
        servicioDolar = mock(ServicioDolar.class);
        this.controladorComponenteEspecifico = new ControladorComponenteEspecifico(servicioProductoEspecifico, servicioDolar);
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoPidoUnComponenteConID1MeDevuelveLaVistaDeUnComponenteEspecifico() {

        Long idComponente = 1L;
        Componente componenteMock = new Componente();
        componenteMock.setId(1L);
        componenteMock.setPrecio(10000.0);

        when(servicioProductoEspecifico.obtenerComponentePorId(idComponente)).thenReturn(componenteMock);
        when(servicioDolar.obtenerCotizacionDolarBlue()).thenReturn(1200.0);

        ModelAndView modelAndView = controladorComponenteEspecifico.mostrarComponenteEspecifico(idComponente);

        String vistaEsperada = "productoEspecifico";

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoPidoUnComponenteConID1MeDevuelveLaVistaDeUnComponenteEspecificoYElModeloDeEseComponenteEspecifico() {

        Long idComponente = 1L;
        Componente componenteMock = new Componente();
        componenteMock.setId(1L);
        componenteMock.setPrecio(10000.0);

        when(servicioProductoEspecifico.obtenerComponentePorId(idComponente)).thenReturn(componenteMock);
        when(servicioDolar.obtenerCotizacionDolarBlue()).thenReturn(1200.0);

        ModelAndView modelAndView = controladorComponenteEspecifico.mostrarComponenteEspecifico(idComponente);

        String vistaEsperada = "productoEspecifico";

        ComponenteEspecificoDto componenteEsperado = new ComponenteEspecificoDto();
        componenteEsperado.setId(1L);

        assertThat(modelAndView.getViewName(), equalTo(vistaEsperada));
        assertThat(modelAndView.getModel().get("componenteEspecificoDto"), equalTo(componenteEsperado));
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoEstoyEnLaVistaDeUnComponenteConID1PuedoVerificarQueElOffCanvasDeTerminosLlegueEnElModelo() {

        Long idComponente = 1L;
        Componente componenteMock = new Componente();
        componenteMock.setId(1L);
        componenteMock.setPrecio(10000.0);

        when(servicioProductoEspecifico.obtenerComponentePorId(idComponente)).thenReturn(componenteMock);

        ModelAndView modelAndView = controladorComponenteEspecifico.mostrarComponenteEspecifico(idComponente);

        assertThat(modelAndView.getModel().get("terminos"), notNullValue());
    }

    @Test
    public void dadoQueExisteUnControladorComponenteEspecificoCuandoEstoyEnLaVistaDeUnComponenteConID1PuedoVerificarQueElOffCanvasDeCuotasLlegueEnElModelo() {

        Long idComponente = 1L;
        Componente componenteMock = new Componente();
        componenteMock.setId(1L);
        componenteMock.setPrecio(10000.0);

        when(servicioProductoEspecifico.obtenerComponentePorId(idComponente)).thenReturn(componenteMock);

        ModelAndView modelAndView = controladorComponenteEspecifico.mostrarComponenteEspecifico(idComponente);

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
