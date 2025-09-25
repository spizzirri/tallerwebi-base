package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioResenia;
import com.tallerwebi.dominio.Resenia;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioResenia")
public class RepositorioReseniaImpl implements RepositorioResenia {
    private SessionFactory  sessionFactory;

    @Autowired
    public RepositorioReseniaImpl(SessionFactory sessionFactory) {this.sessionFactory = sessionFactory;}

    public void guardar(Resenia resenia) {
        sessionFactory.getCurrentSession().save(resenia);
    }

    public Resenia buscarPorId(Long id) {

        return (Resenia) sessionFactory.getCurrentSession().createCriteria(Resenia.class)
                .add(Restrictions.eq("id", id))
                .uniqueResult();
    }

    public List<Resenia> buscarPorHamburguesa(Long hamburguesaId) {
        return  (List<Resenia>) sessionFactory.getCurrentSession().createCriteria(Resenia.class);
    }

    public void modificar(Resenia resenia) {
        sessionFactory.getCurrentSession().update(resenia);
    }

    public void eliminar(Resenia resenia) {
        sessionFactory.getCurrentSession().delete(resenia);
    }
}
