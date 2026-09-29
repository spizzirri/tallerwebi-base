package com.tallerwebi.dominio;

import java.util.List;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RepositorioMascota {
  List<Mascota> buscarPorSocio(Long socioId);
}
