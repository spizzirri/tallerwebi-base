package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;

public interface RepositorioViaje {
    void guardar(Viaje viaje);

    Viaje buscarPorId(Long id);

    List<Viaje> buscarPorFecha(String fechaHora);

    void eliminar(Viaje viaje);

    List<Viaje> buscarPorOrigenDestinoYfecha(String origen, String destino, String fechaHora);
}
