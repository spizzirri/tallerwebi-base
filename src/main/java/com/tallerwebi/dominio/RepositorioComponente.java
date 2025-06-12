package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface RepositorioComponente {
    List<Componente> obtenerComponentesPorTipo(String tipo);

    Componente obtenerComponentePorId(Long idComponente);
    List<Componente> obtenerComponentes();

    List<Componente> obtenerComponentesEnStock();

    List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio);
    List<Componente> obtenerComponentesPorTipoYPorQuery(String tipo, String query) throws ClassNotFoundException;
    List<Componente> obtenerComponentesPorQuery(String query);
}
