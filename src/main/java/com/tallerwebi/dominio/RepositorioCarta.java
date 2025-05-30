package com.tallerwebi.dominio;

import java.util.List;

import com.tallerwebi.dominio.entidades.Carta;

public interface RepositorioCarta {

    Boolean crear(Carta obtenerEntidad);
    Carta obtenerPor(Long id);
    List<Carta> obtener();
    void actualizar(Carta carta);
}
