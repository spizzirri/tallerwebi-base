package com.tallerwebi.dominio.Usuario;

public interface RepositorioUsuario {
  void guardar(Usuario usuario);
  Boolean existeUsuarioPorMail(String email);
  Boolean existeUsuarioPorDni(Long dni);
  void modificar(Usuario usuario);
  Usuario buscarUsuarioPorId(Long id);
  Usuario buscarUsuarioPorEmail(String email);
  void eliminar(Usuario usuario);
}
