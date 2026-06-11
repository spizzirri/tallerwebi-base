package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioLogin {
  Usuario buscar(String email);
  void registrar(Usuario usuario) throws UsuarioExistente;
}
