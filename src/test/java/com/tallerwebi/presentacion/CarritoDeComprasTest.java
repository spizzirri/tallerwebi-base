package com.tallerwebi.presentacion;

import org.dom4j.rule.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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

        Map<String, Object> modelAndView =  carritoController.eliminarProductoDelCarrito(mouse.getId());

        List<ProductoDto> productosEsperados = carritoController.getProductos();

        assertThat(productosEsperados, not(hasItem(mouse)));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoObtengoElValorTotalDeLosProductos(){
        Double valorTotal = carritoController.calcularValorTotalDeLosProductos();

        Double valorEsperado = 219.96;

        assertThat(valorTotal, equalTo(valorEsperado));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel1PorcientoObtengoUnMensajeDeCodigoDeDescuentoInvalido(){
        Map<String, String> requestBody = new HashMap<>();

        String codigoDescuento = "CompraComponentes10";
        requestBody.put("codigoDescuento", codigoDescuento);

        Map<String, String> valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(requestBody);

        String mensajeEsperado = "Codigo de descuento invalido!";

        assertThat(valorTotalConDescuento.get("mensajeDescuento"), equalTo(mensajeEsperado));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel10PorcientoObtengoElValorTotalDeLosProductosConElDescuento(){
        Map<String, String> requestBody = new HashMap<>();

        String codigoDescuento = "CompraComponentes10";
        requestBody.put("codigoInput", codigoDescuento);

        Map<String, String> valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(requestBody);

        String mensajeEsperado = valorTotalConDescuento.get("mensaje");
        String[] separarMensaje = mensajeEsperado.split(": ");

        Double valorEsperadoConDescuento = 197.97;
        Double valorObtenidoConDescuento = Double.parseDouble(separarMensaje[1]);

        assertThat(valorObtenidoConDescuento, equalTo(valorEsperadoConDescuento));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDel15PorcientoObtengoElValorTotalDeLosProductosConElDescuento(){
        Map<String, String> requestBody = new HashMap<>();

        String codigoDescuento = "baratija15";
        requestBody.put("codigoInput", codigoDescuento);

        Map<String, String> valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(requestBody);

        String mensajeEsperado = valorTotalConDescuento.get("mensaje");
        String[] separarMensaje = mensajeEsperado.split(": ");

        Double valorEsperadoConDescuento = 186.97;
        Double valorObtenidoConDescuento = Double.parseDouble(separarMensaje[1]);

        assertThat(valorObtenidoConDescuento, equalTo(valorEsperadoConDescuento));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoQuieroVerElValorTotalDelosProductosEnElCarritoAplicandoUnCodigoDeDescuentoDeSoloLetrasObtengoUnMensajeDeCodigoDeDescuentoInvalido(){
        Map<String, String> requestBody = new HashMap<>();

        String codigoDescuento = "descuentoABC";
        requestBody.put("codigoDescuento", codigoDescuento);

        Map<String, String> valorTotalConDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(requestBody);

        String mensajeEsperado = "Codigo de descuento invalido!";

        assertThat(valorTotalConDescuento.get("mensajeDescuento"), equalTo(mensajeEsperado));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoPresionoElBotonMasDeUnoDeLosProductosObtengoUnoMasEnLaCantidadDeEseProducto() {
        ProductoDto mouse = carritoController.getProductos().stream()
                .filter(producto -> producto.getNombre().equals("Mouse inalámbrico"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mouse no encontrado"));

        Integer mouseCantidad = mouse.getCantidad();

        carritoController.agregarMasCantidadDeUnProducto(mouse.getId());

        Integer cantidadObtenida = mouse.getCantidad();;
        Integer cantidadEsperada = mouseCantidad + 1;

        assertThat(cantidadObtenida, equalTo(cantidadEsperada));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoPresionoElBotonMenosDeUnoDeLosProductosObtengoUnoMenosEnLaCantidadDeEseProducto() {
        ProductoDto mouse = carritoController.getProductos().stream()
                .filter(producto -> producto.getNombre().equals("Mouse inalámbrico"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mouse no encontrado"));

        Integer mouseCantidad = mouse.getCantidad();

        carritoController.restarCantidadDeUnProducto(mouse.getId());

        Integer cantidadObtenida = mouse.getCantidad();;
        Integer cantidadEsperada = mouseCantidad - 1;

        assertThat(cantidadObtenida, equalTo(cantidadEsperada));
    }

    @Test
    public void dadoQueExisteUnCarritoControllerCuandoPresionoComprarSinSeleccionarElMetodoDePagoNoSeActivaElModal() {
        ModelAndView modelAndView = carritoController.procesarCompra("");
        assertTrue(modelAndView.getModel().containsKey("error"));
        assertEquals("Debes seleccionar un metodo de pago", modelAndView.getModel().get("error"));
        assertEquals(false, modelAndView.getModel().get("mostrarModal"));
    }
}
