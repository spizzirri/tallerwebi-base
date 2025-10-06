package com.tallerwebi.dominio;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioSubcategorias {

    private final RepositorioSubcategorias repositorioSubcategorias;

    public ServicioSubcategorias(RepositorioSubcategorias repositorioSubcategorias) {
        this.repositorioSubcategorias = repositorioSubcategorias;
    }

    @Transactional(readOnly = true)
    public List<Subcategorias> listarSubcategorias() {
        return repositorioSubcategorias.listarSubcategorias();
    }

    @Transactional(readOnly = true)
    public Subcategorias buscarSubcategoriaPorNombreDeRuta(String nombreDeSubcategoriaEnUrl) {
        return repositorioSubcategorias.buscarSubcategoriaPorNombreDeRuta(nombreDeSubcategoriaEnUrl);
    }

}
