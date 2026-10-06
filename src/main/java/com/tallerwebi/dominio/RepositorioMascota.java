package com.tallerwebi.dominio;

/* Interfaz para el repositorio de mascotas */
public interface RepositorioMascota {
  Mascota buscarPorId(Long id);
}
