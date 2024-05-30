package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioReto")
public class RepositorioRetoImpl implements RepositorioReto {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioRetoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Reto obtenerReto() {
        Session session = this.sessionFactory.getCurrentSession();
        Reto reto = null;
        boolean retoEncontrado = false;
        while (!retoEncontrado) {
            // Obtener un reto aleatorio que no haya sido seleccionado
            String hql = "FROM Reto WHERE seleccionado = false ORDER BY rand()";
            Query<Reto> query = session.createQuery(hql, Reto.class).setMaxResults(1);
            reto = query.uniqueResult();
            if (reto != null) {
                retoEncontrado = true;
            }
        }
        return reto;
    }

    @Override
    public void empezarReto(Long retoId) {
        Session session = this.sessionFactory.getCurrentSession();
        Reto reto = session.get(Reto.class, retoId);
        if (reto != null) {
            reto.setSeleccionado(true); // Marcar como seleccionado
            session.update(reto);
        }
    }

    @Override
    public Reto obtenerRetoPorId(Long retoId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE id = :id AND seleccionado = true";
        Query<Reto> query = session.createQuery(hql, Reto.class);
        query.setParameter("id", retoId);
        return query.uniqueResult();
    }


}
