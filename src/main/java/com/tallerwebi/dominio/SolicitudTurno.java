package com.tallerwebi.dominio;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

/** Lo que elige el socio al pedir un turno. También es el objeto del formulario. */
public class SolicitudTurno {

  private Long mascotaId;
  private Long veterinariaId;
  private Long servicioId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fecha;

  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime hora;

  private String observaciones;

  public Long getMascotaId() {
    return mascotaId;
  }

  public void setMascotaId(Long mascotaId) {
    this.mascotaId = mascotaId;
  }

  public Long getVeterinariaId() {
    return veterinariaId;
  }

  public void setVeterinariaId(Long veterinariaId) {
    this.veterinariaId = veterinariaId;
  }

  public Long getServicioId() {
    return servicioId;
  }

  public void setServicioId(Long servicioId) {
    this.servicioId = servicioId;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public LocalTime getHora() {
    return hora;
  }

  public void setHora(LocalTime hora) {
    this.hora = hora;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}
