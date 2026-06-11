package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHome extends VistaWeb {

  public VistaHome(Page page) {
    super(page);
  }

  public void agregarProductoAlCarrito() {
    this.darClickEnElElemento(".btn-add-product");
  }
}
