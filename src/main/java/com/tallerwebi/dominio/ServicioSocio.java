package com.tallerwebi.dominio;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface ServicioSocio {
  Socio buscarPorUsuario(Long usuarioId);
}
