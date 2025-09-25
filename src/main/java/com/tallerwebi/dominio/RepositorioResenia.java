package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioResenia {
    void guardar(Resenia resenia);
    Resenia buscarPorId(Long id);
    List<Resenia> buscarPorHamburguesa(Long hamburguesaId);
    void modificar(Resenia resenia);
    void eliminar(Resenia resenia);
}
