package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import java.util.List;
import javax.persistence.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioHijo")
public class RepositorioHijoImpl implements RepositorioHijo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioHijoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Hijo> listarHijos(Long idPadre) {
    String query = "FROM Hijo h WHERE h.padre.id=:idPadre";
    return this.sessionFactory.getCurrentSession()
      .createQuery(query, Hijo.class)
      .setParameter("idPadre", idPadre)
      .getResultList();
  }

  @Override
  public Hijo buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Hijo.class, id);
  }

  @Override
  public void guardar(Hijo hijo) {
    sessionFactory.getCurrentSession().save(hijo);
  }

  @Override
  public Boolean existeHijoPorDni(long dni) {
    String hql = "FROM Hijo h WHERE h.dni=:dni";
    Query query = sessionFactory.getCurrentSession().createQuery(hql);
    query.setParameter("dni", dni);

    //         el return seria esto: al tener el ! se lee lo contrario
    //         List resultados = query.getResultList();
    //        if (resultados.isEmpty()) {
    //            return false; // no existe
    //        } else {
    //            return true;  // existe
    //        }
    return !query.getResultList().isEmpty();
  }

  @Override
  public boolean existeAlias(String alias) {
    return (
      this.sessionFactory.getCurrentSession()
        .createQuery("SELECT COUNT(h) FROM Hijo h WHERE h.aliasRetiro = :alias", Long.class)
        .setParameter("alias", alias)
        .getSingleResult() >
      0
    );
  }

  @Override
  public void modificar(Hijo hijo) {
    sessionFactory.getCurrentSession().update(hijo);
  }

    @Override
    public void eliminar(Hijo hijo) {
        sessionFactory.getCurrentSession().delete(hijo);
    }
}
