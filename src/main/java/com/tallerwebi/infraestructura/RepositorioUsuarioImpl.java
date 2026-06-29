package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.List;
import javax.persistence.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Usuario usuario) {
    sessionFactory.getCurrentSession().save(usuario);
  }

  @Override
  public Boolean existeUsuarioPorMail(String email) {
    //    return (Usuario) sessionFactory
    //      .getCurrentSession()
    //      .createCriteria(Usuario.class)
    //      .add(Restrictions.eq("email", email))
    //      .uniqueResult(); DE LOS PROFES
    String hql = "FROM Usuario WHERE email=:email";
    Query query = sessionFactory.getCurrentSession().createQuery(hql);
    query.setParameter("email", email);
    return !query.getResultList().isEmpty();
  }

  @Override
  public Boolean existeUsuarioPorDni(Long dni) {
    String hql = "FROM Usuario u WHERE u.datosPersonales.dni=:dni";
    Query query = sessionFactory.getCurrentSession().createQuery(hql);
    query.setParameter("dni", dni);
    return !query.getResultList().isEmpty();
  }

  @Override
  public void modificar(Usuario usuario) {
    sessionFactory.getCurrentSession().update(usuario);
  }

  @Override
  public Usuario buscarUsuarioPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Usuario.class, id);
  }

  @Override
  public Usuario buscarUsuarioPorEmail(String email) {
    String hql = "FROM Usuario WHERE email=:email";
    Query query = sessionFactory.getCurrentSession().createQuery(hql);
    query.setParameter("email", email);
    List<Usuario> resultado = query.getResultList();
    return resultado.isEmpty() ? null : resultado.get(0);
  }

  @Override
  public void eliminar(Usuario usuario) {
    sessionFactory.getCurrentSession().delete(usuario);
  }
}
