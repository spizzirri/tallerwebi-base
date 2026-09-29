package com.tallerwebi.infraestructura;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/** Consultas comunes (listar y buscar por id) para las entidades de catálogo. */
public class RepositorioBase<T> {

  private final SessionFactory sessionFactory;
  private final Class<T> tipo;

  protected RepositorioBase(SessionFactory sessionFactory, Class<T> tipo) {
    this.sessionFactory = sessionFactory;
    this.tipo = tipo;
  }

  protected Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  public T buscarPorId(Long id) {
    return sesion().get(tipo, id);
  }

  protected List<T> listarOrdenadoPor(String campo) {
    String hql = "select distinct e from " + tipo.getSimpleName() + " e order by e." + campo;
    return sesion().createQuery(hql, tipo).getResultList();
  }
}
