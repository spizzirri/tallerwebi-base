package com.tallerwebi.dominio;

import javax.transaction.Transactional;

import com.tallerwebi.presentacion.dtos.CartaDto;

@Transactional
public interface ServicioCarta {

    Boolean crear(CartaDto carta);
}
