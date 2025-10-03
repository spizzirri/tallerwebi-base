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
    public List<Categorias> listarCategorias() {
        return repositorioCategorias.listarCategorias();
    }

    @Transactional(readOnly = true)
    public Categorias buscarCategoriaPorNombre(String nombreDeCategoriaEnUrl) {
        return repositorioCategorias.buscarCategoriaPorNombre(nombreDeCategoriaEnUrl);
    }

}
