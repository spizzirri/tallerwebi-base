package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.Viaje;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioViajeImpl implements RepositorioViaje {

    private  SessionFactory sessionFactory;

    @Autowired
    public RepositorioViajeImpl(SessionFactory sessionFactory){

        this.sessionFactory = sessionFactory;

    }
    @Override
    public void guardar(Viaje viaje) {
        sessionFactory.getCurrentSession().save(viaje);
    }

    @Override
    public Viaje buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Viaje.class,id);
    }

    @Override
    public List<Viaje> listarViajes() {
        return sessionFactory.getCurrentSession().createQuery("FROM Viaje", Viaje.class).list();
    }

    @Override
    public List<Viaje> buscarPorOrigen(String origen) {
        return sessionFactory.getCurrentSession().createQuery("FROM Viaje V WHERE V.origen = :origen_viaje", Viaje.class).setParameter("origen_viaje",origen).list();
    }
}
