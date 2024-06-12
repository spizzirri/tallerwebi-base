package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.ItemRendimiento;

import java.util.List;

public interface RepositorioReto {

    Reto obtenerRetoDisponible();

    Reto obtenerRetoPorId(Long retoId);

    void actualizarReto(Reto reto);

    Reto obtenerRetoEnProceso();



    List<Reto> obtenerTodosLosRetos();
}
