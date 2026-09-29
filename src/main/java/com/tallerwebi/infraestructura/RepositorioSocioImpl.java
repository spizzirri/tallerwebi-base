package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioSocio;
import com.tallerwebi.dominio.Socio;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioSocio")
public class RepositorioSocioImpl implements RepositorioSocio {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioSocioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Socio socio) {
    sessionFactory.getCurrentSession().persist(socio);
  }

  @Override
  public Socio buscarPorDni(String dni) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Socio where dni = :dni", Socio.class)
      .setParameter("dni", dni)
      .uniqueResult();
  }

  @Override
  public Socio buscarPorUsuario(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("select distinct s from Socio s where s.usuario.id = :usuarioId", Socio.class)
      .setParameter("usuarioId", usuarioId)
      .uniqueResult();
  }
}
