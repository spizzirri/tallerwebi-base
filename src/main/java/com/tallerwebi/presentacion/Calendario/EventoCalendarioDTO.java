package com.tallerwebi.presentacion.Calendario;

public class EventoCalendarioDTO {

  private String titulo;

  private String fecha;

  private String estado;
  private String claseCss;
  private Long pedidoId;

  //getter y setter

  public Long getPedidoId() {
    return pedidoId;
  }

  public void setPedidoId(Long pedidoId) {
    this.pedidoId = pedidoId;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public String getClaseCss() {
    return claseCss;
  }

  public void setClaseCss(String claseCss) {
    this.claseCss = claseCss;
  }

  public String getFecha() {
    return fecha;
  }

  public void setFecha(String fecha) {
    this.fecha = fecha;
  }
}
