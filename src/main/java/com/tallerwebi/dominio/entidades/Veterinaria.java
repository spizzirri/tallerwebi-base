package com.tallerwebi.dominio.entidades;

import java.util.List;

public class Veterinaria {

  private Integer id;
  private String nombre;
  private String direccion;
  private String telefono;
  private boolean abiertoHoy;
  private List<Especialidad> especialidades;

  public Veterinaria(
    Integer id,
    String nombre,
    String direccion,
    String telefono,
    boolean abiertoHoy,
    List<Especialidad> especialidades
  ) {
    this.id = id;
    this.nombre = nombre;
    this.direccion = direccion;
    this.telefono = telefono;
    this.abiertoHoy = abiertoHoy;
    this.especialidades = especialidades;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDireccion() {
    return direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public boolean getAbiertoHoy() {
    return abiertoHoy;
  }

  public List<Especialidad> getEspecialidades() {
    return especialidades;
  }

  public Integer getId() {
    return id;
  }
}
