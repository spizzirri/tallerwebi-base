package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDolar;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.ServicioProductoEspecifico;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Procesador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ControladorComponenteEspecificoTest {

    private ControladorComponenteEspecifico controladorComponenteEspecifico;
    private ServicioProductoEspecifico servicioProductoEspecifico;
    private ServicioDolar servicioDolar;
    private ServicioPrecios servicioPrecios;

    @BeforeEach
    public void init() {
        servicioProductoEspecifico = mock(ServicioProductoEspecifico.class);
        servicioDolar = mock(ServicioDolar.class);
        servicioPrecios = mock(ServicioPrecios.class);
        this.controladorComponenteEspecifico = new ControladorComponenteEspecifico(servicioProductoEspecifico, servicioDolar, servicioPrecios);
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

    /*@Test
    void cuandoLlamoMostrarComponenteEspecificoDeberiaRetornarModelAndViewCorrectamente() {
        Long id = 1L;
        Double precio = 250.0;

        Componente componenteMock = new Procesador();
        componenteMock.setId(1L);
        componenteMock.setNombre("Intel Core i5");
        componenteMock.setPrecio(250.0);

        List<ComponenteEspecificoDto> componentesComparar = new ArrayList<>();

        Procesador procesador1 = new Procesador();
        procesador1.setId(2L);
        procesador1.setNombre("AMD Ryzen 5");
        procesador1.setPrecio(200.0);

        Procesador procesador2 = new Procesador();
        procesador2.setId(3L);
        procesador2.setNombre("Intel Core i7");
        procesador2.setPrecio(300.0);

        componentesComparar.add(new ComponenteEspecificoDto(procesador1));
        componentesComparar.add(new ComponenteEspecificoDto(procesador2));

        when(servicioProductoEspecifico.obtenerComponentePorId(id)).thenReturn(componenteMock);
        when(servicioProductoEspecifico.obtenerComponentesAcomparar(id)).thenReturn(componentesComparar);
        when(servicioPrecios.obtenerPrecioFormateado(precio)).thenReturn("$250.00");


        ModelAndView resultado = controladorComponenteEspecifico.mostrarComponenteEspecifico(id);

        assertEquals("productoEspecifico", resultado.getViewName());

        Map<String, Object> model = resultado.getModel();
        assertNotNull(model);

        assertEquals(componentesComparar, model.get("componentesEspecifico"));
        assertNotNull(model.get("componenteEspecificoDto"));
        assertEquals("$250.00", model.get("precioFormateado"));

        verify(servicioProductoEspecifico, times(1)).obtenerComponentePorId(id);
        verify(servicioProductoEspecifico, times(1)).obtenerComponentesAcomparar(id);
        verify(servicioPrecios, times(1)).obtenerPrecioFormateado(precio);
    }*/

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
