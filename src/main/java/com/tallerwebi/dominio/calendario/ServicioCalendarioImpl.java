package com.tallerwebi.dominio.calendario;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
@Service
@Transactional
public class ServicioCalendarioImpl implements ServicioCalendario {


    private RepositorioCalendario repositorioCalendario;

    @Autowired
    public ServicioCalendarioImpl(RepositorioCalendario repositorioCalendario){
        this.repositorioCalendario = repositorioCalendario;
    }

    @Override
    public List<DatosItemRendimiento> obtenerItemsRendimiento() {
        return this.convertirADatosItemRendimiento(this.repositorioCalendario.obtenerItemsRendimiento());
    }

    private List<DatosItemRendimiento> convertirADatosItemRendimiento(List<ItemRendimiento> listaItemRendimiento) {
    List<DatosItemRendimiento> datosItemRendimiento = new ArrayList<>();
        for (ItemRendimiento itemRendimiento: listaItemRendimiento) {
            datosItemRendimiento.add(new DatosItemRendimiento(itemRendimiento));
        }
        return datosItemRendimiento;
    }

    @Override
    public void guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        if (!repositorioCalendario.existeItemRendimientoPorFecha(itemRendimiento.getFecha())) {
            itemRendimiento.setFecha(LocalDate.now());
            itemRendimiento.setDiaNombre();
            repositorioCalendario.guardar(itemRendimiento);
        } else {
            throw new ItemRendimientoDuplicadoException("No se puede guardar tu rendimiento más de una vez el mismo día.");
        }
    }

    @Override
    public DatosItemRendimiento obtenerItemMasSeleccionado() {
        ItemRendimiento itemRendimiento = repositorioCalendario.obtenerItemMasSeleccionado();
        return itemRendimiento != null ? new DatosItemRendimiento(itemRendimiento) : null;
    }

}
