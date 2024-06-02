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
    public Reto obtenerRetoDisponible() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE seleccionado = false ORDER BY rand()";
        Query <Reto> query = session.createQuery(hql, Reto.class).setMaxResults(1);

        return query.uniqueResult();
    }

    @Override
    public Reto obtenerRetoPorId(Long retoId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE id = :id AND seleccionado = true";
        Query<Reto> query = session.createQuery(hql, Reto.class);
        query.setParameter("id", retoId);
        return query.uniqueResult();
    }

    @Override
    public void empezarRetoActualizar(Reto reto) {
        Session session = this.sessionFactory.getCurrentSession();
        session.update(reto);
        session.flush(); // Sincronizar los cambios con la base de datos
        session.getTransaction().commit();
}


    @Override
    public Reto obtenerRetoEnProceso() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE seleccionado = true AND enProceso = true";
        Query<Reto> query = session.createQuery(hql, Reto.class);
        return query.uniqueResult();
    }


}
