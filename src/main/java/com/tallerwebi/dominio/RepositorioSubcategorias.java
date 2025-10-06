package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioSubcategorias {
    List<Subcategorias> listarSubcategorias();
    Subcategorias buscarSubcategoriaPorNombreDeRuta(String nombreDeSubcategoriaEnUrl);
}
