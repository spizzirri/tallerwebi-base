package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCategorias {
    List<Categoria> listarCategorias();
    Categoria buscarCategoriaConSusSubcategoriasPorNombreDeRuta(String nombreDeCategoriaEnUrl);
    List<Categoria> listarCategoriaConSubCategorias();
}