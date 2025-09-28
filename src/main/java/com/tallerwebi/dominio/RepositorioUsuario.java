package com.tallerwebi.dominio;


import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioUsuario {

    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);


    Carrera buscarCarreraPorNombre(String nombre);
}