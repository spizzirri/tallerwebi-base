package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioPerfilImpl(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public Usuario obtenerPerfil(String email) {
        return repositorioUsuario.buscarPorEmail(email);
    }

    @Override
    public void actualizarPerfil(Usuario usuario) {
        repositorioUsuario.modificar(usuario);
    }
}