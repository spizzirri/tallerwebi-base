package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface ServicioCalendario {

    List<DatosItemRendimiento> obtenerItemsRendimiento();
    List<DatosItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);
    void guardarItemRendimiento(ItemRendimiento itemRendimiento);
    void setRepositorioCalendario(RepositorioCalendario mockRepositorio);
    //.....................................................
    ItemRendimiento getItemPorId(Long id);
    ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento);
    void eliminarItemRendimiento(ItemRendimiento itemRendimiento);
    List<TipoRendimiento> obtenerOpcionesRendimiento();

}
