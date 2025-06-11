package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ProductoCarritoDto;
import junit.framework.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ServicioProductoCarritoTest {

    @Mock
    private ProductoCarritoDto productoMock1;
    @Mock
    private ProductoCarritoDto productoMock2;

    private ServicioProductoCarritoImpl servicioProductoCarritoImpl;
    private List<ProductoCarritoDto> productos;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        servicioProductoCarritoImpl = new ServicioProductoCarritoImpl();
        productos = new ArrayList<>();
    }

    // buscarPorId
    @Test
    public void cuandoBuscoUnProductoPorIdObtengoEseProducto() {
        Long idBuscado = 1L;
        servicioProductoCarritoImpl.setProductos(productos);

        when(productoMock1.getId()).thenReturn(1L);

        productos.add(productoMock1);


        ProductoCarritoDto productoEncontrado = servicioProductoCarritoImpl.buscarPorId(idBuscado);
        Assert.assertEquals(productoMock1, productoEncontrado);
    }

    @Test
    public void cuandoBuscoUnProductoPorIdQueNoExisteObtengoNull() {
        Long idProductoInexistente = 12L;
        servicioProductoCarritoImpl.setProductos(productos);

        when(productoMock1.getId()).thenReturn(1L);

        productos.add(productoMock1);

        ProductoCarritoDto productoNoEncontrado = servicioProductoCarritoImpl.buscarPorId(idProductoInexistente);
        assertNull(productoNoEncontrado);
    }

    @Test
    public void cuandoElCarritoEstaVacioObtengoNull() {
        Long idBuscado = 1L;
        servicioProductoCarritoImpl.setProductos(productos);

        ProductoCarritoDto productoEncontrado = servicioProductoCarritoImpl.buscarPorId(idBuscado);
        assertNull(productoEncontrado);
    }

    // agregarProducto
    public void cuandoAgregoUnProductoAlCarritoEsteSeAgregaCorrectamente() {
        Long idBuscado = 1L;
        Integer cantidad = 1;


    }


    @Test
    public void cuandoQuieroCalcularElValorTotalDeLosProductosEnElCarritoObtengoElTotal() {
        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);

        when(productoMock2.getPrecio()).thenReturn(50.0);
        when(productoMock2.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        servicioProductoCarritoImpl.setProductos(productos);

        Double valorTotal = servicioProductoCarritoImpl.calcularValorTotalDeLosProductos();
        // Cálculo esperado: (100*2) + (50*1) = 200 + 50 = 250.0
        assertEquals(250.0, valorTotal);
    }

    @Test
    public void cuandoQuieroCalcularElValorTotalDeLosProductosEnElCarritoCuandoNoHayProductosObtengoCero() {
        servicioProductoCarritoImpl.setProductos(new ArrayList<>());

        Double valorTotal = servicioProductoCarritoImpl.calcularValorTotalDeLosProductos();

        assertEquals(0.0, valorTotal);
    }

    @Test
    public void cuandoQuieroCalcularElValorTotalDeLosProductosEnElCarritoConDecimalesObtengoElTotalRedondeadoCorrectamente() {
        when(productoMock1.getPrecio()).thenReturn(123.241);
        when(productoMock1.getCantidad()).thenReturn(2);

        when(productoMock2.getPrecio()).thenReturn(50.34);
        when(productoMock2.getCantidad()).thenReturn(1);

        productos.add(productoMock1);
        productos.add(productoMock2);

        servicioProductoCarritoImpl.setProductos(productos);

        Double valorTotal = servicioProductoCarritoImpl.calcularValorTotalDeLosProductos();
        assertEquals(296.83, valorTotal);
    }

    @Test
    public void cuandoAplicoDescuentoDelCincoPorcientoObtengoElValorTotalDeLosProductosCorrectamenteCalculadoConSuDescuento(){
        Integer codigoDeDescuento = 5;

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);

        productos.add(productoMock1);

        servicioProductoCarritoImpl.setProductos(productos);

        Double valorTotalConDescuento = servicioProductoCarritoImpl.calcularDescuento(codigoDeDescuento);

        assertEquals(190.0, valorTotalConDescuento, 0.01);
    }

    @Test
    public void cuandoAplicoDescuentoDelDiezPorcientoObtengoElValorTotalDeLosProductosCorrectamenteCalculadoConSuDescuento(){
        Integer codigoDeDescuento = 10;

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);

        productos.add(productoMock1);

        servicioProductoCarritoImpl.setProductos(productos);

        Double valorTotalConDescuento = servicioProductoCarritoImpl.calcularDescuento(codigoDeDescuento);

        assertEquals(180.0, valorTotalConDescuento, 0.01);
    }

    @Test
    public void cuandoAplicoDescuentoDelQuincePorcientoObtengoElValorTotalDeLosProductosCorrectamenteCalculadoConSuDescuento(){
        Integer codigoDeDescuento = 15;

        when(productoMock1.getPrecio()).thenReturn(100.0);
        when(productoMock1.getCantidad()).thenReturn(2);

        productos.add(productoMock1);

        servicioProductoCarritoImpl.setProductos(productos);

        Double valorTotalConDescuento = servicioProductoCarritoImpl.calcularDescuento(codigoDeDescuento);

        assertEquals(170.0, valorTotalConDescuento, 0.01);
    }
}
