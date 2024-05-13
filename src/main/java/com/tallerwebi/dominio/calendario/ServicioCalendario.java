package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface ServicioCalendario {

    List<DatosItemRendimiento> obtenerItems();
//    List<DatosItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);
    //.....................................................
    ItemRendimiento getItemPorId(Long id);
    ItemRendimiento guardarItemRendimiento(ItemRendimiento itemRendimiento);
    ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento);
    void eliminarItemRendimiento(ItemRendimiento itemRendimiento);
    List<TipoRendimiento> obtenerOpcionesRendimiento();
}
