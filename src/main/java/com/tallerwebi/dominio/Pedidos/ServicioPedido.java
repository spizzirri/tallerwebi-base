package com.tallerwebi.dominio.Pedidos;

import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ServicioPedido {
  void crearPedido(
    Long hijoId,
    List<ItemDistribucionDTO> items,
    LocalDate fechaRetiro,
    Usuario usuario
  );
  List<Pedido> obtenerPedidosPendientesDePago(Long usuarioId);
  void limpiarPedidosPendientes(Long usuarioId);
  List<Pedido> obtenerTodosLosPedidos(Long usuarioId);
  void marcarComoPagados(Long id);

  List<Pedido> obtenerPedidosDeLosUsuarios();

  List<Pedido> obtenerPedidosDeLosUsuariosFiltrado(String estadoPedido);

  List<Pedido> obtenerResultadosBusquedaPorNombre(String nombreAlumno);

  void actualizarEstadoPedido(Long idPedido, String estadoNuevo);

  Pedido obtenerResultadosBusquedaPedidoPorId(Long idPedido);

  void actualizarPedidoExistente(
    Long pedidoId,
    Map<Long, List<ItemDistribucionDTO>> listaPorHijo,
    LocalDate fechaRetiro,
    Usuario usuario
  );

  Pedido buscarPorId(Long id);

  List<Pedido> obtenerPedidosEnCarrito(Long usuarioId);

  void marcarPedidosEnCarritoComoPendientes(Long usuarioId);
}
