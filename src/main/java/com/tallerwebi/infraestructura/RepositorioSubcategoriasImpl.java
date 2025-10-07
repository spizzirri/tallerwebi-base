package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioSubcategorias;
import com.tallerwebi.dominio.Subcategoria;
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
    public List<Subcategoria> listarSubcategorias(){
        final var session = sessionFactory.getCurrentSession();
        return session.createQuery("from Subcategoria", Subcategoria.class).list();
    }

    @Override
    public Subcategoria buscarSubcategoriaPorNombreDeRuta(String nombreDeCategoriaEnUrl, String nombreDeSubcategoriaEnUrl) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                "select s from Subcategoria s " +
                        "join fetch s.categoria c " +
                        " where c.nombreEnUrl = :nombreCategoria and s.nombreEnUrl = :nombreSubcategoria",
                        Subcategoria.class)
                .setParameter("nombreSubcategoria", nombreDeSubcategoriaEnUrl)
                .setParameter("nombreCategoria", nombreDeCategoriaEnUrl)
                .uniqueResult();
        }
}



