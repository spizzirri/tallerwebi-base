package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface RepositorioComponente {
    List<Componente> obtenerComponentesPorTipo(String tipo);

    Componente obtenerComponentePorId(Long idComponente);

    List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio);
}
