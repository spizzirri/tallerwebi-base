package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.util.List;

public interface ServicioVeterinaria {
  /** Lista las veterinarias adheridas; si servicioId no es null, solo las que lo ofrecen. */
  List<Veterinaria> listar(Long servicioId);
  Veterinaria buscarPorId(Long id);
  Veterinaria buscarPorUsuario(Long usuarioId);
  void registrar(Veterinaria veterinaria, List<Long> serviciosIds)
    throws DatosDeRegistroInvalidos, UsuarioExistente;
  void actualizarPerfil(Long usuarioId, Veterinaria datos, String email, List<Long> serviciosIds)
    throws DatosDeRegistroInvalidos, UsuarioExistente;
}
