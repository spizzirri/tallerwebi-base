package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.RepositorioCarrito;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioCarritoImpl implements RepositorioCarrito {

  private final SessionFactory sessionFactory;

  public RepositorioCarritoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Carrito buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Carrito.class, id);
  }

  @Override
  public void guardar(Carrito carrito) {
    sessionFactory.getCurrentSession().saveOrUpdate(carrito);
  }

  @Override
  public Carrito buscarPorUsuario(Long usuarioId) {
    String hql = "FROM Carrito c " + "WHERE c.usuario.id = :usuarioId";

    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Carrito.class)
      .setParameter("usuarioId", usuarioId)
      .uniqueResult();
  }

  @Override
  public void eliminarPorUsuario(Long usuarioId) {
    Carrito carrito = buscarPorUsuario(usuarioId);
    if (carrito != null) {
      sessionFactory.getCurrentSession().delete(carrito);
    }
  }
}
