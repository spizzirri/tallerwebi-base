package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioItem {

    void guardar(Item item);
    Item buscarPorId(Long id);
    List<Item> obtenerItemsDeOrden(Orden orden);
}
