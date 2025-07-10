package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCategorias;
import com.tallerwebi.dominio.ServicioBuscarProducto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tallerwebi.dominio.ServicioPrecios;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ControladorIndexTest {

    @Mock
    private ServicioBuscarProducto productoServiceMock;
    @Mock
    private ProductoDto productoDtoMock;
    @Mock
    private ServicioCategorias categoriasServiceMock;
    @Mock
    private CategoriaDto categoriaDtoMock;

    @Mock
    private ServicioPrecios servicioPreciosMock;

    private ControladorIndex controladorIndex;
    private List<ProductoDto> productos;



    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.controladorIndex = new ControladorIndex(this.productoServiceMock, this.categoriasServiceMock, this.servicioPreciosMock);
        this.productos = new ArrayList<>();
    }
    @Test
    public void cuandoQuieroVerElIndexObtengoLaVistaDelIndex() {
        // Configurar el mock para que devuelva una lista de categorías
        List<CategoriaDto> categorias = Arrays.asList(categoriaDtoMock, Mockito.mock(CategoriaDto.class));
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(categorias);
        // Configurar el mock para productos en descuento
        Mockito.when(productoServiceMock.getProductosMenoresAUnPrecio(Mockito.anyDouble())).thenReturn(productos);
        // Ejecutar
        ModelAndView mostrarVistaIndex = controladorIndex.irAlIndex("");
        // Verificar
        assertThat(mostrarVistaIndex.getViewName(), equalTo("index"));
    }
    @Test
    public void CuandoLePasoUnIdParaCargarProductoMeDevuelveLaVistaCargarProductosDinamicos(){
        Integer id = 5;
        ModelAndView mostrarVistaCargarProductosDinamicos = controladorIndex.cargarProductosPorCategoria(id);

        assertThat(mostrarVistaCargarProductosDinamicos.getViewName(), equalTo("cargarProductosDinamicos"));
    }

    @Test
    public void CuandoLePasoUnNumeroParaElegirUnaCategoriaDestacadaMeDevuelveLaMismaCategoriaDelModelo(){
        List<CategoriaDto> categoriasMock = Arrays.asList(categoriaDtoMock, Mockito.mock(CategoriaDto.class)
        );
        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(categoriasMock);
        CategoriaDto categoriaEsperada = categoriasMock.get(0);
        ModelMap modeloObtenido = controladorIndex.irAlIndex("").getModelMap();
        CategoriaDto categoriaObtenida = (CategoriaDto) modeloObtenido.get("categoriaDestacada");

        assertThat(categoriaEsperada, Matchers.equalTo(categoriaObtenida));
    }

//    @Test
//    public void CuandoQuieroObtenerUnaListaDeProductosMenoresAUnPrecioObtengoEseModelo(){
//        List<CategoriaDto> categoriasMock = Arrays.asList(categoriaDtoMock, Mockito.mock(CategoriaDto.class)
//        );
//        Mockito.when(categoriasServiceMock.getCategorias()).thenReturn(categoriasMock);
//        List<ProductoDto> productosDescuento = Arrays.asList(productoDtoMock, Mockito.mock(ProductoDto.class));
//        Mockito.when(productoServiceMock.getProductosMenoresAUnPrecio(150000D)).thenReturn(productosDescuento);
//
//        ModelMap modeloObtenido = controladorIndex.irAlIndex().getModelMap();
//        List<ProductoDto> ListaObtenida = (List<ProductoDto>) modeloObtenido.get("productosDescuento");
//
//        assertThat(ListaObtenida, Matchers.equalTo(productosDescuento));
//
//    }


    @Test
    public void cuandoPasoElId2ObtengoProductosDelTipoProcesador() {
        // Arrange
        ProductoDto p1 = Mockito.mock(ProductoDto.class);
        ProductoDto p2 = Mockito.mock(ProductoDto.class);
        ProductoDto p3 = Mockito.mock(ProductoDto.class);
        ProductoDto p4 = Mockito.mock(ProductoDto.class);
        ProductoDto p5 = Mockito.mock(ProductoDto.class);
        ProductoDto p6 = Mockito.mock(ProductoDto.class);


        List<ProductoDto> productos = Arrays.asList(p1, p2, p3, p4, p5, p6);

        Mockito.when(productoServiceMock.getProductosPorTipo("Procesador")).thenReturn(productos);

        // Act
        ModelAndView mav = controladorIndex.cargarProductosPorCategoria(2);
        List<ProductoDto> resultado = (List<ProductoDto>) mav.getModelMap().get("productosDestacados");

        assertThat(resultado, equalTo(productos));
    }
}
