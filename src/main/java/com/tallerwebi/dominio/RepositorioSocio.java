package com.tallerwebi.dominio;

public interface RepositorioSocio {
  void guardar(Socio socio);
  Socio buscarPorDni(String dni);
}
