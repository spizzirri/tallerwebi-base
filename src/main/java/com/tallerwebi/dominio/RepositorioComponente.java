package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;

import java.util.List;

public interface RepositorioComponente {
    List<Componente> obtenerComponentesPorTipo(String tipo);

    Componente obtenerComponentePorId(Long idComponente);
}
