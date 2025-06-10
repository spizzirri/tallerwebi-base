package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioRegistro {

    String registrarUsuario(Usuario usuario) throws UsuarioExistente;
}
