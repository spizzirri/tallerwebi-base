package com.tallerwebi.dominio.calendario;

import java.util.List;

public interface ServicioItemRendimiento {

    List<ItemRendimiento> obtenerItems();
    List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);
    ItemRendimiento getItemPorId(Long id);
    ItemRendimiento guardarItemRendimiento(ItemRendimiento itemRendimiento);
    ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento);
    void eliminarItemRendimiento(ItemRendimiento itemRendimiento);
    List<TipoRendimiento> obtenerOpcionesRendimiento();
}
