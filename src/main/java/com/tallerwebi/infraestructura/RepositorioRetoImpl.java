package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.reto.RepositorioReto;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioRetoImpl implements RepositorioReto {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioRetoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
