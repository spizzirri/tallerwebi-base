package com.tallerwebi.dominio.calendario;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
@Service
@Transactional
public class ServicioCalendarioImpl implements ServicioCalendario {


    private RepositorioCalendario repositorioCalendario;

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
    public List<DatosItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento) {
        return convertirADatosItemRendimiento(this.repositorioCalendario.obtenerItemsPorTipoRendimiento(tipoRendimiento));
    }

    @Override
    public void guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        this.repositorioCalendario.guardar(itemRendimiento);
    }

    //..................................................................

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
        return List.of();
    }


}
