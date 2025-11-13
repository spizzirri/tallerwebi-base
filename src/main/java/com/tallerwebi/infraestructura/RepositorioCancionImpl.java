package com.tallerwebi.infraestructura;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Cancion;
import com.tallerwebi.dominio.RepositorioCancion;

@Repository
public class RepositorioCancionImpl implements RepositorioCancion {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioCancionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Cancion cancion) {
        this.sessionFactory.getCurrentSession().save(cancion);
    }

}