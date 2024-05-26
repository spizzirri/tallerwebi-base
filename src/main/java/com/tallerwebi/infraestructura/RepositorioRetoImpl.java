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
    public Reto obtenerYMarcarReto() {
        Session session = this.sessionFactory.getCurrentSession();
        // Obtener un reto aleatorio que no haya sido seleccionado
        String hql = "FROM Reto WHERE seleccionado = false ORDER BY rand()";
        Query<Reto> query = session.createQuery(hql, Reto.class).setMaxResults(1);
        Reto reto = query.uniqueResult();

        if (reto != null) {
            // Marcar el reto como seleccionado
            reto.setSeleccionado(true);
            session.update(reto);
        }

        return reto;
    }



}
