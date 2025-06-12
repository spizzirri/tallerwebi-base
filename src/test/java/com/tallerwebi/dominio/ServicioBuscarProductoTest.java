package com.tallerwebi.dominio;

import com.tallerwebi.dominio.ServicioBuscarProducto;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.PlacaDeVideo;
import com.tallerwebi.dominio.entidades.Procesador;
import com.tallerwebi.presentacion.ProductoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioBuscarProductoTest {

    private RepositorioComponente repositorioComponenteMock;
    private ServicioBuscarProducto servicio;

    @BeforeEach
    public void init() {
        repositorioComponenteMock = mock(RepositorioComponente.class);
        servicio = new ServicioBuscarProducto(repositorioComponenteMock);
    }
    @Test
    public void CuandoLePidoUnaListaDeProductosMeDevuelveUnaListaDeProductos() {
        Componente componente = new Componente();
        componente.setNombre("Producto de prueba");
        when(repositorioComponenteMock.obtenerComponentes()).thenReturn(List.of(componente));

        servicio = new ServicioBuscarProducto(repositorioComponenteMock);
        List<ProductoDto> listaObtenida = servicio.getProductos();

        assertFalse(listaObtenida.isEmpty());
        assertEquals("Producto de prueba", listaObtenida.get(0).getNombre());
    }
    @Test
    public void CuandoLePidoUnComponenteLLamadoProcesadordeberiaDevolverProductosPorTipoProcesador() {
        Componente componente = new Procesador();
        when(repositorioComponenteMock.obtenerComponentesPorTipo("Procesador")).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosPorTipo("Procesador");
        ProductoDto productoDtoObtenido = resultado.get(0);
        String claseObtenida = productoDtoObtenido.getTipoComponente();
        assertEquals("Procesador", claseObtenida);
    }

    @Test
    public void CuandoPidoUnaListaDeProductosMenoresACienMilMeDeberiaDevolverProductosMenoresACienMil() {
        Componente componente = new Componente();
        componente.setPrecio(50000D);
        when(repositorioComponenteMock.obtenerComponentesMenoresDelPrecioPorParametro(100000.0)).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosMenoresAUnPrecio(100000.0);
        Double precioObtenido = resultado.get(0).getPrecio();

        assertFalse(resultado.isEmpty());
        assertTrue(precioObtenido<100000D);
    }

    @Test
    public void CuandoPidoUnaListaDeProductosEnStockDeberiaDevolvermeProductosEnStockMayoresACero() {
        Componente componente = new Componente();
        componente.setStock(10);
        when(repositorioComponenteMock.obtenerComponentesEnStock()).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosEnStock();
        Integer stockObtenido = resultado.get(0).getStock();

        assertFalse(resultado.isEmpty());
        assertTrue(stockObtenido>0);
    }
    @Test
    public void CuandoBuscoUnProductoPorQueryAlNombreDeIntelDeberiaDevolverProductosPorQueryConElNombreIntel() {
        Componente componente = new Componente();
        componente.setNombre("intel");
        when(repositorioComponenteMock.obtenerComponentesPorQuery("intel")).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosPorQuery("intel");
        String nombreObtenido = resultado.get(0).getNombre();

        assertFalse(resultado.isEmpty());
        assertEquals("intel", nombreObtenido);
    }
    @Test
    public void CuandoLePidoUnComponentePorCategoriadeberiaDevolverProductosDeEsaCategoria() {
        Componente componente = new PlacaDeVideo();
        when(repositorioComponenteMock.obtenerComponentesPorTipo("PlacaDeVideo")).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosPorCategoria("PlacaDeVideo");
        ProductoDto productoDtoObtenido = resultado.get(0);

        String claseObtenida = productoDtoObtenido.getTipoComponente();
        assertEquals("PlacaDeVideo", claseObtenida);
    }

    @Test
    public void deberiaDevolverProductosPorTipoYPorQuery() throws ClassNotFoundException {
        Componente componente = new Procesador();
        componente.setNombre("Intel");

        when(repositorioComponenteMock.obtenerComponentesPorTipoYPorQuery("Procesador", "Intel")).thenReturn(Arrays.asList(componente));

        List<ProductoDto> resultado = servicio.getProductosPorTipoYPorQuery("Procesador", "Intel");
        ProductoDto productoDtoObtenido = resultado.get(0);
        String claseObtenida = productoDtoObtenido.getTipoComponente();
        String nombre = productoDtoObtenido.getNombre();
        assertEquals("Procesador", claseObtenida);
        assertEquals("Intel", nombre);
    }



}