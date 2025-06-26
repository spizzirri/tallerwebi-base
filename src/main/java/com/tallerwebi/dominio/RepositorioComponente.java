package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface RepositorioComponente {
    List<Componente> obtenerComponentesPorTipo(String tipo);
    List<Componente> obtenerComponentesPorTipoEnStock(String tipo);
    List<Componente> obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(String tipo);
    List<Componente> obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(String tipo, String nombre);

    Componente obtenerComponentePorId(Long idComponente);
    List<Componente> obtenerComponentes();

    List<Componente> obtenerComponentesEnStock();

    List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio);
    List<Componente> obtenerComponentesPorTipoYPorQuery(String tipo, String query) throws ClassNotFoundException;
    List<Componente> obtenerComponentesPorQuery(String query);

}
