package com.tallerwebi.dominio.Pedidos;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.RepositorioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.FechaRetiroInvalidaException;
import com.tallerwebi.dominio.excepcion.PedidoNoEncontradoException;
import com.tallerwebi.dominio.excepcion.ProductoSinStockException;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
  ) throws ProductoSinStockException {
    Hijo hijo = repositorioHijo.buscarPorId(hijoId);

    Pedido pedido = new Pedido();
    pedido.setUsuario(usuario);
    pedido.setHijo(hijo);
    pedido.setEstado(EstadoPedido.EN_CARRITO);
    pedido.setFechaRetiro(fechaRetiro);

    validarFechaRetiro(fechaRetiro);

    for (ItemDistribucionDTO item : items) {
      Producto producto = repositorioProducto.buscarProductoPorId(item.getProductoId());

      // VALIDACIÓN: Verificar si hay suficiente stock antes de restar
      if (producto.getCantidad() < item.getCantidad()) {
        throw new ProductoSinStockException(
          "Lo sentimos, no hay suficiente stock de: " + producto.getNombre()
        );
      }
      // Restamos el stock del producto
      int nuevoStock = producto.getCantidad() - item.getCantidad();
      producto.setCantidad(nuevoStock);
      // Al estar bajo @Transactional, Hibernate guardará el cambio de stock automáticamente.
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
    // 1. Buscamos los pedidos que están en PAGO_PENDIENTE antes de borrarlos/cancelarlos
    List<Pedido> pedidosPendientes = repositorioPedido.obtenerPedidosPorUsuario(usuarioId);

    if (pedidosPendientes != null) {
      for (Pedido pedido : pedidosPendientes) {
        // 2. Por cada ítem del pedido, le devolvemos las cantidades al stock del producto
        for (ItemPedido item : pedido.getItems()) {
          Producto producto = item.getProducto();
          int stockRestaurado = producto.getCantidad() + item.getCantidad();
          producto.setCantidad(stockRestaurado);
          // Hibernate actualizará el stock automáticamente al terminar la transacción.
        }
        // 3. Cambiamos el estado a CANCELADO de forma controlada
        pedido.setEstado(EstadoPedido.CANCELADO);
      }
    }
  }

  @Override
  public List<Pedido> obtenerTodosLosPedidos(Long usuarioId) {
    return repositorioPedido.obtenerTodosLosPedidosPorUsuario(usuarioId);
  }

  @Override
  public void marcarComoPagados(Long usuarioId) {
    repositorioPedido.marcarPedidoPagado(usuarioId);
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

  @Override
  public void actualizarEstadoPedido(Long idPedido, String estadoNuevo) {
    // 1. Buscamos el pedido para asegurarnos de que exista
    Pedido pedido = repositorioPedido.buscarPedidoPorId(idPedido);
    if (pedido == null) {
      throw new PedidoNoEncontradoException("El pedido con ID " + idPedido + " no existe.");
    }
    EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoNuevo);
    if (nuevoEstado == EstadoPedido.EN_CARRITO) {
      throw new IllegalArgumentException(
        "No es un estado válido para que el quiosquero lo asigne."
      );
    }
    repositorioPedido.cambiarEstadoPedido(idPedido, estadoNuevo);
  }

  @Override
  public Pedido obtenerResultadosBusquedaPedidoPorId(Long idPedido) {
    return repositorioPedido.buscarPedidoPorId(idPedido);
  }

  @Override
  public void actualizarPedidoExistente(
    Long pedidoId,
    Map<Long, List<ItemDistribucionDTO>> listaPorHijo,
    LocalDate fechaRetiro,
    Usuario usuario
  ) throws ProductoSinStockException {
    // 1. Buscamos el pedido original que se quiere editar
    Pedido pedido = buscarPorId(pedidoId);

    // 2. Validamos la nueva fecha de retiro
    validarFechaRetiro(fechaRetiro);
    pedido.setFechaRetiro(fechaRetiro);

    // 3. RETORNO DE STOCK: Antes de hacer cualquier cambio, le devolvemos al stock físico lo que este pedido ya tenía reservado
    for (ItemPedido itemViejo : pedido.getItems()) {
      Producto prod = itemViejo.getProducto();
      prod.setCantidad(prod.getCantidad() + itemViejo.getCantidad());
    }

    // 4. Limpiamos la lista actual de ítems del pedido para meter los nuevos
    pedido.getItems().clear();

    // 5. Buscamos los nuevos ítems correspondientes al hijo de este pedido
    // Recordá que 'listaPorHijo' viene agrupada por HijoID desde el controlador
    Long hijoId = pedido.getHijo().getId();
    List<ItemDistribucionDTO> nuevosItems = listaPorHijo.get(hijoId);

    if (nuevosItems == null || nuevosItems.isEmpty()) {
      // Si en la modificación se le quitaron todos los productos a este hijo, lo pasamos a cancelado
      pedido.setEstado(EstadoPedido.CANCELADO);
      pedido.setSubtotal(0.0);
      return;
    }

    // 6. Procesamos y re-descontamos el stock con los nuevos valores ingresados
    for (ItemDistribucionDTO nuevoItem : nuevosItems) {
      Producto producto = repositorioProducto.buscarProductoPorId(nuevoItem.getProductoId());

      // Volvemos a validar stock con el valor actualizado del paso 3
      if (producto.getCantidad() < nuevoItem.getCantidad()) {
        throw new ProductoSinStockException(
          "Lo sentimos, no hay suficiente stock de: " + producto.getNombre()
        );
      }

      // Descontamos las nuevas unidades confirmadas
      producto.setCantidad(producto.getCantidad() - nuevoItem.getCantidad());

      // Vinculamos el nuevo ítem al pedido
      ItemPedido nuevoItemPedido = new ItemPedido(producto, nuevoItem.getCantidad());
      nuevoItemPedido.setPedido(pedido);
      pedido.agregarItem(nuevoItemPedido);
    }

    // 7. Recalculamos el total del pedido modificado y nos aseguramos de mantenerlo en PAGO_PENDIENTE
    pedido.calcularSubtotal();
    pedido.setEstado(EstadoPedido.EN_CARRITO);

    // Hibernate se encarga de sincronizar todos estos cambios automáticamente al cerrar la transacción
    repositorioPedido.guardar(pedido);
  }

  @Override
  public Pedido buscarPorId(Long id) {
    Pedido pedido = repositorioPedido.buscarPedidoPorId(id);
    if (pedido == null) {
      throw new PedidoNoEncontradoException("El pedido con ID " + id + " no existe.");
    }
    return pedido;
  }

  @Override
  public List<Pedido> obtenerPedidosEnCarrito(Long usuarioId) {
    return repositorioPedido.obtenerPedidosEnCarrito(usuarioId);
  }

  @Override
  public void marcarPedidosEnCarritoComoPendientes(Long usuarioId) {
    repositorioPedido.marcarEnCarritoComoPendiente(usuarioId);
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
}
