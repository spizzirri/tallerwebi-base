package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

public interface ServicioReto {

    Reto obtenerRetoDisponible();
    void empezarRetoActualizado(Long retoId);
    Reto obtenerRetoPorId(Long retoId);
    Reto obtenerRetoEnProceso();

}
