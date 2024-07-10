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

        if (itemRendimiento.getTipoRendimiento() == null) {
            throw new IllegalArgumentException("Tipo de rendimiento no puede ser nulo.");
        }
        this.repositorioCalendario.guardar(itemRendimiento);

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





    @Override
    public ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento) {
        repositorioCalendario.guardar(itemRendimiento);
        return itemRendimiento;
    }

    @Override
    public void eliminarItemRendimiento(ItemRendimiento dia) {
        repositorioCalendario.eliminar(dia);
    }

    @Override
    public List<TipoRendimiento> obtenerOpcionesRendimiento() {
        return List.of(TipoRendimiento.ALTO, TipoRendimiento.NORMAL, TipoRendimiento.BAJO, TipoRendimiento.DESCANSO);
    }

    @Override
    public void vaciarCalendario() {
        repositorioCalendario.vaciarCalendario();
    }



}
