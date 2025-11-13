package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Item;
import com.tallerwebi.dominio.Orden;
import com.tallerwebi.dominio.RepositorioItem;
import com.tallerwebi.dominio.RepositorioOrden;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioItem")

public class RepositorioItemImpl implements RepositorioItem {

    private SessionFactory sessionFactory;

    public RepositorioItemImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Item item) {
        sessionFactory.getCurrentSession().save(item);
    }

    @Override
    public Item buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Item.class, id);
    }

    @Override
    public List<Item> obtenerItemsDeOrden(Orden orden) {

        String hql = "FROM Item WHERE orden = :orden";

        return sessionFactory.getCurrentSession()
                .createQuery(hql,Item.class)
                .setParameter("orden",orden)
                .getResultList();
    }
}
