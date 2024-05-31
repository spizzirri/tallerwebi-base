package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

public interface RepositorioReto {

    Reto obtenerReto();
    void empezarReto(Long retoId);
    Reto obtenerRetoPorId(Long retoId);
    Reto obtenerRetoEnProceso();


}
