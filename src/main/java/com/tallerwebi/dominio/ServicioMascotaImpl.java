package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioMascota")
@Transactional
public class ServicioMascotaImpl implements ServicioMascota {

  private final RepositorioMascota repositorioMascota;
  private final RepositorioPlan repositorioPlan;
  private final RepositorioSocio repositorioSocio;

  @Autowired
  public ServicioMascotaImpl(
    RepositorioMascota repositorioMascota,
    RepositorioPlan repositorioPlan,
    RepositorioSocio repositorioSocio
  ) {
    this.repositorioMascota = repositorioMascota;
    this.repositorioPlan = repositorioPlan;
    this.repositorioSocio = repositorioSocio;
  }

  @Override
  public void agregarMascota(Socio socio, Mascota mascota, Long planId)
    throws DatosDeRegistroInvalidos {
    validarMascota(mascota);
    mascota.setId(null);
    mascota.setPlan(buscarPlan(planId));
    buscarSocioActual(socio).agregarMascota(mascota);
  }

  private Plan buscarPlan(Long planId) throws DatosDeRegistroInvalidos {
    Plan plan = planId == null ? null : repositorioPlan.buscarPorId(planId);
    if (plan == null) {
      throw new DatosDeRegistroInvalidos("Elegí un plan para tu mascota");
    }
    return plan;
  }

  private Socio buscarSocioActual(Socio socio) throws DatosDeRegistroInvalidos {
    if (socio == null || socio.getUsuario() == null || socio.getUsuario().getId() == null) {
      throw new DatosDeRegistroInvalidos("No se pudo identificar al socio");
    }
    Socio socioActual = repositorioSocio.buscarPorUsuario(socio.getUsuario().getId());
    if (socioActual == null) {
      throw new DatosDeRegistroInvalidos("No se pudo identificar al socio");
    }
    return socioActual;
  }

  private void validarMascota(Mascota mascota) throws DatosDeRegistroInvalidos {
    validarDatosObligatorios(mascota);
    validarPeso(mascota.getPeso());
    validarFechaNacimiento(mascota.getFechaNacimiento());
  }

  private void validarDatosObligatorios(Mascota mascota) throws DatosDeRegistroInvalidos {
    if (mascota == null || algunoVacio(mascota.getNombre()) || mascota.getEspecie() == null) {
      throw new DatosDeRegistroInvalidos("El nombre y la especie de la mascota son obligatorios");
    }
  }

  private void validarPeso(Double peso) throws DatosDeRegistroInvalidos {
    if (peso != null && peso <= 0) {
      throw new DatosDeRegistroInvalidos("El peso de la mascota debe ser mayor a cero");
    }
  }

  private void validarFechaNacimiento(LocalDate fechaNacimiento) throws DatosDeRegistroInvalidos {
    if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
      throw new DatosDeRegistroInvalidos("La fecha de nacimiento no puede ser futura");
    }
  }

  private boolean algunoVacio(String... valores) {
    for (String valor : valores) {
      if (valor == null || valor.isBlank()) {
        return true;
      }
    }
    return false;
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
