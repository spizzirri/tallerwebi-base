package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.RepositorioSubasta;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioSubasta")
public class RepositorioSubastaImpl implements RepositorioSubasta {
    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioSubastaImpl(SessionFactory sessionFactory) {this.sessionFactory = sessionFactory;}

    public void guardar(Subasta subasta) {
        sessionFactory.getCurrentSession().save(subasta);
    }
}
