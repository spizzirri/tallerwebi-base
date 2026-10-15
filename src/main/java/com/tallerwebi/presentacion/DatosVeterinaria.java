package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.Veterinaria;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

public class DatosVeterinaria {

  private String nombre;
  private String cuit;
  private String direccion;
  private String telefono;
  private String email;
  private String password;

  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime horaApertura;

  @DateTimeFormat(pattern = "HH:mm")
  private LocalTime horaCierre;

  private Set<DayOfWeek> diasAtencion = EnumSet.noneOf(DayOfWeek.class);
  private List<Long> serviciosIds = new ArrayList<>();

  public static DatosVeterinaria desde(Veterinaria veterinaria) {
    DatosVeterinaria datos = new DatosVeterinaria();
    datos.setNombre(veterinaria.getNombre());
    datos.setCuit(veterinaria.getCuit());
    datos.setDireccion(veterinaria.getDireccion());
    datos.setTelefono(veterinaria.getTelefono());
    datos.setEmail(veterinaria.getUsuario().getEmail());
    datos.setHoraApertura(veterinaria.getHoraApertura());
    datos.setHoraCierre(veterinaria.getHoraCierre());
    Set<DayOfWeek> dias = veterinaria.getDiasAtencion();
    datos.setDiasAtencion(
      dias == null || dias.isEmpty() ? EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY) : dias
    );
    datos.setServiciosIds(
      veterinaria
        .getServicios()
        .stream()
        .map(servicio -> servicio.getId())
        .collect(Collectors.toList())
    );
    return datos;
  }

  public Veterinaria crearVeterinaria() {
    Veterinaria veterinaria = new Veterinaria();
    aplicarDatos(veterinaria);
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword(password);
    veterinaria.setUsuario(usuario);
    return veterinaria;
  }

  public Veterinaria crearDatosPerfil() {
    Veterinaria veterinaria = new Veterinaria();
    aplicarDatos(veterinaria);
    return veterinaria;
  }

  private void aplicarDatos(Veterinaria veterinaria) {
    veterinaria.setNombre(nombre);
    veterinaria.setCuit(cuit);
    veterinaria.setDireccion(direccion);
    veterinaria.setTelefono(telefono);
    veterinaria.setHoraApertura(horaApertura);
    veterinaria.setHoraCierre(horaCierre);
    veterinaria.setDiasAtencion(diasAtencion);
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getCuit() {
    return cuit;
  }

  public void setCuit(String cuit) {
    this.cuit = cuit;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  public Set<DayOfWeek> getDiasAtencion() {
    return diasAtencion;
  }

  public void setDiasAtencion(Set<DayOfWeek> diasAtencion) {
    this.diasAtencion =
      diasAtencion == null || diasAtencion.isEmpty()
        ? EnumSet.noneOf(DayOfWeek.class)
        : EnumSet.copyOf(diasAtencion);
  }

  public List<Long> getServiciosIds() {
    return serviciosIds;
  }

  public void setServiciosIds(List<Long> serviciosIds) {
    this.serviciosIds = serviciosIds == null ? new ArrayList<>() : new ArrayList<>(serviciosIds);
  }

  public static String nombreDia(DayOfWeek dia) {
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
}
