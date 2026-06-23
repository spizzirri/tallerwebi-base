package com.tallerwebi.dominio.CarritoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ItemCarrito;
import com.tallerwebi.dominio.Carrito.RepositorioCarrito;
import com.tallerwebi.dominio.Carrito.ServicioCarritoImpl;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.ProductoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServicioCarritoTest {

  @Mock
  private RepositorioProducto repositorioProductoMock;

  @Mock
  private RepositorioCarrito repositorioCarritoMock;

  @InjectMocks
  private ServicioCarritoImpl servicioCarrito;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void dadoUnProductoExistenteCuandoLoAgregoAlCarritoEntoncesSeAgregaCorrectamente() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Galletitas");

    Usuario usuario = new Usuario();
    usuario.setId(2L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    //ejecucion
    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().size();

    //validacion
    assertEquals(1, resultado);
  }

  @Test
  public void dadoUnCarritoConDosProductosCalculaCorrectamenteElTotal() {
    //preparacion
    Producto producto1 = new Producto();
    producto1.setId(1L);
    producto1.setNombre("Galletitas");
    producto1.setPrecio(100.0);

    Producto producto2 = new Producto();
    producto2.setId(2L);
    producto2.setNombre("Gaseosa");
    producto2.setPrecio(200.0);

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto1.getId())).thenReturn(producto1);
    when(repositorioProductoMock.buscarProductoPorId(producto2.getId())).thenReturn(producto2);

    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto1.getId(), usuario.getId());
    servicioCarrito.agregarProducto(producto2.getId(), usuario.getId());

    //ejecucion
    Double total = servicioCarrito.calcularTotal(usuario.getId());

    //validacion
    assertEquals(300.0, total, 0.01);
  }

  @Test
  public void dadoUnProductoYaExistenteEnCarritoCuandoLoAgregoNoSeDuplica() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);

    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().size();

    //validacion
    assertEquals(1, resultado);
  }

  @Test
  public void dadoUnProductoEnCarritoCuandoLoEliminoEntoncesSeRemueveCorrectamente() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.eliminarProducto(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().size();

    //validacion
    assertEquals(0, resultado);
  }

  @Test
  public void dadoUnProductoEnCarritoCuandoAumentoCantidadEntoncesIncrementaEnUno() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.aumentarCantidad(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().get(0).getCantidad();

    //validacion
    assertEquals(2, resultado);
  }

  @Test
  public void dadoUnProductoEnCarritoCuandoRestoCantidadEntoncesRestoEnUno() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());
    servicioCarrito.aumentarCantidad(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.disminuirCantidad(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().get(0).getCantidad();

    //validacion
    assertEquals(1, resultado);
  }

  @Test
  public void dadoUnProductoInexistenteCuandoLoAgregoAlCarritoEntoncesLanzaExcepcion() {
    // preparacion
    Long productoId = 1L;
    Long usuarioId = 2L;

    when(repositorioProductoMock.buscarProductoPorId(productoId)).thenReturn(null);

    // ejecucion

    // validacion
    assertThrows(
      ProductoNoEncontradoException.class,
      () -> servicioCarrito.agregarProducto(productoId, usuarioId)
    );
  }

  @Test
  public void dadoUnProductoConCantidadUnoCuandoRestoCantidadEntoncesLaCantidadNoDisminuye() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.disminuirCantidad(producto.getId(), usuario.getId());

    int resultado = carrito.getItems().get(0).getCantidad();

    //validacion
    assertEquals(1, resultado);
  }

  @Test
  public void dadoUnProductoQueNoExisteEnElCarritoCuandoLoEliminoEntoncesElCarritoPermaneceIgual() {
    //preparacion
    Producto producto = new Producto();
    producto.setId(1L);
    producto.setNombre("Alfajor");

    Usuario usuario = new Usuario();
    usuario.setId(3L);

    Carrito carrito = new Carrito();

    carrito.setUsuario(usuario);

    carrito.agregarProducto(producto);

    when(repositorioProductoMock.buscarProductoPorId(producto.getId())).thenReturn(producto);
    when(repositorioCarritoMock.buscarPorUsuario(usuario.getId())).thenReturn(carrito);

    servicioCarrito.agregarProducto(producto.getId(), usuario.getId());

    //ejecucion
    servicioCarrito.eliminarProducto(999L, usuario.getId());

    int resultado = carrito.getItems().size();

    //validacion
    assertEquals(1, resultado);
  }

  @Test
  void noDeberiaAgregarProductoSinStock() {
    // given
    Producto producto = new Producto();
    producto.setCantidad(0);

    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(producto);

    // then
    assertThrows(ProductoSinStockException.class, () -> servicioCarrito.agregarProducto(1L, 1L));
  }

  @Test
  void noDeberiaAgregarProductoCuandoElStockEsNegativo() {
    Producto producto = new Producto();
    producto.setCantidad(-5);

    when(repositorioProductoMock.buscarProductoPorId(1L)).thenReturn(producto);

    assertThrows(ProductoSinStockException.class, () -> servicioCarrito.agregarProducto(1L, 1L));
  }
}
