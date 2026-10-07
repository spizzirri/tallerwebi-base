package com.tallerwebi.dominio;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RepositorioMascota {
  Mascota buscarPorId(Long id);
}
