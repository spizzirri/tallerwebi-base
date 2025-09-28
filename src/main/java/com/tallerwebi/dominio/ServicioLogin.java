package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;
    void asignarCarrerasPorNombre(Usuario usuario, List<String> nombresCarreras);

}