package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioHamburgueserias {
    List<Hamburgueseria> buscarHamburgueseriasCercanas(Double latitud, Double longitud);
}

