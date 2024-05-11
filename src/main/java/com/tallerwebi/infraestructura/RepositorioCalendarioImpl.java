package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("repositorioDiaCalendario")
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
        calendarioItems.add(new ItemRendimiento(1L, fechaActual, TipoRendimiento.DESCANSO));
    }


    @Override
    public void guardar(ItemRendimiento dia) {
        this.sessionFactory.getCurrentSession().save(dia);
    }

    @Override
    public ItemRendimiento buscar(Integer id) {
        return null;
    }

    @Override
    public void modificar(ItemRendimiento dia) {}


}

