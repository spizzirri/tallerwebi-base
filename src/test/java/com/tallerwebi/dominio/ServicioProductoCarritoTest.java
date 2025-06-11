package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import junit.framework.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicioProductoCarritoTest {

    @Mock
    private ProductoCarritoDto productoMock1;
    @Mock
    private ProductoCarritoDto productoMock2;
    @Mock
    private RepositorioComponente repositorioComponente;

    private ServicioProductoCarritoImpl servicioProductoCarritoImpl;
    private List<ProductoCarritoDto> productos;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        servicioProductoCarritoImpl = new ServicioProductoCarritoImpl();
        ReflectionTestUtils.setField(servicioProductoCarritoImpl, "repositorioComponente", repositorioComponente);
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
    @Test
    public void cuandoAgregoUnProductoAlCarritoEsteSeAgregaCorrectamente() {
        servicioProductoCarritoImpl.setProductos(productos);
        when(productoMock1.getId()).thenReturn(1L);

        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(1L);

        when(repositorioComponente.obtenerComponentePorId(1L)).thenReturn(componenteMock);

        servicioProductoCarritoImpl.agregarProducto(componenteMock.getId(), 1);

        assertEquals(1, productos.size());
        assertEquals(1, servicioProductoCarritoImpl.getProductos().size());
    }

    @Test
    public void cuandoAgregoUnProductoExistenteSeSumaLaCantidadCorrectamente() {
        Componente componenteMock = mock(Componente.class);
        ProductoCarritoDto productoExistente = new ProductoCarritoDto(componenteMock, 2);
        productos.add(productoExistente);
        servicioProductoCarritoImpl.setProductos(productos);

        when(productoMock1.getId()).thenReturn(1L);

        servicioProductoCarritoImpl.agregarProducto(productoExistente.getId(), 2);

        assertEquals(1, productos.size());
        assertEquals(4, productos.get(0).getCantidad());
    }

    @Test
    public void cuandoAgregoUnComponenteInexistenteNoSeAgregaNada(){
        servicioProductoCarritoImpl.setProductos(productos);
        Long componenteInexistenteId = 1995L;

        when(repositorioComponente.obtenerComponentePorId(componenteInexistenteId)).thenReturn(null);

        servicioProductoCarritoImpl.agregarProducto(1995L, 1);

        assertEquals(0, productos.size());
        assertEquals(0, servicioProductoCarritoImpl.getProductos().size());
    }

    // verificarStock
    @Test
    public void cuandoVerificoElStockYAlcanzaObtengoTrue(){
        Long componenteId = 1L;
        Integer cantidadDeseada = 2;
        Integer stockDisponible = 10;


        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(componenteId);
        when(componenteMock.getStock()).thenReturn(stockDisponible);

        when(repositorioComponente.obtenerComponentePorId(componenteId)).thenReturn(componenteMock);

        boolean stockSuficiente = servicioProductoCarritoImpl.verificarStock(componenteMock.getId(), cantidadDeseada);

        assertTrue(stockSuficiente);
    }

    @Test
    public void cuandoVerificoStockYNoAlcanzaObtengoFalse() {
        Long componenteId = 1L;
        Integer cantidadDeseada = 2;
        Integer stockDisponible = 1;


        Componente componenteMock = mock(Componente.class);
        when(componenteMock.getId()).thenReturn(componenteId);
        when(componenteMock.getStock()).thenReturn(stockDisponible);

        when(repositorioComponente.obtenerComponentePorId(componenteId)).thenReturn(componenteMock);

        boolean stockSuficiente = servicioProductoCarritoImpl.verificarStock(componenteMock.getId(), cantidadDeseada);

        assertFalse(stockSuficiente);
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

    // calcularCantidadTotalDeProductos

}
