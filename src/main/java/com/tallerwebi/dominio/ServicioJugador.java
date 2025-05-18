package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioJugador {
    List<Anuncio> getAnuncios();
    Jugador getJugadorActual();
}
