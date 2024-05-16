package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository("repositorioCalendario")
public class RepositorioCalendarioImpl implements RepositorioCalendario {

    private SessionFactory sessionFactory;
    private List<ItemRendimiento> itemsRendimiento = new ArrayList<>();

    @Autowired
    public RepositorioCalendarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.itemsRendimiento = new ArrayList<>();
        itemsRendimiento.add(new ItemRendimiento(TipoRendimiento.BAJO));
        itemsRendimiento.add(new ItemRendimiento(TipoRendimiento.DESCANSO));
        itemsRendimiento.add(new ItemRendimiento(TipoRendimiento.ALTO));
    }

    @Override
    public List<ItemRendimiento> obtenerItemsRendimiento() {
        return this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();
    }

    @Override
    public void guardar(ItemRendimiento itemRendimiento) {
        this.sessionFactory.getCurrentSession().save(itemRendimiento);
    }

    @Override
    public void vaciarCalendario() {
        Session session = this.sessionFactory.getCurrentSession();
        session.createQuery("DELETE FROM ItemRendimiento").executeUpdate();
    }

    @Override
    public List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento) {
        return List.of();
    }

    @Override
    public void eliminar(ItemRendimiento dia) {
        this.sessionFactory.getCurrentSession().delete(dia);
    }

    @Override
    public ItemRendimiento buscar(long id) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.get(ItemRendimiento.class, id);
    }

    @Override
    public void actualizar(ItemRendimiento itemRendimiento) {
        this.sessionFactory.getCurrentSession().merge(itemRendimiento);
    }


}

