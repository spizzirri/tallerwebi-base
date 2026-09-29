package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioServicio {
  List<Servicio> listar();
  Servicio buscarPorId(Long id);
}
