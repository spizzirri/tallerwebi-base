package com.tallerwebi.dominio.reto;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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

}
