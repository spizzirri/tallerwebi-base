package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioMascota")
@Transactional
public class ServicioMascotaImpl implements ServicioMascota {

  private final RepositorioMascota repositorioMascota;
  private final RepositorioPlan repositorioPlan;

  @Autowired
  public ServicioMascotaImpl(
    RepositorioMascota repositorioMascota,
    RepositorioPlan repositorioPlan
  ) {
    this.repositorioMascota = repositorioMascota;
    this.repositorioPlan = repositorioPlan;
  }

  @Override
  public Mascota buscarPorId(Long id) {
    return id == null ? null : repositorioMascota.buscarPorId(id);
  }

  @Override
  public Mascota buscarPorIdParaSocio(Socio socio, Long mascotaId) {
    if (socio == null || mascotaId == null) return null;
    return socio.buscarMascota(mascotaId);
  }

  @Override
  public void cambiarPlan(Socio socio, Long mascotaId, Long planId) {
    if (socio == null || socio.buscarMascota(mascotaId) == null) {
      return;
    }
    Mascota mascota = repositorioMascota.buscarPorId(mascotaId);
    Plan plan = repositorioPlan.buscarPorId(planId);
    if (mascota != null && plan != null) {
      mascota.setPlan(plan);
    }
  }

  @Override
  public void darDeBaja(Socio socio, Long mascotaId) {
    if (socio == null || socio.buscarMascota(mascotaId) == null) {
      return;
    }
    Mascota mascota = repositorioMascota.buscarPorId(mascotaId);
    if (mascota != null) {
      mascota.setPlan(null);
    }
  }
}
