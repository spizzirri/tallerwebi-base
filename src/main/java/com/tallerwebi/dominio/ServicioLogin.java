package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.UsuarioDto;

public interface ServicioLogin {

    UsuarioDto consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;
}
