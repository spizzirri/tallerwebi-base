package com.tallerwebi.dominio.calendario;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
@Service
public class ServicioCalendarioImpl implements ServicioCalendario {

    private RepositorioCalendario repositorioCalendario;
    private List<ItemRendimiento> listaItemRendimiento;
    LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual

    public ServicioCalendarioImpl(RepositorioCalendario repositorioCalendario){
        this.repositorioCalendario = repositorioCalendario;
        this.listaItemRendimiento = new ArrayList<>();
        listaItemRendimiento.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
        listaItemRendimiento.add(new ItemRendimiento(fechaActual, TipoRendimiento.NORMAL));
        listaItemRendimiento.add(new ItemRendimiento(fechaActual, TipoRendimiento.DESCANSO));
    }

    @Override
    public List<DatosItemRendimiento> obtenerItems() {
        return this.convertirADatosItemRendimiento(this.listaItemRendimiento);
    }

    private List<DatosItemRendimiento> convertirADatosItemRendimiento(List<ItemRendimiento> listaItemRendimiento) {
    List<DatosItemRendimiento> datosItemRendimiento = new ArrayList<>();
        for (ItemRendimiento itemRendimiento: this.listaItemRendimiento) {
            datosItemRendimiento.add(new DatosItemRendimiento(itemRendimiento));
        }
        return datosItemRendimiento;
    }

    @Override
    public List<DatosItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento) {
        List<DatosItemRendimiento> itemsRendimientoFiltrados = new ArrayList<>();
        for (ItemRendimiento itemRendimiento : this.listaItemRendimiento) {
            if (itemRendimiento.getTipoRendimiento().equals(tipoRendimiento)){
                itemsRendimientoFiltrados.add(new DatosItemRendimiento(itemRendimiento));
            }
        }
        return itemsRendimientoFiltrados;
    }

    //..................................................................
    @Override
    public ItemRendimiento getItemPorId(Long id) {
        return repositorioCalendario.buscar(id);
    }

    @Override
    public ItemRendimiento guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        return repositorioCalendario.guardar(itemRendimiento);
    }

    @Override
    public ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento) {
        return repositorioCalendario.guardar(itemRendimiento);
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
