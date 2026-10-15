package com.tallerwebi.dominio;

import java.util.List;

public class ResumenMascotasParaAsistente {

  private ResumenMascotasParaAsistente() {}

  public static String formatear(Socio socio) {
    if (socio == null) {
      return "El socio no tiene mascotas registradas.";
    }

    List<Mascota> mascotas = socio.getMascotas();

    if (mascotas.isEmpty()) {
      return "El socio no tiene mascotas registradas.";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Mascotas del socio datos reales, no inventar otras):\n");

    for (Mascota m : mascotas) {
      sb.append("- id=").append(m.getId());
      sb.append(" | nombre=").append(m.getNombre());
      sb.append(" | especie=").append(m.getEspecie());
      if (m.getRaza() != null && !m.getRaza().isBlank()) {
        sb.append(" | raza=").append(m.getRaza());
      }
      sb.append("\n");
    }

    return sb.toString().trim();
  }
}
