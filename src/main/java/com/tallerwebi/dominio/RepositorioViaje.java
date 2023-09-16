package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

public interface RepositorioViaje {
    void guardar(Viaje viaje);

    Viaje buscarPorId(Long id);
}
