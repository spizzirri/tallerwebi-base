package com.tallerwebi.dominio;

import javax.transaction.Transactional;

import com.tallerwebi.presentacion.CartaDto;

@Transactional
public interface ServicioCarta {

    Boolean crear(CartaDto carta);
}
