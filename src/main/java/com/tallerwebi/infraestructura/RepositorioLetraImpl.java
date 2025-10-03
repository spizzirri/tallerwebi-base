package com.tallerwebi.infraestructura;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Letra;
import com.tallerwebi.dominio.RepositorioLetra;

@Repository
public class RepositorioLetraImpl implements RepositorioLetra {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioLetraImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Letra letra) {
        this.sessionFactory.getCurrentSession().save(letra);
    }

}
