package com.tallerwebi.dominio;

public class Plan {

  private Long id;
  private String nombre;
  private Integer nivel;
  private String descripcion;
  private Double precioMensual;

  public Plan(Long id, String nombre, Integer nivel, String descripcion, Double precioMensual) {
    this.id = id;
    this.nombre = nombre;
    this.nivel = nivel;
    this.descripcion = descripcion;
    this.precioMensual = precioMensual;
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public Integer getNivel() {
    return nivel;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public Double getPrecioMensual() {
    return precioMensual;
  }
}
