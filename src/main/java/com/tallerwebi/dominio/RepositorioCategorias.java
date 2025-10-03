package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCategorias {
    List<Categorias> listarCategorias();
    Categorias buscarCategoriaPorNombre(String nombreDeCategoriaEnUrl);
}