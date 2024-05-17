package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface ServicioCalendario {

    List<DatosItemRendimiento> obtenerItemsRendimiento();
    List<DatosItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);
    void guardarItemRendimiento(ItemRendimiento itemRendimiento);

    //.....................................................
    ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento);
    void eliminarItemRendimiento(ItemRendimiento itemRendimiento);
    List<TipoRendimiento> obtenerOpcionesRendimiento();

}
