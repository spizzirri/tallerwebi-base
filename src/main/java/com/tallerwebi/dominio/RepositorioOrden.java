package com.tallerwebi.dominio;

public interface RepositorioOrden {
    void guardar(Orden orden);
    Orden buscarPorId(Long id);
    Boolean crear(Orden orden);
}
