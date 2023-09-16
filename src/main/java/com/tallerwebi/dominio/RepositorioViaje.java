package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;

public interface RepositorioViaje {
    void guardar(Viaje viaje);

    Viaje buscarPorId(Long id);

    List<Viaje> listarViajes();

    List<Viaje> buscarPorOrigen(String origen);
}
