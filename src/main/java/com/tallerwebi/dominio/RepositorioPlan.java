package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPlan {
  List<Plan> listar();
  Plan buscarPorId(Long id);
}
