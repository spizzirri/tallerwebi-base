package com.tallerwebi.dominio;


import com.tallerwebi.infraestructura.RepositorioOfertaImpl;
import com.tallerwebi.infraestructura.RepositorioUsuarioImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("servicioOferta")
@Transactional
public class ServicioOfertaImpl {

    private final RepositorioOferta repositorioOferta;
    private final RepositorioUsuarioImpl repositorioUsuario;

    @Autowired
    public ServicioOfertaImpl(RepositorioOferta repositorioOferta, RepositorioUsuarioImpl repositorioUsuario) {
        this.repositorioOferta = repositorioOferta;
        this.repositorioUsuario = repositorioUsuario;
    }


    public void crearOferta(Oferta oferta) {

        //oferta.setFechaOferta(LocalDateTime.now());
        repositorioOferta.guardarOferta(oferta);
    }


}
