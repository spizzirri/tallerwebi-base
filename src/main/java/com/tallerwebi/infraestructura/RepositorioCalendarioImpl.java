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

    @Autowired
    public RepositorioCalendarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private List<ItemRendimiento> calendarioItems;

    public RepositorioCalendarioImpl() {
        this.calendarioItems = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual
        calendarioItems.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
    }

    @Override
    public void guardar(ItemRendimiento dia) {
        this.sessionFactory.getCurrentSession().save(dia);
    }

    @Override
    public void vaciarCalendario() {
        Session session = this.sessionFactory.getCurrentSession();
        session.createQuery("DELETE FROM ItemRendimiento").executeUpdate();
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

