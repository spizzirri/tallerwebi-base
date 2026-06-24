package com.tallerwebi.dominio.Hijos;

import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ServicioHijo {
  List<Hijo> obtenerHijosPorUsuario(Long idUsuario);

  void guardarHijo(Hijo hijo, MultipartFile fotoPerfil, Usuario usuario);

  void editarHijo(Long idHijo, Hijo datosNuevos, MultipartFile fotoPerfil, Usuario usuario);

  void actualizarAlias(Long hijoId, String aliasRetiro, Usuario usuario);
}
