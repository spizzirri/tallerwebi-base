package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.ServicioDeEnviosImpl;
import com.tallerwebi.dominio.ServicioProductoCarritoImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorCarritoTest {

    @Mock
    private ProductoCarritoDto productoMock1;
    @Mock
    private ProductoCarritoDto productoMock2;
    @Mock
    private ServicioProductoCarritoImpl servicioProductoCarritoImplMock;
    @Mock
    private ServicioDeEnviosImpl servicioEnviosMock;
    @Mock
    private RepositorioComponente repositorioMock;


    private CarritoController carritoController;
    private List<ProductoCarritoDto> productos;

    @BeforeEach
    public void init() {
        repositorioMock = mock(RepositorioComponente.class);
        MockitoAnnotations.openMocks(this);

        carritoController = new CarritoController(servicioProductoCarritoImplMock, servicioEnviosMock);
        productos = new ArrayList<>();
    }

    // mostrarVistaCarritoDeCompras
    @Test
    public void cuandoQuieroVerElCarritoDeComprasObtengoLaVistaDelCarrito() {
        ModelAndView mostrarVistaCarrito = carritoController.mostrarVistaCarritoDeCompras();
        assertThat(mostrarVistaCarrito.getViewName(), equalTo("carritoDeCompras"));
    }

    // eliminarProductoDelCarrito
    @Test
    public void cuandoQuieroEliminarUnProductoDelCarritoObtengoLaListaDeProductosDelCarritoSinEseProducto() {
        productos.add(productoMock1);
        productos.add(productoMock2);

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock2.getPrecio()).thenReturn(130.0);

        when(productoMock1.getId()).thenReturn(1L);
        when(productoMock2.getId()).thenReturn(2L);

        when(productoMock1.getCantidad()).thenReturn(1);
        when(productoMock2.getCantidad()).thenReturn(1);

        List<ProductoCarritoDto> productosSinUno = new ArrayList<>();
        productosSinUno.add(productoMock2);

        when(servicioProductoCarritoImplMock.buscarPorId(1L)).thenReturn(productoMock1);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos).thenReturn(productosSinUno);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(130.0);

        Map<String, Object> response = carritoController.eliminarProductoDelCarrito(productoMock1.getId());

        assertEquals(true, response.get("eliminado"));
        assertEquals(1, ((List<?>) response.get("productos")).size());
        assertEquals(130.0, response.get("valorTotal"));
    }

    // calcularValorTotalDeLosProductosConDescuento
    @Test
    public void cuandoIntroduzcoUnCodigoDeDescuentoDelCincoPorcientoObtengoElPrecioTotalDelCarritoConSuDescuentoAplicado(){
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);
        when(productoMock2.getPrecio()).thenReturn(130.0);
        when(productoMock1.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoImplMock.calcularDescuento(5)).thenReturn(313.5);

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

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoImplMock.calcularDescuento(10)).thenReturn(297.0);

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

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoImplMock.calcularDescuento(15)).thenReturn(280.5);

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

    // agregarMasCantidadDeUnProducto
    @Test
    public void cuandoQuieroAgregarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {
        ProductoCarritoDto productoMock = mock(ProductoCarritoDto.class);

        when(productoMock.getId()).thenReturn(1L);
        when(productoMock.getCantidad()).thenReturn(2).thenReturn(3);
        when(productoMock.getPrecio()).thenReturn(8000.0);

        when(servicioProductoCarritoImplMock.buscarPorId(1L)).thenReturn(productoMock);

        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(24000.0);
        servicioProductoCarritoImplMock.valorTotal = 24000.0;

        Map<String, Object> response = carritoController.agregarMasCantidadDeUnProducto(1L);

        assertEquals(3, response.get("cantidad"));
        assertEquals(24000.0, response.get("valorTotal"));
        assertEquals(24000.0, response.get("precioTotalDelProducto"));

        verify(productoMock).setCantidad(3);
        verify(servicioProductoCarritoImplMock).buscarPorId(1L);
        verify(servicioProductoCarritoImplMock).calcularValorTotalDeLosProductos();
    }

    // restarCantidadDeUnProducto
    @Test
    public void cuandoQuieroRestarUnaUnidadDeUnMismoProductoAlCarritoObtengoEseProductoConLaCantidadSumadaYElValorTotalDelCarritoActualizado() {
        ProductoCarritoDto productoMock = mock(ProductoCarritoDto.class);

        when(productoMock.getId()).thenReturn(1L);
        when(productoMock.getCantidad()).thenReturn(2).thenReturn(1);
        when(productoMock.getPrecio()).thenReturn(8000.0);

        when(servicioProductoCarritoImplMock.buscarPorId(1L)).thenReturn(productoMock);

        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(8000.0);
        servicioProductoCarritoImplMock.valorTotal = 8000.0;

        Map<String, Object> response = carritoController.restarCantidadDeUnProducto(1L);

        assertEquals(1, response.get("cantidad"));
        assertEquals(8000.0, response.get("valorTotal"));
        assertEquals(8000.0, response.get("precioTotalDelProducto"));

        verify(servicioProductoCarritoImplMock).buscarPorId(1L);
        verify(servicioProductoCarritoImplMock).calcularValorTotalDeLosProductos();
    }

    // procesarCompra
    @Test
    public void cuandoSeleccionoElMetodoDePagoValidoSinEnvioObtengoUnMensajeDeExito(){
        String metodoPagoValido = "mercadoPago";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoValido);

        assertEquals(true, response.get("success"));
        assertEquals(metodoPagoValido, response.get("metodoPago"));
        assertNull(null, response.get("error"));
        assertNull(null, response.get("costoEnvio"));
    }

    @Test
    public void cuandoSeleccionoElMetodoDePagoValidoConEnvioIncluyeCostoDeEnvio() {
        String metodoPagoValido = "mercadoPago";

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(1500.0);
        envioDto.setDestino("Ramos Mejia");

        carritoController.envioActual = envioDto;
        carritoController.codigoPostalActual = "1704";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoValido);

        assertTrue((Boolean) response.get("success"));
        assertEquals("mercadoPago", response.get("metodoPago"));
        assertEquals(1500.0, (Double) response.get("costoEnvio"));
    }

    @Test
    public void cuandoElMetodoDePagoEsNullDebeRetornarError() {
        String metodoPagoNull = null;

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoNull);

        assertEquals(false, response.get("success"));
        assertEquals("Debes seleccionar un metodo de pago", response.get("error"));
        assertNull(response.get("metodoPago"));
        assertNull(response.get("costoEnvio"));
    }

    @Test
    public void cuandoElMetodoDePagoEsVacioDebeRetornarError(){
        String metodoPagoVacio = "";

        Map<String, Object> response = carritoController.procesarCompra(metodoPagoVacio);

        assertEquals(false, response.get("success"));
        assertEquals("Debes seleccionar un metodo de pago", response.get("error"));
        assertNull(response.get("metodoPago"));
    }

    // calcularEnvio
    @Test
    public void cuandoIngresoUnCodigoPostalValidoObtengoLaVistaDelCarritoConSusValores(){
        String codigoPostalValido = "1704";
        Double costoEnvio = 1500.0;
        Double totalCarrito = 10000.0;

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(costoEnvio);
        envioDto.setDestino("Ramos Mejia");
        envioDto.setTiempo("1-2 dias habiles");

        when(servicioEnviosMock.calcularEnvio(codigoPostalValido)).thenReturn(envioDto);
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(totalCarrito);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalValido);
        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals(true, modelMap.get("envioCalculado"));
        assertEquals(false, modelMap.get("sinCobertura"));
        assertEquals(totalCarrito + costoEnvio, modelMap.get("totalConEnvio"));
        assertEquals(envioDto, modelMap.get("envio"));
    }

    @Test
    public void cuandoIngresoUnCodigoPostalNoValidoObtengoErrorEnElEnvio(){
        String codigoPostalInvalido = "12";

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido);

        ModelMap modelMap = (ModelMap) modelAndView.getModel();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals("El código postal debe tener 4 dígitos", modelMap.get("errorEnvio"));
        assertEquals(false, modelMap.get("envioCalculado"));
        assertEquals(false, modelMap.get("sinCobertura"));
        assertEquals(codigoPostalInvalido, modelMap.get("codigoPostal"));
    }

    @Test
    public void cuandoElCodigoPostalTieneMasDeCuatroNumerosDevuelveError(){
        String codigoPostalInvalido = "12345";

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido);
        ModelMap model = modelAndView.getModelMap();

        assertEquals("El código postal debe tener 4 dígitos", model.get("errorEnvio"));
        assertEquals(false, model.get("envioCalculado"));
        assertEquals(false, model.get("sinCobertura"));
    }

    @Test
    public void cuandoElCodigoPostalTieneLetrasDevuelveError(){
        String codigoPostalInvalido = "aa15";
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalInvalido);
        ModelMap model = modelAndView.getModelMap();

        assertEquals("El código postal debe tener 4 dígitos", model.get("errorEnvio"));
        assertEquals(false, model.get("envioCalculado"));
        assertEquals(false, model.get("sinCobertura"));
    }

    @Test
    public void cuandoNoHayCoberturaParaElCodigoPostalMuestraMensaje() {
        String codigoPostalSinCobertura = "9999";
        when(servicioEnviosMock.calcularEnvio(codigoPostalSinCobertura)).thenReturn(null);
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostalSinCobertura);
        ModelMap model = modelAndView.getModelMap();

        assertEquals(false, model.get("envioCalculado"));
        assertEquals(true, model.get("sinCobertura"));
        assertEquals("No disponemos de envío Andreani para este código postal", model.get("mensajeEnvio"));
        assertEquals(codigoPostalSinCobertura, model.get("codigoPostal"));
    }

    @Test
    public void cuandoElCodigoPostalEsNuloNoCalculaEnvio(){
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio("");
        ModelMap model = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals(null, model.get("envioCalculado"));
        assertEquals("", model.get("codigoPostal"));
    }

    @Test
    public void cuandoElCodigoPostalSoloTieneEspaciosNoSeCalculaElEnvio(){
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(" ");
        ModelMap model = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals(null ,model.get("envioCalculado"));
        assertEquals(" ", model.get("codigoPostal"));
    }

    @Test
    public void cuandoElServicioDeEnviosLanzaExcepcionDevuelveError(){
        String codigoPostal = "1704";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenThrow(new RuntimeException("Error"));
        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(List.of());
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(0.0);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostal);
        ModelMap model = modelAndView.getModelMap();

        assertEquals("Error al calcular envío. Intenta nuevamente.", model.get("errorEnvio"));
        assertEquals(false, model.get("envioCalculado"));
        assertEquals(false, model.get("sinCobertura"));
    }

    @Test
    public void siempreDevuelveLaVistaCorrectaConProductosYTotal(){
        String codigoPostal = "1704";
        List<ProductoCarritoDto> productos = Arrays.asList(new ProductoCarritoDto(1L, "Motherboard", 50000.0, 1, "motherboard"));
        Double total = productos.get(0).getPrecio();

        when(servicioProductoCarritoImplMock.getProductos()).thenReturn(productos);
        when(servicioProductoCarritoImplMock.calcularValorTotalDeLosProductos()).thenReturn(total);

        ModelAndView modelAndView = carritoController.calcularEnvio(codigoPostal);
        ModelMap model = modelAndView.getModelMap();

        assertThat(modelAndView.getViewName(), equalTo("carritoDeCompras"));
        assertEquals(total, model.get("valorTotal"));
        assertEquals(productos,  model.get("productos"));
        assertEquals(codigoPostal, model.get("codigoPostal"));
    }

    // calcularEnvioAjax
    @Test
    public void cuandoCalculoEnvioConCodigoValidoDevuelveEnvioExitoso(){
        String codigoPostal = "1704";
        Double valorTotalCarrito = 50000.0;

        EnvioDto envioDto = new EnvioDto();
        envioDto.setCosto(1500.0);
        envioDto.setDestino("Ramos Mejia");
        envioDto.setTiempo("2-3 dias habiles");

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenReturn(envioDto);
        servicioProductoCarritoImplMock.valorTotal = valorTotalCarrito;

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal);

        assertEquals(true, response.get("success"));
        assertEquals(envioDto.getCosto(), response.get("costo"));
        assertEquals(envioDto.getTiempo(), response.get("tiempo"));
        assertEquals(envioDto.getDestino(), response.get("destino"));
        assertEquals(valorTotalCarrito, response.get("valorTotal"));
    }

    @Test
    public void cuandoCalculoEnvioSinCoberturaDevuelveError(){
        String codigoPostal = "9999";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenReturn(null);

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal);

        assertEquals(false, response.get("success"));
        assertEquals("Sin cobertura para este código postal", response.get("mensaje"));
    }

    @Test
    public void cuandoElServicioLanzaUnaExepcionDevuelveError(){
        String codigoPostal = "1704";

        when(servicioEnviosMock.calcularEnvio(codigoPostal)).thenThrow(new RuntimeException("Error"));

        Map<String, Object> response = carritoController.calcularEnvioAjax(codigoPostal);

        assertEquals(false, response.get("success"));
        assertEquals("Error al calcular envío", response.get("mensaje"));
    }
}
