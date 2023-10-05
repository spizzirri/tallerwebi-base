package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioAutoImpl implements RepositorioAuto {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void guardarAutos(Auto[] autos){
        Session session = sessionFactory.getCurrentSession();
        for(Auto auto: autos){
            session.save(auto);
        }
    }

    @Override
    public Auto buscarAutoPorPatente(String patente) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Auto a WHERE a.patente = :patente");
        query.setParameter("patente", patente);
        Auto autos = (Auto) query.uniqueResult();
        return autos;
    }

    @Override
    public List<Auto> buscarAutosPorMarca(Marca marca) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Auto a WHERE a.marca = :marca");
        query.setParameter("marca", marca);
        List<Auto> autos = (List<Auto>) query.list();
        return autos;
    }

    @Override
    public List<Auto> buscarAutosDeUnUsuario(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("FROM Auto a INNER JOIN Usuario u ON u.id = a.usuario.id WHERE u.id = :id");
        query.setParameter("id", usuario.getId());
        List<Auto> autos = (List<Auto>) query.list();
        return autos;
    }
}
