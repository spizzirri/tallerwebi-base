package com.tallerwebi.dominio.Pedidos;

import java.util.List;

public interface RepositorioPedido {
  void guardar(Pedido pedido);
  List<Pedido> obtenerPedidosPorUsuario(Long usuarioId);
  List<Pedido> obtenerPedidosEnCarrito(Long usuarioId); // 👈 nueva línea

  void eliminarPedidosPendientes(Long usuarioId);
  List<Pedido> obtenerTodosLosPedidosPorUsuario(Long usuarioId);
  void marcarPedidoPagado(Long usuarioId);

  void eliminarPorUsuario(Long id);

  List<Pedido> obtenerTodosLosPedidosDeTodosLosClientes();

  List<Pedido> obtenerTodosLosPedidosDeTodosLosClientesFiltrado(String estadoPedido);

  List<Pedido> buscarPedidosPorNombreDelAlumno(String nombreAlumno);

  void cambiarEstadoPedido(Long idPedido, String estadoNuevo);
  void marcarEnCarritoComoPendiente(Long usuarioId);

  Pedido buscarPedidoPorId(Long idPedido);
}
