package com.tallerwebi.dominio.Usuario;

import org.springframework.web.multipart.MultipartFile;

public interface ServicioUsuario {
  void actualizarMail(Long id, String mailNuevo);

  void actualizarCelular(Long id, Long celular);

  void actualizarFoto(Long id, MultipartFile fotoPerfil);

  Usuario buscarPorId(Long id);

  void eliminarCuenta(Long id);
}
