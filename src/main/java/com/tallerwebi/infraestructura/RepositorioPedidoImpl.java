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

  @Override
  public List<Pedido> obtenerTodosLosPedidosDeTodosLosClientes() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p FROM Pedido p " +
        "LEFT JOIN FETCH p.hijo " + // Trae los datos del alumno de una
        "LEFT JOIN FETCH p.items i " + // Trae los items del pedido
        "LEFT JOIN FETCH i.producto " + // Trae los datos de cada producto en el item
        "ORDER BY p.fecha DESC", // Siempre viene bien ver los más nuevos primero
        Pedido.class
      )
      .getResultList();
  }

  @Override
  public List<Pedido> obtenerTodosLosPedidosDeTodosLosClientesFiltrado(String estadoPedido) {
    // Primero convertimos el String que viene de la URL al Enum correspondiente
    EstadoPedido estadoEnum = EstadoPedido.valueOf(estadoPedido);

    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p FROM Pedido p " +
        "LEFT JOIN FETCH p.hijo " +
        "LEFT JOIN FETCH p.items i " +
        "LEFT JOIN FETCH i.producto " +
        "WHERE p.estado = :estado " + // Filtramos solo por el estado
        "ORDER BY p.fecha DESC",
        Pedido.class
      )
      .setParameter("estado", estadoEnum)
      .getResultList();
  }

  @Override
  public List<Pedido> buscarPedidosPorNombreDelAlumno(String nombreAlumno) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p FROM Pedido p " +
        "LEFT JOIN FETCH p.hijo h " + // Le damos el alias 'h' a la relación hijo (alumno)
        "LEFT JOIN FETCH p.items i " +
        "LEFT JOIN FETCH i.producto " +
        "WHERE LOWER(h.nombre) LIKE LOWER(:nombre) " + // Búsqueda por coincidencia parcial e insensible a mayúsculas
        "ORDER BY p.fecha DESC",
        Pedido.class
      )
      .setParameter("nombre", "%" + nombreAlumno + "%")
      .getResultList();
  }
}
