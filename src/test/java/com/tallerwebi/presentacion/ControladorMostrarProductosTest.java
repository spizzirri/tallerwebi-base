package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCategorias;
import com.tallerwebi.dominio.ServicioBuscarProducto;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;


public class ControladorMostrarProductosTest {

    @Mock
    private ServicioBuscarProducto productoServiceMock;
    @Mock
    private ProductoDto productoDtoMock;
    @Mock
    private ServicioCategorias categoriasServiceMock;
    @Mock
    private CategoriaDto categoriaDtoMock;
    private ControladorMostrarProductos controladorMostrarProductos;
    private List<ProductoDto> productos;



    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.controladorMostrarProductos = new ControladorMostrarProductos(this.productoServiceMock, this.categoriasServiceMock);
        this.productos = new ArrayList<>();

    }
    @Test
    public void cuandoQuieroVerProductosObtengoLaVistaShowProductos() {

        ModelAndView vistaObtenida = controladorMostrarProductos.showProductos();

        assertThat(vistaObtenida.getViewName(), equalTo("productos"));
    }

    @Test
    public void cuandoQuieroBuscarProductosObtengoLaVistaProductos() throws ClassNotFoundException {

        ModelAndView vistaObtenida = controladorMostrarProductos.buscarProductos("","");

        assertThat(vistaObtenida.getViewName(), equalTo("productos"));
    }
    @Test
    public void cuandoQuieroObtenerUnMapaDeShowProductosPorLaKeyProductosLoObtengo(){
        List<ProductoDto> productosMock =  Arrays.asList(this.productoDtoMock,  Mockito.mock(ProductoDto.class));
        Mockito.when(this.productoServiceMock.getProductosEnStock()).thenReturn(productosMock);

        ModelMap modelObtenido = this.controladorMostrarProductos.showProductos().getModelMap();
        List<ProductoDto> productosObtenidos = (List<ProductoDto>) modelObtenido.get("productos");

        assertEquals(productosObtenidos, productosMock);
    }
    @Test
    public void cuandoQuieroObtenerUnMapaDeShowProductosPorLaKeyCategoriasLoObtengo(){
        List<CategoriaDto> categoriasMock = Arrays.asList(this.categoriaDtoMock, Mockito.mock(CategoriaDto.class));
        Mockito.when(this.categoriasServiceMock.getCategorias()).thenReturn(categoriasMock);

        ModelMap modelObtenido = this.controladorMostrarProductos.showProductos().getModelMap();
        List<CategoriaDto> categoriasObtenidos = (List<CategoriaDto>) modelObtenido.get("categorias");

        assertEquals(categoriasObtenidos, categoriasMock);
    }
    @Test
    public void cuandoBuscoSoloPorQuerySeLlamaGetProductosPorQuery() throws ClassNotFoundException {

        String query = "placa";
        String categoria = null;
        List<ProductoDto> mockProductos = Arrays.asList(Mockito.mock(ProductoDto.class));
        List<CategoriaDto> mockCategorias = Arrays.asList(Mockito.mock(CategoriaDto.class));

        Mockito.when(productoServiceMock.getProductosPorQuery(query)).thenReturn(mockProductos);
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(mockCategorias);


        ModelMap model = this.controladorMostrarProductos.buscarProductos(query, categoria).getModelMap();

        // Assert
        Mockito.verify(productoServiceMock).getProductosPorQuery(query);
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorCategoria(Mockito.any());
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorTipoYPorQuery(Mockito.any(), Mockito.any());
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosEnStock();


        assertThat((List<?>) model.get("productosBuscados"), equalTo(mockProductos));
    }
    @Test
    public void cuandoBuscoSoloPorCategoriaSeLlamaGetProductosPorCategoria() throws ClassNotFoundException {

        String query = null;
        String categoria = "Procesador";
        List<ProductoDto> mockProductos = Arrays.asList(Mockito.mock(ProductoDto.class));
        List<CategoriaDto> mockCategorias = Arrays.asList(Mockito.mock(CategoriaDto.class));

        Mockito.when(productoServiceMock.getProductosPorCategoria(categoria)).thenReturn(mockProductos);
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(mockCategorias);


        ModelMap model = this.controladorMostrarProductos.buscarProductos(query, categoria).getModelMap();

        // Assert
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorQuery(query);
        Mockito.verify(productoServiceMock).getProductosPorCategoria(categoria);
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorTipoYPorQuery(Mockito.any(), Mockito.any());
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosEnStock();


        assertThat((List<?>) model.get("productosBuscados"), equalTo(mockProductos));
    }
    @Test
    public void cuandoBuscoPorQueryYPorCategoriaMeBuscaPorEsasYNoPorSoloQueryOSoloCategoria() throws ClassNotFoundException {

        String query = "Intel";
        String categoria = "Procesador";
        List<ProductoDto> mockProductos = Arrays.asList(Mockito.mock(ProductoDto.class));
        List<CategoriaDto> mockCategorias = Arrays.asList(Mockito.mock(CategoriaDto.class));

        Mockito.when(productoServiceMock.getProductosPorTipoYPorQuery(categoria,query)).thenReturn(mockProductos);
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(mockCategorias);

        ModelMap model = this.controladorMostrarProductos.buscarProductos(query, categoria).getModelMap();

        // Assert
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorQuery(query);
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosPorCategoria(Mockito.any());
        Mockito.verify(productoServiceMock).getProductosPorTipoYPorQuery(categoria, query);
        Mockito.verify(productoServiceMock, Mockito.never()).getProductosEnStock();


        assertThat((List<?>) model.get("productosBuscados"), equalTo(mockProductos));
    }
    @Test
    public void cuandoBusquedaNoEncuentraProductosSeMuestraMensajeYProductosEnStock() throws ClassNotFoundException {
        String query = "inexistente";
        String categoria = null;

        List<ProductoDto> productosVacios = Collections.emptyList();
        List<ProductoDto> productosEnStock = Arrays.asList(Mockito.mock(ProductoDto.class));
        List<CategoriaDto> categorias = Arrays.asList(Mockito.mock(CategoriaDto.class));

        Mockito.when(productoServiceMock.getProductosPorQuery(query)).thenReturn(productosVacios);
        Mockito.when(productoServiceMock.getProductosEnStock()).thenReturn(productosEnStock);
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(categorias);

        ModelMap model = this.controladorMostrarProductos.buscarProductos(query, categoria).getModelMap();

        assertThat(model.get("productosBuscados"), equalTo(false));
        assertThat(model.get("mensaje"), equalTo("No se encontraron productos para esa búsqueda. Mostrando todos los productos."));

    }

}
