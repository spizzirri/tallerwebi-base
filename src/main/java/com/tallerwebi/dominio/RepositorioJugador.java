package com.tallerwebi.dominio;

public interface RepositorioJugador {
    Jugador buscar(Long jugadorId);
    void guardar(Jugador jugador);
    void modificar(Jugador jugador);
}
