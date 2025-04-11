package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.CartaDto;

public class ServicioCartaImpl {
    public Boolean crear(CartaDto carta) {

        return !carta.getNombre().isEmpty();
    }
}
