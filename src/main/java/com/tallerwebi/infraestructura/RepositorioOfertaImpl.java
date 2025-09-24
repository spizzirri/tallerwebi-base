package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Oferta;
import com.tallerwebi.dominio.RepositorioOferta;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioOferta")
public class RepositorioOfertaImpl implements RepositorioOferta {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioOfertaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardarOferta(Oferta oferta) {
        sessionFactory.getCurrentSession().save(oferta);
    }
}
