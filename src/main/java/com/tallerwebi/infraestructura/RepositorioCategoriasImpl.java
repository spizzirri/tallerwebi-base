package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Categoria;
import com.tallerwebi.dominio.RepositorioCategorias;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioCategoriasImpl implements RepositorioCategorias {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioCategoriasImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Categoria> listarCategorias(){
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Categoria", Categoria.class).list();
    }

    @Override
    public Categoria buscarCategoriaPorNombreDeRuta(String nombreDeCategoriaEnUrl) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Categoria where nombreEnUrl = :nombre", Categoria.class)
                .setParameter("nombre", nombreDeCategoriaEnUrl)
                .uniqueResult();

    }
}
