package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Turno {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate fecha;
  private LocalTime hora;

  @Enumerated(EnumType.STRING)
  private EstadoTurno estado = EstadoTurno.CONFIRMADO;

  private String observaciones;

  @ManyToOne
  private Mascota mascota;

  @ManyToOne
  private Veterinaria veterinaria;

  @ManyToOne
  private Servicio servicio;

  public boolean estaConfirmado() {
    return estado == EstadoTurno.CONFIRMADO;
  }

  public void cancelar() {
    estado = EstadoTurno.CANCELADO;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public EstadoTurno getEstado() {
    return estado;
  }

  public void setEstado(EstadoTurno estado) {
    this.estado = estado;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Mascota getMascota() {
    return mascota;
  }

  public void setMascota(Mascota mascota) {
    this.mascota = mascota;
  }

  public Veterinaria getVeterinaria() {
    return veterinaria;
  }

  public void setVeterinaria(Veterinaria veterinaria) {
    this.veterinaria = veterinaria;
  }

  public Servicio getServicio() {
    return servicio;
  }

  public void setServicio(Servicio servicio) {
    this.servicio = servicio;
  }
}
