package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioSubcategorias;
import com.tallerwebi.dominio.Subcategorias;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioSubcategoriasImpl implements RepositorioSubcategorias {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioSubcategoriasImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Subcategorias> listarSubcategorias(){
        final var session = sessionFactory.getCurrentSession();
        return session.createQuery("from Subcategorias", Subcategorias.class).list();
    }

    @Override
    public Subcategorias buscarSubcategoriaPorNombreDeRuta(String nombreDeSubcategoriaEnUrl) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Subcategorias where nombreEnUrl = :nombre",Subcategorias.class)
                .setParameter("nombre", nombreDeSubcategoriaEnUrl)
                .uniqueResult();
        }
}



