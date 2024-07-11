package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface ServicioCalendario {

    List<DatosItemRendimiento> obtenerItemsRendimiento();
    void guardarItemRendimiento(ItemRendimiento itemRendimiento);
    DatosItemRendimiento obtenerItemMasSeleccionado();




}
