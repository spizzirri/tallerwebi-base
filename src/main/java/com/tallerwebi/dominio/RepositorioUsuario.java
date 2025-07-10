package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;

public interface RepositorioUsuario {

    Usuario buscarUsuario(String email);

    Usuario buscarUsuarioPorEmailYPassword(String email, String password);

    void guardar(Usuario usuario);

    Usuario buscar(String email);

    void modificar(Usuario usuario);
}

