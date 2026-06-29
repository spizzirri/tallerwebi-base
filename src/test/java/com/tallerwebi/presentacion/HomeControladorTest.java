package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Productos.CategoriaProductos;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.ServicioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.ProductoNoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;

public class HomeControladorTest {

  private HomeControlador homeControlador;
  private Usuario usuarioMock;
  private HttpSession sessionMock;
  private Producto productoMock;
  private Producto productoMock2;

  private CategoriaProductos categoriaProductoMock;
  private ServicioProducto servicioProductoMock;
  private ServicioCarrito servicioCarritoMock;

  @BeforeEach
  public void init() {
    servicioProductoMock = Mockito.mock(ServicioProducto.class);
    servicioCarritoMock = Mockito.mock(ServicioCarrito.class); // ← primero esto
    homeControlador = new HomeControlador(servicioProductoMock, servicioCarritoMock);
    usuarioMock = Mockito.mock(Usuario.class);
    productoMock = Mockito.mock(Producto.class);
    productoMock2 = Mockito.mock(Producto.class);
    sessionMock = Mockito.mock(HttpSession.class);
    categoriaProductoMock = Mockito.mock(CategoriaProductos.class);
    Carrito carritoMock = Mockito.mock(Carrito.class);
    when(carritoMock.getItems()).thenReturn(new ArrayList<>());
    when(servicioCarritoMock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("CLIENTE");
  }

  @Test
  public void siNoHayUsuarioLogueadoDebeVolverALogin() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, null);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void elHomeDebeMostrarNombreDeUsuario() {
    //preparacion
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(usuarioMock.getNombre()).thenReturn("Rocio");

    //ejecucion
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, null);

    //validacion

    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getNombre(),
      equalToIgnoringCase("Rocio")
    );
  }

  @Test
  public void elHomeDebeMostrarFotoDePerfilDelUsuario() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(usuarioMock.getFotoPerfil()).thenReturn("fotoUsuario.jpeg");

    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, null);

    assertThat(
      ((Usuario) modelAndView.getModel().get("usuario")).getFotoPerfil(),
      equalToIgnoringCase("fotoUsuario.jpeg")
    );
  }

  @Test
  public void seDebeVerElListadoDeProductosCompleto() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    //preparacion
    when(productoMock.getNombre()).thenReturn("Mogul");

    List<Producto> productos = List.of(productoMock);

    when(servicioProductoMock.obtenerListadoProductos()).thenReturn(productos);
    //ejecucion
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, null);
    //validacion
    List<Producto> productosObtenidos = (List<Producto>) modelAndView.getModel().get("productos");

    assertThat(productosObtenidos.get(0).getNombre(), equalToIgnoringCase("Mogul"));
  }

  @Test
  public void siNoHayProductosCargadosEnLaBD_seDebeMostrarMensajeCorrespondiente() {
    String mensajeException = "No se encontraron productos en la base de datos";
    // preparacion
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(servicioProductoMock.obtenerListadoProductos())
      .thenThrow(new ProductoNoEncontradoException(mensajeException));

    // ejecucion
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, null);

    // validacion
    String mensajeError = (String) modelAndView.getModel().get("errorCargaProductos");
    assertThat(mensajeError, equalToIgnoringCase(mensajeException));
  }

  @Test
  public void seDebenVisualizarTodasLasCategorias() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(categoriaProductoMock.getNombreCategoria()).thenReturn("algo");

    List<CategoriaProductos> categoriasSimuladas = List.of(categoriaProductoMock);

    when(servicioProductoMock.obtenerListadoCategorias()).thenReturn(categoriasSimuladas);

    ModelAndView mv = homeControlador.irAHome(sessionMock, null, null);

    //validacion
    List<CategoriaProductos> catObtenidos = (List<CategoriaProductos>) mv
      .getModel()
      .get("categorias");

    assertThat(catObtenidos.get(0).getNombreCategoria(), equalToIgnoringCase("algo"));
  }

  @Test
  public void SeDebenVerListadoProductosFiltradoPorCategoria() {
    String categoria = "golo";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    when(productoMock.getNombre()).thenReturn("Mogul");
    when(categoriaProductoMock.getNombreCategoria()).thenReturn(categoria);

    when(productoMock.getCategoria()).thenReturn(categoriaProductoMock);

    List<Producto> productosFiltrados = List.of(productoMock);

    when(servicioProductoMock.obtenerListadoProductosFiltrado(categoria))
      .thenReturn(productosFiltrados);

    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, categoria, null);
    List<Producto> productosObtenidos = (List<Producto>) modelAndView.getModel().get("productos");
    assertThat(productosObtenidos.get(0).getNombre(), equalToIgnoringCase("Mogul"));
    assertThat(
      productosObtenidos.get(0).getCategoria().getNombreCategoria(),
      equalToIgnoringCase(categoria)
    );

    verify(servicioProductoMock, times(1)).obtenerListadoProductosFiltrado(categoria);
  }

  @Test
  public void siNoHayProductosEnUnaCategoria_seDebeMostrarMensajeError() {
    String mensajeException = "No se encontraron productos en esta categoria";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(servicioProductoMock.obtenerListadoProductosFiltrado("categoria"))
      .thenThrow(new ProductoNoEncontradoException(mensajeException));
    //ejecucion
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, "categoria", null);

    //validacion
    String mensajeError = (String) modelAndView.getModel().get("errorCargaProductos");
    assertThat(mensajeError, equalToIgnoringCase(mensajeException));
  }

  @Test
  public void alBuscarUnProductoPorNombreDebeTraerLasCoincidencias() {
    String texto = "cuad";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    //carga simulada de productos
    when(productoMock.getNombre()).thenReturn("Cuaderno Universitario");
    when(productoMock2.getNombre()).thenReturn("Cuaderno Exito");

    List<Producto> productosBuscados = List.of(productoMock, productoMock2);
    when(servicioProductoMock.buscarProductosPorNombre(texto)).thenReturn(productosBuscados);

    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, texto);

    List<Producto> productosObtenidos = (List<Producto>) modelAndView
      .getModel()
      .get("productosBuscados");

    assertThat(productosObtenidos.size(), org.hamcrest.Matchers.equalTo(2));

    // Verificamos el nombre de cada uno de ellos en su posición correspondiente
    assertThat(
      productosObtenidos.get(0).getNombre(),
      equalToIgnoringCase("Cuaderno Universitario")
    );
    assertThat(productosObtenidos.get(1).getNombre(), equalToIgnoringCase("Cuaderno Exito"));

    // Verificamos que se haya invocado al servicio una sola vez con el parámetro correcto
    verify(servicioProductoMock, times(1)).buscarProductosPorNombre(texto);
  }

  @Test
  public void siNoHayProductoConElNombreBuscadoDebeMostrarMensajeError() {
    String busqueda = "Cuaderno Universitario";
    String mensajeException =
      "No se encontró ninguna coincidencia para: " + busqueda + ". Intente otra búsqueda";
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
    when(servicioProductoMock.buscarProductosPorNombre(busqueda))
      .thenThrow(new ProductoNoEncontradoException(mensajeException));
    ModelAndView modelAndView = homeControlador.irAHome(sessionMock, null, busqueda);

    String mensajeError = (String) modelAndView.getModel().get("errorBusquedaProductos");
    assertThat(mensajeError, equalToIgnoringCase(mensajeException));
  }

  @Test
  public void elHomeDebeCargarLosIdsDeProductosQueYaEstanEnElCarrito() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);

    Producto productoEnCarritoMock = mock(Producto.class);
    when(productoEnCarritoMock.getId()).thenReturn(1L);

    ItemCarrito itemMock = mock(ItemCarrito.class);
    when(itemMock.getProducto()).thenReturn(productoEnCarritoMock);

    Carrito carritoMock = mock(Carrito.class);
    when(carritoMock.getItems()).thenReturn(List.of(itemMock));
    when(servicioCarritoMock.obtenerOCrearCarrito(any())).thenReturn(carritoMock);

    ModelAndView mv = homeControlador.irAHome(sessionMock, null, null);

    List<Long> idsEnCarrito = (List<Long>) mv.getModel().get("idsEnCarrito");

    assertThat(idsEnCarrito.size(), org.hamcrest.Matchers.equalTo(1));
    assertThat(idsEnCarrito.get(0), org.hamcrest.Matchers.equalTo(1L));
  }
}
