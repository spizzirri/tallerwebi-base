package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.RepositorioMascota;
import java.util.List;

public class RepositorioMascotaImpl implements RepositorioMascota {

  @Override
  public List<Mascota> buscarPorSocio(Long socioId) {
    return List.of();
  }
}
