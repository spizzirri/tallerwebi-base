package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerwebi.presentacion.CartaDto;

@Service
public class ServicioCartaImpl implements ServicioCarta {
    
    private RepositorioCarta repositorioCarta; 

    @Autowired
    public ServicioCartaImpl(RepositorioCarta repositorioCarta) {
        this.repositorioCarta = repositorioCarta;
    }

    @Override
    public Boolean crear(CartaDto carta) {
        return this.repositorioCarta.crear(carta.obtenerEntidad());
    }
}
