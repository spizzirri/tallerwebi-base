package com.tallerwebi.dominio;

import java.util.List;

/* Interfaz para el repositorio de mascotas */
public interface RepositorioMascota {
  List<Mascota> listar();
  Mascota buscarPorId(Long id);
}
