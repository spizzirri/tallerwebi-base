package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Usuario buscarUsuario(String email, String password) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email and password = :password", Usuario.class)
      .setParameter("email", email)
      .setParameter("password", password)
      .uniqueResult();
  }

  @Override
  public void guardar(Usuario usuario) {
    sessionFactory.getCurrentSession().persist(usuario);
  }

  @Override
  public Usuario buscar(String email) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email", Usuario.class)
      .setParameter("email", email)
      .uniqueResult();
  }

  @Override
  public void modificar(Usuario usuario) {
    Usuario existente = sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where id = :id", Usuario.class)
      .setParameter("id", usuario.getId())
      .uniqueResult();
    if (existente == null) {
      throw new UsuarioNoEncontrado();
    }
    sessionFactory.getCurrentSession().merge(usuario);
  }
}
