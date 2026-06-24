package com.tallerwebi.dominio.AliasDeRetiro;

import java.util.List;

public interface ServicioGeneradorAlias {
  //  String generarAlias();

  List<String> obtenerTodasLasCombinaciones();

  String generarAliasDisponible();
}
