package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Categorias;
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
    public List<Categorias> listarCategorias(){
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Categorias", Categorias.class).list();
    }

    @Override
    public Categorias buscarCategoriaPorNombre (String nombreDeCategoriaEnUrl) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Categorias where nombreEnUrl = :nombre",Categorias.class)
                .setParameter("nombre", nombreDeCategoriaEnUrl)
                .uniqueResult();

    }
}
