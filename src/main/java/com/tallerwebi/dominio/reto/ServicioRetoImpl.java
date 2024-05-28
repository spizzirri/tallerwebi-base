package com.tallerwebi.dominio.reto;

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
        return repositorioReto.obtenerReto();
    }

    @Override
    public void empezarReto(Long retoId) {
        repositorioReto.empezarReto(retoId);
    }

    @Override
    public Reto obtenerRetoPorId(Long retoId) {
        return repositorioReto.obtenerRetoPorId(retoId);
    }

}
