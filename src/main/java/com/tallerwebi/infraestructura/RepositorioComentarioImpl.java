package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.RepositorioComentario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioComentario")
public class RepositorioComentarioImpl implements RepositorioComentario {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioComentarioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Comentario comentario) {
    this.sessionFactory.getCurrentSession().persist(comentario);
  }

  @Override
  public List<Comentario> listarTodos() {
    return this.sessionFactory.getCurrentSession()
      .createQuery("from Comentario order by id desc", Comentario.class)
      .getResultList();
  }
}
