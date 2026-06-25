package com.tallerwebi.dominio.Carrito;

import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.ProductoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service("servicioCarrito")
@Transactional
public class ServicioCarritoImpl implements ServicioCarrito {

  private final RepositorioProducto repositorioProducto;
  private final RepositorioCarrito repositorioCarrito;
  private final RepositorioUsuario repositorioUsuario;

  public ServicioCarritoImpl(
    RepositorioProducto repositorioProducto,
    RepositorioCarrito repositorioCarrito,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioProducto = repositorioProducto;
    this.repositorioCarrito = repositorioCarrito;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Carrito obtenerOCrearCarrito(Long usuarioId) {
    Carrito carrito = repositorioCarrito.buscarPorUsuario(usuarioId);

    if (carrito == null) {
      Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);

      carrito = new Carrito();

      carrito.setUsuario(usuario);

      repositorioCarrito.guardar(carrito);
    }

    return carrito;
  }

  @Override
  public void agregarProducto(long productoId, long usuarioId) {
    Producto producto = repositorioProducto.buscarProductoPorId(productoId);

    if (producto == null) {
      throw new ProductoNoEncontradoException("El producto no fue encontrado");
    }

    if (producto.getCantidad() <= 0) {
      throw new ProductoSinStockException("No hay stock disponible");
    }

    Carrito carrito = obtenerOCrearCarrito(usuarioId);

    carrito.agregarProducto(producto);
    repositorioCarrito.guardar(carrito);
  }

  @Override
  public Double calcularTotal(long usuarioId) {
    Carrito carrito = obtenerOCrearCarrito(usuarioId);
    return carrito.calcularTotal();
  }

  @Override
  public void eliminarProducto(long productoId, long usuarioId) {
    Carrito carrito = obtenerOCrearCarrito(usuarioId);
    carrito.eliminarProducto(productoId);
    repositorioCarrito.guardar(carrito);
  }

  @Override
  public void aumentarCantidad(long productoId, long usuarioId) {
    Carrito carrito = obtenerOCrearCarrito(usuarioId);
    carrito.aumentarCantidad(productoId);
    repositorioCarrito.guardar(carrito);
  }

  @Override
  public void disminuirCantidad(long productoId, long usuarioId) {
    Carrito carrito = obtenerOCrearCarrito(usuarioId);
    carrito.disminuirCantidad(productoId);
    repositorioCarrito.guardar(carrito);
  }

  @Override
  public void vaciarCarrito(long usuarioId) {
    Carrito carrito = obtenerOCrearCarrito(usuarioId);

    carrito.getItems().clear();

    repositorioCarrito.guardar(carrito);
  }
}
