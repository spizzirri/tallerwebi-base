package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
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

  @Column(unique = true)
  private String cuit;

  private LocalTime horaApertura;
  private LocalTime horaCierre;

  @OneToOne(cascade = CascadeType.ALL)
  private Usuario usuario;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "Veterinaria_Dia_Atencion",
    joinColumns = @JoinColumn(name = "veterinaria_id")
  )
  @Enumerated(EnumType.STRING)
  private Set<DayOfWeek> diasAtencion = new LinkedHashSet<>(
    EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
  );

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
    return fecha != null && diasAtencionConfigurados().contains(fecha.getDayOfWeek());
  }

  public List<LocalTime> horariosDeAtencion() {
    List<LocalTime> horarios = new ArrayList<>();
    if (horaApertura == null || horaCierre == null || !horaApertura.isBefore(horaCierre)) {
      return horarios;
    }
    for (
      LocalTime hora = horaApertura;
      !hora.plusMinutes(MINUTOS_POR_TURNO).isAfter(horaCierre);
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

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Set<DayOfWeek> getDiasAtencion() {
    return diasAtencion;
  }

  public void setDiasAtencion(Set<DayOfWeek> diasAtencion) {
    this.diasAtencion =
      diasAtencion == null ? new LinkedHashSet<>() : new LinkedHashSet<>(diasAtencion);
  }

  public List<DayOfWeek> getDiasAtencionOrdenados() {
    return diasAtencionConfigurados()
      .stream()
      .sorted(Comparator.comparingInt(DayOfWeek::getValue))
      .toList();
  }

  public String getDiasAtencionTexto() {
    return getDiasAtencionOrdenados()
      .stream()
      .map(Veterinaria::nombreDia)
      .collect(java.util.stream.Collectors.joining(", "));
  }

  private Set<DayOfWeek> diasAtencionConfigurados() {
    if (diasAtencion == null || diasAtencion.isEmpty()) {
      return EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    }
    return diasAtencion;
  }

  private static String nombreDia(DayOfWeek dia) {
    return switch (dia) {
      case MONDAY -> "Lunes";
      case TUESDAY -> "Martes";
      case WEDNESDAY -> "Miércoles";
      case THURSDAY -> "Jueves";
      case FRIDAY -> "Viernes";
      case SATURDAY -> "Sábado";
      case SUNDAY -> "Domingo";
    };
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
