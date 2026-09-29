package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Veterinaria {

  public static final int MINUTOS_POR_TURNO = 30;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String direccion;
  private String telefono;
  private String cuit;

  // Por ahora todas atienden de lunes a viernes en una misma franja horaria.
  private LocalTime horaApertura;
  private LocalTime horaCierre;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "Veterinaria_Servicio",
    joinColumns = @JoinColumn(name = "veterinaria_id"),
    inverseJoinColumns = @JoinColumn(name = "servicio_id")
  )
  @OrderBy("nombre")
  private Set<Servicio> servicios = new LinkedHashSet<>();

  public boolean ofrece(Servicio servicio) {
    return servicios.stream().anyMatch(s -> s.getId().equals(servicio.getId()));
  }

  public boolean atiendeEl(LocalDate fecha) {
    DayOfWeek dia = fecha.getDayOfWeek();
    return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
  }

  public List<LocalTime> horariosDeAtencion() {
    List<LocalTime> horarios = new ArrayList<>();
    for (
      LocalTime hora = horaApertura;
      hora.isBefore(horaCierre);
      hora = hora.plusMinutes(MINUTOS_POR_TURNO)
    ) {
      horarios.add(hora);
    }
    return horarios;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getCuit() {
    return cuit;
  }

  public void setCuit(String cuit) {
    this.cuit = cuit;
  }

  public LocalTime getHoraApertura() {
    return horaApertura;
  }

  public void setHoraApertura(LocalTime horaApertura) {
    this.horaApertura = horaApertura;
  }

  public LocalTime getHoraCierre() {
    return horaCierre;
  }

  public void setHoraCierre(LocalTime horaCierre) {
    this.horaCierre = horaCierre;
  }

  public Set<Servicio> getServicios() {
    return servicios;
  }
}
