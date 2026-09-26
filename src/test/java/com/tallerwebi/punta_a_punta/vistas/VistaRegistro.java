package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaRegistro extends VistaWeb {

  public VistaRegistro(Page page) {
    super(page);
  }

  public void escribirDatosDelSocio(String nombre, String apellido, String dni) {
    this.escribirEnElElemento("#nombre", nombre);
    this.escribirEnElElemento("#apellido", apellido);
    this.escribirEnElElemento("#dni", dni);
  }

  public void escribirEMAIL(String email) {
    this.escribirEnElElemento("#email", email);
  }

  public void escribirClave(String clave) {
    this.escribirEnElElemento("#password", clave);
  }

  public void escribirDatosDeLaMascota(String nombre, String especie) {
    this.escribirEnElElemento("#nombreMascota", nombre);
    this.page.selectOption("#especie", especie);
  }

  public void elegirPlan(String nombrePlan) {
    this.page.locator("#plan")
      .selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(nombrePlan));
  }

  public void darClickEnRegistrarme() {
    this.darClickEnElElemento("#btn-registrarme");
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("p.alert.alert-danger");
  }
}
