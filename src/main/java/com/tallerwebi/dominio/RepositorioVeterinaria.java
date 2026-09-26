package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioVeterinaria {
  List<Veterinaria> listar();
  Veterinaria buscarPorId(Long id);
}
