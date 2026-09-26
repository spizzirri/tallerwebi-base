package com.tallerwebi.dominio;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioLogin {
  Usuario consultarUsuario(String email, String password);
}
