package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioMascota")
@Transactional
public class ServicioMascota {

  private RepositorioMascota repositorioMascota;

  @Autowired
  public ServicioMascota(RepositorioMascota repositorioMascota) {
    this.repositorioMascota = repositorioMascota;
  }

  public Mascota buscarPorId(Long id) {
    return id == null ? null : repositorioMascota.buscarPorId(id);
  }

  public Mascota buscarPorIdParaSocio(Socio socio, Long mascotaId) {
   
    if (socio == null || mascotaId == null) {
      return null;
    }
    return socio.buscarMascota(mascotaId);
  }
}
