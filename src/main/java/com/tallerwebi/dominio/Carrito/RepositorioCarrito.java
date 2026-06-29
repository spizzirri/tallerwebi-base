package com.tallerwebi.dominio.Carrito;

public interface RepositorioCarrito {
  Carrito buscarPorId(Long id);

  void guardar(Carrito carrito);

  Carrito buscarPorUsuario(Long usuarioId);

  void eliminarPorUsuario(Long id);
}
