package com.tallerwebi.dominio;

import java.util.List;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RepositorioMascota {
  Mascota buscarPorId(Long id);
  List<Mascota> listar();
}
