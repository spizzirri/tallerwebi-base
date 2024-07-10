package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("repositorioCalendario")
public class RepositorioCalendarioImpl implements RepositorioCalendario {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioCalendarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<ItemRendimiento> obtenerItemsRendimiento() {
        return this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();
    }

    @Override
    public void guardar(ItemRendimiento itemRendimiento) {
        if (itemRendimiento.getTipoRendimiento() == null) {
            throw new IllegalArgumentException("Tipo de rendimiento no puede ser nulo.");
        }

        if (!existeItemRendimientoPorFecha(itemRendimiento.getFecha())) {
            this.sessionFactory.getCurrentSession().save(itemRendimiento);
        } else {
            throw new ItemRendimientoDuplicadoException("Ya existe un ItemRendimiento para esta fecha.");
        }
    }


    @Override
    public boolean existeItemRendimientoPorFecha(LocalDate fecha) {
        String hql = "SELECT count(*) FROM ItemRendimiento WHERE fecha = :fecha";
        Long count = this.sessionFactory.getCurrentSession()
                .createQuery(hql, Long.class)
                .setParameter("fecha", fecha)
                .uniqueResult();
        return count > 0;
    }

    @Override
    public ItemRendimiento obtenerItemMasSeleccionado() {
        String hql = "SELECT ir.tipoRendimiento, COUNT(ir) FROM ItemRendimiento ir " +
                "WHERE ir.tipoRendimiento != :descanso " +
                "GROUP BY ir.tipoRendimiento " +
                "ORDER BY COUNT(ir) DESC";

        List<Object[]> results = this.sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("descanso", TipoRendimiento.DESCANSO)
                .setMaxResults(1)
                .getResultList();

        if (results.isEmpty()) {
            return null;
        }
        TipoRendimiento tipoRendimientoMasSeleccionado = (TipoRendimiento) results.get(0)[0];

        hql = "FROM ItemRendimiento ir WHERE ir.tipoRendimiento = :tipoRendimiento ORDER BY ir.fecha DESC";
        List<ItemRendimiento> items = this.sessionFactory.getCurrentSession()
                .createQuery(hql, ItemRendimiento.class)
                .setParameter("tipoRendimiento", tipoRendimientoMasSeleccionado)
                .setMaxResults(1)
                .getResultList();

        return items.isEmpty() ? null : items.get(0);
    }


    @Override
    public void vaciarCalendario() {
        Session session = this.sessionFactory.getCurrentSession();
        session.createQuery("DELETE FROM ItemRendimiento").executeUpdate();
    }

    @Override
    public List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento) {
        String hql = "FROM ItemRendimiento WHERE tipoRendimiento = :tipoRendimiento";
        return this.sessionFactory.getCurrentSession()
                .createQuery(hql, ItemRendimiento.class)
                .setParameter("tipoRendimiento", tipoRendimiento)
                .getResultList();
    }


    @Override
    public void eliminar(ItemRendimiento dia) {
        this.sessionFactory.getCurrentSession().delete(dia);
    }


    @Override
    public void actualizar(ItemRendimiento itemRendimiento) {
        this.sessionFactory.getCurrentSession().merge(itemRendimiento);
    }


}

