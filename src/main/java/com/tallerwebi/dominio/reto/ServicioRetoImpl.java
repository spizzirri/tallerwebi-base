package com.tallerwebi.dominio.reto;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class ServicioRetoImpl implements ServicioReto{

    private RepositorioReto repositorioReto;

    public ServicioRetoImpl(RepositorioReto repositorioReto){
        this.repositorioReto = repositorioReto;
    }

    @Override
    public Reto obtenerRetoDisponible() {
        Reto reto = null;
        boolean retoEncontrado = false;
        while (!retoEncontrado) {
            reto = repositorioReto.obtenerRetoDisponible();
            if (reto != null) {
                retoEncontrado = true;
            }
        }
        return reto;
    }

    @Override
    @Transactional
    public void empezarRetoActualizado(Long retoId) {
        Reto reto = repositorioReto.obtenerRetoPorId(retoId);
        if (reto != null) {
            reto.setSeleccionado(true); // Marcar como seleccionado
            reto.setEnProceso(true);
            reto.setFechaInicio(LocalDate.now());
            repositorioReto.empezarRetoActualizar(reto); // Actualizar el reto en el repositorio
        }
    }

    @Override
    public Reto obtenerRetoPorId(Long retoId) {
        return repositorioReto.obtenerRetoPorId(retoId);
    }

    @Override
    public Reto obtenerRetoEnProceso() {
        return repositorioReto.obtenerRetoEnProceso();
    }

    @Override
    @Transactional
    public Long terminarReto(Long retoId) {
        Reto reto = repositorioReto.obtenerRetoPorId(retoId);
        long diasPasados = 0;
        if (reto != null) {
            // Calcular la diferencia de días
            LocalDate fechaInicio = reto.getFechaInicio();
            LocalDate fechaActual = LocalDate.now();
            diasPasados = ChronoUnit.DAYS.between(fechaInicio, fechaActual);

            reto.setSeleccionado(false);
            reto.setEnProceso(false);
            reto.setFechaInicio(null);
            repositorioReto.terminarReto(reto); // Actualizar el reto en el repositorio
        }
        return diasPasados;
    }


}
