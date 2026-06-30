package com.tallerwebi.dominio.Pedidos;

import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.presentacion.DistribucionCarrito.ItemDistribucionDTO;
import java.util.List;

public interface ServicioPedido {
  void crearPedido(Long hijoId, List<ItemDistribucionDTO> items, Usuario usuario);
  List<Pedido> obtenerPedidosPendientesDePago(Long usuarioId);
  void limpiarPedidosPendientes(Long usuarioId);
  List<Pedido> obtenerTodosLosPedidos(Long usuarioId);
  void marcarComoPagados(Long id);

  List<Pedido> obtenerPedidosDeLosUsuarios();

  List<Pedido> obtenerPedidosDeLosUsuariosFiltrado(String estadoPedido);

  List<Pedido> obtenerResultadosBusquedaPorNombre(String nombreAlumno);
}
