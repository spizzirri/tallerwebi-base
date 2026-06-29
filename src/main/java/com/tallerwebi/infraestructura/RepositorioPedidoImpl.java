package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Pedidos.EstadoPedido;
import com.tallerwebi.dominio.Pedidos.Pedido;
import com.tallerwebi.dominio.Pedidos.RepositorioPedido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPedidoImpl implements RepositorioPedido {

  private static final String USUARIO_ID = "usuarioId";
  private final SessionFactory sessionFactory;

  public RepositorioPedidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Pedido pedido) {
    sessionFactory.getCurrentSession().saveOrUpdate(pedido);
  }

  @Override
  public List<Pedido> obtenerPedidosPorUsuario(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct p " +
        "from Pedido p " +
        "left join fetch p.items " +
        "where p.usuario.id = :usuarioId " +
        "and p.estado = :estado",
        Pedido.class
      )
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("estado", EstadoPedido.PAGO_PENDIENTE)
      .getResultList();
  }

  @Override
  public void eliminarPedidosPendientes(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createQuery(
        "update Pedido p " +
        "set p.estado = :cancelado " +
        "where p.usuario.id = :usuarioId " +
        "and p.estado = :pendiente"
      )
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("cancelado", EstadoPedido.CANCELADO)
      .setParameter("pendiente", EstadoPedido.PAGO_PENDIENTE)
      .executeUpdate();
  }

  @Override
  public List<Pedido> obtenerTodosLosPedidosPorUsuario(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Pedido WHERE usuario.id = :id ORDER BY fecha DESC", Pedido.class)
      .setParameter("id", usuarioId)
      .getResultList();
  }

  @Override
  public void marcarPedidoPagado(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createQuery(
        "update Pedido p set p.estado = :pagado " +
        "where p.usuario.id = :usuarioId " +
        "and p.estado = :pendiente"
      )
      .setParameter("pagado", EstadoPedido.PAGADO)
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("pendiente", EstadoPedido.PAGO_PENDIENTE)
      .executeUpdate();
  }

  @Override
  public void eliminarPorUsuario(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createQuery("DELETE FROM Pedido p WHERE p.usuario.id = :usuarioId")
      .setParameter(USUARIO_ID, usuarioId)
      .executeUpdate();
  }
}
