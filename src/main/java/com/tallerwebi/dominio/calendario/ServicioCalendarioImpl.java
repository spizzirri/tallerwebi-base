package com.tallerwebi.dominio.calendario;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
@Service
public class ServicioCalendarioImpl implements ServicioCalendario {

    private List<ItemRendimiento> listaItemRendimiento;
    private RepositorioCalendario repositorioCalendario;
    LocalDate fechaActual = LocalDate.now(); // Obtener fecha actual

    public ServicioCalendarioImpl(RepositorioCalendario repositorioCalendario){
        this.repositorioCalendario = repositorioCalendario;
    }

    @Override
    public List<DatosItemRendimiento> obtenerItemsRendimiento() {
        return this.convertirADatosItemRendimiento(this.repositorioCalendario.obtenerItemsRendimiento());
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
        return convertirADatosItemRendimiento(this.repositorioCalendario.obtenerItemsPorTipoRendimiento(tipoRendimiento));
    }

    @Override
    public List<DatosItemRendimiento> guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        // Save the itemRendimiento object
        this.repositorioCalendario.guardar(itemRendimiento);
        // Retrieve saved items
        List<ItemRendimiento> itemsGuardados = this.repositorioCalendario.obtenerItemsRendimiento();
        // Convert items to DatosItemRendimiento
        List<DatosItemRendimiento> itemsRendimientoFiltrados = new ArrayList<>();
        for (ItemRendimiento itemRendimientoGuardado : itemsGuardados) {
            itemsRendimientoFiltrados.add(new DatosItemRendimiento(itemRendimientoGuardado));
        }
        // Return the list of DatosItemRendimiento
        return itemsRendimientoFiltrados;
    }

    @Override
    public void setRepositorioCalendario(RepositorioCalendario mockRepositorio) {
        this.repositorioCalendario = mockRepositorio;
    }

    //..................................................................
    @Override
    public ItemRendimiento getItemPorId(Long id) {
        return repositorioCalendario.buscar(id);
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
