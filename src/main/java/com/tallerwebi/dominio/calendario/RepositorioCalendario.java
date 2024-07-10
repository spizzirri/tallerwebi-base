package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface RepositorioCalendario {

        List<ItemRendimiento> obtenerItemsRendimiento();

        void guardar(ItemRendimiento dia);

        boolean existeItemRendimientoPorFecha(LocalDate fecha);

        ItemRendimiento obtenerItemMasSeleccionado();

}
