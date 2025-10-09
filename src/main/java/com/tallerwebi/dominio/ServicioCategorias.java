package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioCategorias {

    private final RepositorioCategorias repositorioCategorias;

    public ServicioCategorias(RepositorioCategorias repositorio) {
        this.repositorioCategorias = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return repositorioCategorias.listarCategorias();
    }

    @Transactional(readOnly = true)
    public Categoria buscarCategoriaConSusSubcategoriasPorNombreDeRuta(String nombreDeCategoriaEnUrl) {
        return repositorioCategorias.buscarCategoriaConSusSubcategoriasPorNombreDeRuta(nombreDeCategoriaEnUrl);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriaConSubCategorias() {
        return repositorioCategorias.listarCategoriaConSubCategorias();
    }

}
