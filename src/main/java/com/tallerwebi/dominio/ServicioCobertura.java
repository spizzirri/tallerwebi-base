package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioCobertura {
  List<Servicio> listarServicios();
  List<ItemCobertura> coberturaDe(Mascota mascota, FiltroCobertura filtro);
  /** Los servicios que ofrece la veterinaria, marcando cuáles cubre el plan de la mascota. */
  List<ItemCobertura> coberturaEn(Veterinaria veterinaria, Mascota mascota);
}
