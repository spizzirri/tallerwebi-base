package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioMascota {
  Mascota buscarPorId(Long id);
  List<Mascota> listar();
}
