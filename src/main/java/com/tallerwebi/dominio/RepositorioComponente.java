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



    Componente buscarComponenteConImagenesPorId(Long id);

    Componente obtenerComponentePorId(Long idComponente);

    List<Componente> obtenerComponentes();

    List<Componente> obtenerComponentesEnStock();

    boolean descontarStockDeUnComponente(Long componenteId, Integer cantidadARestar);

    boolean devolverStockDeUnComponente(Long componenteId, Integer cantidadASumar);

    List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio);

    List<Componente> obtenerComponentesPorTipoYPorQuery(String tipo, String query) throws ClassNotFoundException;

    List<Componente> obtenerComponentesPorQuery(String query);

}
