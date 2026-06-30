package com.tallerwebi.dominio.Pedidos;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioPedido")
@Transactional
public class ServicioPedidoImpl implements ServicioPedido {

  private final RepositorioPedido repositorioPedido;
  private final RepositorioHijo repositorioHijo;
  private final RepositorioProducto repositorioProducto;

  public ServicioPedidoImpl(
    RepositorioPedido repositorioPedido,
    RepositorioHijo repositorioHijo,
    RepositorioProducto repositorioProducto
  ) {
    this.repositorioPedido = repositorioPedido;
    this.repositorioHijo = repositorioHijo;
    this.repositorioProducto = repositorioProducto;
  }

  @Override
  public void crearPedido(
    Long hijoId,
    List<ItemDistribucionDTO> items,
    LocalDate fechaRetiro,
    Usuario usuario
  ) {
    Hijo hijo = repositorioHijo.buscarPorId(hijoId);

    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setEstado(EstadoPedido.PAGO_PENDIENTE);
    pedido.setFechaRetiro(fechaRetiro);

    validarFechaRetiro(fechaRetiro);

    for (ItemDistribucionDTO item : items) {
      Producto producto = repositorioProducto.buscarProductoPorId(item.getProductoId());

      ItemPedido itemPedido = new ItemPedido(producto, item.getCantidad());
      itemPedido.setPedido(pedido);

      pedido.agregarItem(itemPedido);
    }
    pedido.calcularSubtotal();

    repositorioPedido.guardar(pedido);
  }

  @Override
  public List<Pedido> obtenerPedidosPendientesDePago(Long usuarioId) {
    return repositorioPedido.obtenerPedidosPorUsuario(usuarioId);
  }

  @Override
  public void limpiarPedidosPendientes(Long usuarioId) {
    repositorioPedido.eliminarPedidosPendientes(usuarioId);
  }

  @Override
  public List<Pedido> obtenerTodosLosPedidos(Long usuarioId) {
    return repositorioPedido.obtenerTodosLosPedidosPorUsuario(usuarioId);
  }

  @Override
  public void marcarComoPagados(Long usuarioId) {
    repositorioPedido.marcarPedidoPagado(usuarioId);
  }

  //---- MÉTODOS AUXILIARES PRIVADOS ----

  private void validarFechaRetiro(LocalDate fechaRetiro) {
    if (fechaRetiro == null) {
      throw new FechaRetiroInvalidaException("Debe seleccionar una fecha de retiro.");
    }

    if (fechaRetiro.isBefore(LocalDate.now().plusDays(1))) {
      throw new FechaRetiroInvalidaException("La fecha de retiro debe ser a partir de mañana.");
    }
  }

  @Override
  public List<Pedido> obtenerPedidosDeLosUsuarios() {
    return repositorioPedido.obtenerTodosLosPedidosDeTodosLosClientes();
  }

  @Override
  public List<Pedido> obtenerPedidosDeLosUsuariosFiltrado(String estadoPedido) {
    return repositorioPedido.obtenerTodosLosPedidosDeTodosLosClientesFiltrado(estadoPedido);
  }

  @Override
  public List<Pedido> obtenerResultadosBusquedaPorNombre(String nombreAlumno) {
    return repositorioPedido.buscarPedidosPorNombreDelAlumno(nombreAlumno);
  }
}
