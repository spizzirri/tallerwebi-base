package com.tallerwebi.dominio;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioVerHamburguesa")
@Transactional
public class ServicioVerHamburguesaImpl implements ServicioVerHamburguesa {

    @Autowired
    public ServicioVerHamburguesaImpl() {
    }

    @Override
    public void mostrar() {
        // lógica
    }
}