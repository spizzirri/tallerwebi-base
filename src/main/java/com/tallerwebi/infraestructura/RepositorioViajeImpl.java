package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.Viaje;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
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
    public List<Viaje> buscarPorFecha(String fechaHora) {
        return sessionFactory.getCurrentSession().createCriteria(Viaje.class)
            .add(Restrictions.eq("fecha_hora",fechaHora))
            .list();
    }

    @Override
    public void eliminar(Viaje viaje) {sessionFactory.getCurrentSession().delete(viaje);}
}
