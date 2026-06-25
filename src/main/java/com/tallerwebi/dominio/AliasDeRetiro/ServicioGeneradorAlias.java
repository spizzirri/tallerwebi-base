package com.tallerwebi.dominio.AliasDeRetiro;

import java.util.List;

public interface ServicioGeneradorAlias {
  List<String> obtenerTodasLasCombinaciones();

  String generarAliasDisponible();
}
