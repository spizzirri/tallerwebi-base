package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import java.time.LocalDate;

final class ValidadorMascota {

  private ValidadorMascota() {}

  static void validar(Mascota mascota, String mensajeDatosObligatorios)
    throws DatosDeRegistroInvalidos {
    if (mascota == null || algunoVacio(mascota.getNombre()) || mascota.getEspecie() == null) {
      throw new DatosDeRegistroInvalidos(mensajeDatosObligatorios);
    }
    validarPeso(mascota.getPeso());
    validarFechaNacimiento(mascota.getFechaNacimiento());
  }

  private static void validarPeso(Double peso) throws DatosDeRegistroInvalidos {
    if (peso != null && peso <= 0) {
      throw new DatosDeRegistroInvalidos("El peso de la mascota debe ser mayor a cero");
    }
  }

  private static void validarFechaNacimiento(LocalDate fechaNacimiento)
    throws DatosDeRegistroInvalidos {
    if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
      throw new DatosDeRegistroInvalidos("La fecha de nacimiento no puede ser futura");
    }
  }

  private static boolean algunoVacio(String... valores) {
    for (String valor : valores) {
      if (valor == null || valor.isBlank()) {
        return true;
      }
    }
    return false;
  }
}
