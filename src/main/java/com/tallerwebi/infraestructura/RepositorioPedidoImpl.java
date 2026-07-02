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
  private static final String JOIN_ITEMS = "LEFT JOIN FETCH p.items i ";
  private static final String JOIN_PRODUCTO = "LEFT JOIN FETCH i.producto ";
  private static final String WHERE_USUARIO = "WHERE p.usuario.id = :usuarioId ";
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
        WHERE_USUARIO +
        "and p.estado = :estado",
        Pedido.class
      )
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("estado", EstadoPedido.PAGO_PENDIENTE)
      .getResultList();
  }

  @Override
  public List<Pedido> obtenerPedidosEnCarrito(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct p " +
        "from Pedido p " +
        "left join fetch p.items " +
        WHERE_USUARIO +
        "and p.estado = :estado",
        Pedido.class
      )
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("estado", EstadoPedido.EN_CARRITO)
      .getResultList();
  }

  @Override
  public void eliminarPedidosPendientes(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createQuery(
        "update Pedido p " +
        "set p.estado = :cancelado " +
        WHERE_USUARIO +
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
        "update Pedido p set p.estado = :pagado " + WHERE_USUARIO + "and p.estado = :pendiente"
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
      .createQuery("DELETE FROM Pedido p " + WHERE_USUARIO)
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
        JOIN_ITEMS + // Trae los items del pedido
        JOIN_PRODUCTO + // Trae los datos de cada producto en el item
        "WHERE p.estado != :enCarrito " + // 👈 nuevo
        "ORDER BY p.fecha DESC", // Siempre viene bien ver los más nuevos primero
        Pedido.class
      )
      .setParameter("enCarrito", EstadoPedido.EN_CARRITO) // 👈 nuevo
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
        JOIN_ITEMS +
        JOIN_PRODUCTO +
        "WHERE p.estado = :estado " + // Filtramos solo por el estado
        "ORDER BY p.fecha DESC",
        Pedido.class
      )
      .setParameter("estado", estadoEnum)
      .getResultList();
  }

  @Override
  public List<Pedido> buscarPedidosPorNombreDelAlumno(String nombreApellidoAlumno) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p FROM Pedido p " +
        "LEFT JOIN FETCH p.hijo h " + // Le damos el alias 'h' a la relación hijo (alumno)
        JOIN_ITEMS +
        JOIN_PRODUCTO +
        "WHERE LOWER(CONCAT(h.nombre,'',h.apellido)) LIKE LOWER(:busqueda) " + // Búsqueda por coincidencia parcial e insensible a mayúsculas
        "AND p.estado != :enCarrito " + // 👈 nuevo
        "ORDER BY p.fecha DESC",
        Pedido.class
      )
      .setParameter("busqueda", "%" + nombreApellidoAlumno.trim() + "%")
      .setParameter("enCarrito", EstadoPedido.EN_CARRITO) // 👈 nuevo
      .getResultList();
  }

  @Override
  public void cambiarEstadoPedido(Long idPedido, String estadoNuevo) {
    Pedido pedido = sessionFactory.getCurrentSession().get(Pedido.class, idPedido);
    if (pedido != null) {
      pedido.setEstado(EstadoPedido.valueOf(estadoNuevo)); // Se convierte y se usa directo acá
      sessionFactory.getCurrentSession().update(pedido);
    }
  }

  @Override
  public void marcarEnCarritoComoPendiente(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createQuery(
        "update Pedido p set p.estado = :pendiente " + WHERE_USUARIO + "and p.estado = :enCarrito"
      )
      .setParameter("pendiente", EstadoPedido.PAGO_PENDIENTE)
      .setParameter(USUARIO_ID, usuarioId)
      .setParameter("enCarrito", EstadoPedido.EN_CARRITO)
      .executeUpdate();
  }

  @Override
  public Pedido buscarPedidoPorId(Long idPedido) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT p FROM Pedido p " + JOIN_ITEMS + JOIN_PRODUCTO + "WHERE p.id= :idPedido",
        Pedido.class
      )
      .setParameter("idPedido", idPedido)
      .uniqueResult();
  }
}
