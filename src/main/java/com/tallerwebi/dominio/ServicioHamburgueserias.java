package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioHamburgueserias {
    List<Hamburgueseria> obtenerHamburgueseriasCercanas(Double latitud, Double longitud);
}