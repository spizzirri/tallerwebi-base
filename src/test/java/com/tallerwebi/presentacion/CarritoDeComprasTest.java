package com.tallerwebi.presentacion;

import org.dom4j.rule.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarritoDeComprasTest {

    public CarritoController carritoController;

    @BeforeEach
    public void init(){
        carritoController = new CarritoController();
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerLaVistaDelCarritoDeComprasObtengoLaVistaDelCarrito(){
        ModelAndView modelAndView =  carritoController.mostrarVistaCarritoDeCompras();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoAgregoUnProductoYMuestroLaVistaDelCarritoDeComprasObtengoLaVistaDelCarritoConUnProducto(){
        ProductoDto procesador = new ProductoDto("Procesador Intel Celeron G4900 3.10GHz Socket 1151 OEM Coffe Lake", 53.650);

        ModelAndView modelAndView =  carritoController.agregarProductoAlCarrito(procesador);
        String mensajeEsperado = "El producto fue agregado al carrito correctamente!";

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelAndView.getModel().get("mensaje"), equalTo(mensajeEsperado));
        assertThat(modelAndView.getModel().get("productoDto"), equalTo(procesador));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerEliminoUnProductoYMuestroLaVistaDelCarritoDeComprasObtengoLaVistaDelCarritoConUnProducto(){
        ProductoDto mouse = carritoController.getProductos().stream()
                .filter(producto -> producto.getNombre().equals("Mouse inalámbrico"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mouse no encontrado"));

        ModelAndView modelAndView =  carritoController.eliminarProductoDelCarrito(mouse.getId());

        List<ProductoDto> productosEsperados = carritoController.getProductos();

        String mensajeEsperado = "El producto fue eliminado!";

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertThat(modelAndView.getModel().get("mensaje"), equalTo(mensajeEsperado));
        assertThat(productosEsperados, not(hasItem(mouse)));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoObtengoElValorTotalDeLosProductos(){
        Double valorTotal = carritoController.calcularValorTotalDeLosProductos();

        Double valorEsperado = 109.98;

        assertThat(valorTotal, equalTo(valorEsperado));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel1PorcientoObtengoUnMensajeDeCodigoDeDescuentoInvalido(){
        String codigoDescuento = "PromoBarato1";
        ModelAndView valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(codigoDescuento);

        String mensajeEsperado = "Codigo de descuento invalido!";

        assertThat(valorTotalConDescuento.getModel().get("mensajeDescuento"), equalTo(mensajeEsperado));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel10PorcientoObtengoElValorTotalDeLosProductosConElDescuento(){
        String codigoDescuento = "CompraComponentes10";
        ModelAndView valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(codigoDescuento);

        Double valorEsperadoConDescuento = 98.99;
        Double valorObtenidoConDescuento = (Double) valorTotalConDescuento.getModel().get("valorTotalConDescuento");

        assertThat(valorObtenidoConDescuento, equalTo(valorEsperadoConDescuento));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel15PorcientoObtengoElValorTotalDeLosProductosConElDescuento(){
        String codigoDescuento = "baratija15";
        ModelAndView valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(codigoDescuento);

        Double valorEsperadoConDescuento = 93.49;

        Double valorObtenidoConDescuento = (Double) valorTotalConDescuento.getModel().get("valorTotalConDescuento");

        assertThat(valorObtenidoConDescuento, equalTo(valorEsperadoConDescuento));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDeSoloLetrasObtengoUnMensajeDeCodigoDeDescuentoInvalido(){
        String codigoDescuento = "descuentoABC";
        ModelAndView valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(codigoDescuento);

        String mensajeEsperado = "Codigo de descuento invalido!";

        assertThat(valorTotalConDescuento.getModel().get("mensajeDescuento"), equalTo(mensajeEsperado));
    }

//    @Test
//    public void dadoQueExisteUnCarritoControllerCuandoPresionoComprarSeActivaElModal() {
//        ModelAndView mv = carritoController.procesarCompra();
//        assertTrue(mv.getModel().containsKey("mostrarModal"));
//        assertEquals(true, mv.getModel().get("mostrarModal"));
//    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoPresionoElBotonMasDeUnoDeLosProductosObtengoUnoMasEnLaCantidadDeEseProducto() {
        ProductoDto mouse = carritoController.getProductos().stream()
                .filter(producto -> producto.getNombre().equals("Mouse inalámbrico"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mouse no encontrado"));

        Integer sumarCantidadProducto = Integer.valueOf(carritoController.agregarMasCantidadDeUnProducto(mouse.getId()));

        Integer cantidadEsperada = 3;

        assertThat(sumarCantidadProducto, equalTo(cantidadEsperada));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoPresionoElBotonMenosDeUnoDeLosProductosObtengoUnoMenosEnLaCantidadDeEseProducto() {
        ProductoDto mouse = carritoController.getProductos().stream()
                .filter(producto -> producto.getNombre().equals("Mouse inalámbrico"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mouse no encontrado"));

        Integer sumarCantidadProducto = Integer.valueOf(carritoController.restarCantidadDeUnProducto(mouse.getId()));

        Integer cantidadEsperada = 1;

        assertThat(sumarCantidadProducto, equalTo(cantidadEsperada));
    }



}
