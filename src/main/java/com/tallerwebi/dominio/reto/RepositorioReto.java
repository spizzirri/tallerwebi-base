package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

public interface RepositorioReto {

    Reto obtenerRetoDisponible();

    Reto obtenerRetoPorId(Long retoId);

    void empezarRetoActualizar(Reto reto);

    Reto obtenerRetoEnProceso();


    void terminarReto(Reto reto);
}
