package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioSubcategorias {
    List<Subcategoria> listarSubcategorias();
    Subcategoria buscarSubcategoriaPorNombreDeRuta(String nombreDeCategoriaEnUrl, String nombreDeSubcategoriaEnUrl);
}
