package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.reto.RepositorioReto;
import com.tallerwebi.dominio.reto.Reto;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioReto")
public class RepositorioRetoImpl implements RepositorioReto {

    private SessionFactory sessionFactory;



    @Autowired
    public RepositorioRetoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardarReto(Reto reto) {
        Session session = sessionFactory.getCurrentSession();
        session.save(reto);
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
        Session session = sessionFactory.getCurrentSession();
        return session.get(Reto.class, retoId); // Utiliza session.get para obtener el reto por ID
    }

    @Override
    public void actualizarReto(Reto reto) {
        Session session = this.sessionFactory.getCurrentSession();
        session.update(reto);
        session.flush(); // Sincronizar los cambios con la base de datos
    }

    @Override
    public Reto obtenerRetoEnProceso() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto WHERE seleccionado = true AND enProceso = true";
        Query<Reto> query = session.createQuery(hql, Reto.class);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    @Override
    public List<Reto> obtenerTodosLosRetos() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM Reto";
        Query<Reto> query = session.createQuery(hql, Reto.class);
        return query.list();
    }


}
