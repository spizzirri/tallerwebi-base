package com.tallerwebi.dominio.Hijos;

import java.util.List;

public interface RepositorioHijo {
  List<Hijo> listarHijos(Long idPadre);

  Hijo buscarPorId(Long id);

  void guardar(Hijo hijo);

  Boolean existeHijoPorDni(long dni);

  boolean existeAlias(String alias);

  void modificar(Hijo hijo);

  void eliminar(Hijo hijoExistente);
}
