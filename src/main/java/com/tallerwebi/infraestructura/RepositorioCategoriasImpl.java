package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Categoria;
import com.tallerwebi.dominio.Subcategoria;
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
    public Categoria buscarCategoriaConSusSubcategoriasPorNombreDeRuta(String nombreDeCategoriaEnUrl) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select distinct c from Categoria c " +
                        "left join fetch c.subcategorias s " +
                        "where c.nombreEnUrl = :nombre " +
                        "order by s.nombre asc", Categoria.class)
                .setParameter("nombre", nombreDeCategoriaEnUrl)
                .uniqueResult();
    }

    @Override
    public List<Categoria> listarCategoriaConSubCategorias() {
        final Session session = sessionFactory.getCurrentSession();

        // Usamos fetch junto con join para indicar que se debe traer una relación asociada en la misma consulta,
        // en lugar de cargarla después (carga lazy), evitando el envío repetido de varias consultas.

        return session.createQuery("select distinct c from Categoria c " +
                "left join fetch c.subcategorias s" +
                " order by c.nombre asc, s.nombre asc ", Categoria.class)
                .list();
    }
}
