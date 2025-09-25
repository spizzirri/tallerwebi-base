package com.tallerwebi.dominio;

import java.util.List;

import org.springframework.stereotype.Service;

@Service("servicioHamburgueserias")
public class ServicioHamburgueseriasImpl implements ServicioHamburgueserias {

    private RepositorioHamburgueserias repositorioHamburgueserias;

    public ServicioHamburgueseriasImpl(RepositorioHamburgueserias repositorioHamburgueserias) {
        this.repositorioHamburgueserias = repositorioHamburgueserias;
    }

    public List<Hamburgueseria> obtenerHamburgueseriasCercanas(Double latitud, Double longitud) {
        return repositorioHamburgueserias.buscarHamburgueseriasCercanas(latitud, longitud);
    }

}
