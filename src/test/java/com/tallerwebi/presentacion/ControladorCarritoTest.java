package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDeEnvios;
import com.tallerwebi.dominio.ServicioProductoCarrito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ControladorCarritoTest {

    @Mock
    private ProductoCarritoDto productoMock1;
    @Mock
    private ProductoCarritoDto productoMock2;
    @Mock
    private ServicioProductoCarrito servicioProductoCarritoMock;
    @Mock
    private ServicioDeEnvios servicioEnviosMock;

    private CarritoController carritoController;
    private List<ProductoCarritoDto> productos;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        carritoController = new CarritoController(servicioProductoCarritoMock, servicioEnviosMock);
        productos = new ArrayList<>();
    }

    @Test
    public void cuandoBuscoUnProductoPorIdObtengoEseProducto() {
        Long idBuscado = 1L;

        when(productoMock1.getId()).thenReturn(1L);

        productos.add(productoMock1);
        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);

        ProductoCarritoDto productoEncontrado = carritoController.buscarPorId(idBuscado);
        assertEquals(productoMock1, productoEncontrado);
    }

    @Test
    public void cuandoBuscoUnProductoPorIdQueNoExisteObtengoNull() {
        Long idProductoInexistente = 12L;

        when(productoMock1.getId()).thenReturn(1L);

        productos.add(productoMock1);
        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);

        ProductoCarritoDto productoNoEncontrado = carritoController.buscarPorId(idProductoInexistente);
        assertNull("El producto no existe!", productoNoEncontrado);
    }

    @Test
    public void cuandoElCarritoEstaVacioObtengoNull() {
        Long idBuscado = 1L;

        productos.add(productoMock1);
        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);

        ProductoCarritoDto productoEncontrado = carritoController.buscarPorId(idBuscado);
        assertNull("El producto no existe!", productoEncontrado);
    }

    @Test
    public void cuandoQuieroVerElCarritoDeComprasObtengoLaVistaDelCarrito() {
        ModelAndView mostrarVistaCarrito = carritoController.mostrarVistaCarritoDeCompras();
        assertThat(mostrarVistaCarrito.getViewName(), equalTo("carritoDeCompras"));
    }

    @Test
    public void cuandoQuieroEliminarUnProductoDelCarritoObtengoLaListaDeProductosDelCarritoSinEseProducto() {
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock2.getPrecio()).thenReturn(130.0);

        productos.add(productoMock1);
        productos.add(productoMock2);

        List<ProductoCarritoDto> productosSinUno = new ArrayList<>();
        productosSinUno.add(productoMock2);

        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos).thenReturn(productosSinUno);
        when(servicioProductoCarritoMock.calcularValorTotalDeLosProductos()).thenReturn(130.0);

        Map<String, Object> response = carritoController.eliminarProductoDelCarrito(productoMock1.getId());

        assertEquals(true, response.get("eliminado"));
        assertEquals(1, ((List<?>) response.get("productos")).size());
        assertEquals(130.0, response.get("valorTotal"));
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelCincoPorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado(){
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoMock.calcularDescuento(5)).thenReturn(313.5);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija5");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertEquals("Descuento aplicado! Nuevo total: $315.5", porcentajeDescuento.get("mensaje"));
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelDiezPorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado(){
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoMock.calcularDescuento(10)).thenReturn(297.0);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija10");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertEquals("Descuento aplicado! Nuevo total: $297.0", porcentajeDescuento.get("mensaje"));
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelQuincePorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado(){
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        when(servicioProductoCarritoMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoMock.calcularDescuento(15)).thenReturn(280.5);

        Map<String, String> inputConDescuento = new HashMap<>();
        inputConDescuento.put("codigoInput", "baratija15");

        Map<String, Object> porcentajeDescuento = carritoController.calcularValorTotalDeLosProductosConDescuento(inputConDescuento);

        assertEquals("Descuento aplicado! Nuevo total: $280.5", porcentajeDescuento.get("mensaje"));
    }

    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoInvalidoObtengoMensajeDeError() {
        Map<String, String> input = new HashMap<>();
        input.put("codigoInput", "descuento99");

        Map<String, Object> response = carritoController.calcularValorTotalDeLosProductosConDescuento(input);

        assertTrue(response.containsKey("mensajeDescuento"));
        assertEquals("Codigo de descuento invalido!", response.get("mensajeDescuento"));
    }

    @Test
    public void cuandoQuieroAgregarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {

        ServicioProductoCarrito servicioProductoCarritoReal = new ServicioProductoCarrito();
        // inicializo los productos por afuera con un metodo
        servicioProductoCarritoReal.init();
        CarritoController carritoControllerReal = new CarritoController(servicioProductoCarritoReal, servicioEnviosMock);

        Map<String, Object> response = carritoControllerReal.agregarMasCantidadDeUnProducto(1L);

        assertEquals(3, response.get("cantidad"));
        assertEquals(54000.0, response.get("valorTotal"));
        assertEquals(24000.0, response.get("precioTotalDelProducto"));
    }

    @Test
    public void cuandoQuieroRestarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {

        ServicioProductoCarrito servicioProductoCarritoReal = new ServicioProductoCarrito();
        servicioProductoCarritoReal.init();
        CarritoController carritoControllerReal = new CarritoController(servicioProductoCarritoReal, servicioEnviosMock);

        Map<String, Object> response = carritoControllerReal.restarCantidadDeUnProducto(1L);

        assertEquals(1, response.get("cantidad"));
        assertEquals(38000.0, response.get("valorTotal"));
        assertEquals(8000.0, response.get("precioTotalDelProducto"));
    }

    @Test
    public void cuandoSeleccionoElMetodoDePagoValidoObtengoUnMensajeDeExito(){
        String metodoPagoValido = "mercadoPago";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoValido);

        assertEquals(true, response.get("success"));
        assertNull(null, response.get("error"));
    }

    @Test
    public void cuandoElMetodoDePagoEsNullDebeRetornarError() {
        String metodoPagoNull = null;

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoNull);

        assertEquals(false, response.get("success"));
        assertEquals("Debes seleccionar un metodo de pago", response.get("error"));
        assertNull(response.get("metodoPago"));
    }

    @Test
    public void cuandoIngresoUnCodigoPostalValidoObtengoLaVistaDelCarritoConSusValores(){
        String codigoPostalValido = "1704";
        Double costoEnvio = 1500.0;
        Double totalCarrito = 10000.0;

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(costoEnvio);

        when(servicioEnviosMock.calcularEnvio(codigoPostalValido)).thenReturn(envioDto);
        when(servicioProductoCarritoMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoMock.calcularValorTotalDeLosProductos()).thenReturn(totalCarrito);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalValido);
        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals(true, modelMap.get("envioCalculado"));
        assertEquals(false, modelMap.get("sinCobertura"));
        assertEquals(totalCarrito + costoEnvio, modelMap.get("totalConEnvio"));
    }

    @Test
    public void cuandoIngresoUnCodigoPostalNoValidoObtengoErrorEnElEnvio(){
        String codigoPostalValido = "12";

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalValido);

        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals("El código postal debe tener 4 dígitos", modelMap.get("errorEnvio"));
        assertEquals(false, modelMap.get("envioCalculado"));
        assertEquals(false, modelMap.get("sinCobertura"));
    }

    @Test
    public void cuandoNoHayCoberturaParaElCodigoPostalMuestraMensaje() {
        when(servicioEnviosMock.calcularEnvio("9999")).thenReturn(null);

        ModelAndView modelAndView = carritoController.calcularEnvio("9999");

        ModelMap model = modelAndView.getModelMap();

        assertEquals(false, model.get("envioCalculado"));
        assertEquals(true, model.get("sinCobertura"));
        assertEquals("No disponemos de envío Andreani para este código postal", model.get("mensajeEnvio"));
    }
}
