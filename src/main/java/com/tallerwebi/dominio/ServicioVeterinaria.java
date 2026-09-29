package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioVeterinaria {
  /** Lista las veterinarias adheridas; si servicioId no es null, solo las que lo ofrecen. */
  List<Veterinaria> listar(Long servicioId);
  Veterinaria buscarPorId(Long id);
}
