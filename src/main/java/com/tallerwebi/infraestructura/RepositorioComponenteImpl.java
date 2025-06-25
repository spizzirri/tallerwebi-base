package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.entidades.Componente;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository("repositorioComponente")
public class RepositorioComponenteImpl implements RepositorioComponente {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioComponenteImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Componente> obtenerComponentesPorTipo(String tipo) {

        String hql = "FROM " + tipo;

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoEnStock(String tipo) {

        String hql = "FROM " + tipo + " c WHERE c.stock > 0";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {

        String hql = "FROM Componente WHERE id = :idComponente";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idComponente", idComponente);

        return (Componente)query.getSingleResult();
    }

    @Override
    public List<Componente> obtenerComponentesEnStock() {
        String hql = "FROM Componente where stock > 0 ";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);


        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentes() {
        String hql = "FROM Componente";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorQuery(String nombre) {
        String hql = "FROM Componente WHERE lower(nombre) LIKE lower(:nombre)";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio) {

        String hql = "FROM Componente WHERE precio < :precio";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("precio", precio);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoYPorQuery(String tipo, String nombre) throws ClassNotFoundException {
        Class<?> tipoClase = Class.forName("com.tallerwebi.dominio.entidades." + tipo);
        String hql = "FROM Componente c WHERE TYPE(c) = :tipo AND lower(c.nombre) LIKE lower(:nombre)";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("tipo", tipoClase);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }


}
