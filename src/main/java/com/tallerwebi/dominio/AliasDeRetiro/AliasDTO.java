package com.tallerwebi.dominio.AliasDeRetiro;

import java.util.List;

public class AliasDTO {

  private List<String> colores;
  private List<String> animales;
  private List<String> objetos;

  public List<String> getColores() {
    return colores;
  }

  public void setColores(List<String> colores) {
    this.colores = colores;
  }

  public List<String> getAnimales() {
    return animales;
  }

  public void setAnimales(List<String> animales) {
    this.animales = animales;
  }

  public List<String> getObjetos() {
    return objetos;
  }

  public void setObjetos(List<String> objetos) {
    this.objetos = objetos;
  }
}
