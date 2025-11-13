package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Orden;
import com.tallerwebi.dominio.RepositorioOrden;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioOrden")
public class RepositorioOrdenImpl implements RepositorioOrden {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioOrdenImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Orden orden) {
        sessionFactory.getCurrentSession().save(orden);
    }

    @Override
    public Orden buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Orden.class, id);
    }

    @Override
    public Boolean crear(Orden orden) {
       return  (Long) sessionFactory.getCurrentSession().save(orden) > 0;
    }
}
