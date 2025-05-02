package com.tallerwebi.presentacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CarritoDeComprasTest {

    public ProductoController productoController;

    @BeforeEach
    public void init(){
        productoController = new ProductoController();
    }

    @Test
    public void dadoQueexisteUnProductoControllerCuandoQuieroVerLaVistaDelCarritoDeComprasObtengoLaVistaDelCarrito(){
//        ProductoDto procesador = new ProductoDto("Procesador Intel Celeron G4900 3.10GHz Socket 1151 OEM Coffe Lake", 53.650);

        ModelAndView modelAndView =  productoController.mostrarVistaCarritoDeCompras();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
    }
}
