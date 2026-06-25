package com.tallerwebi.dominio.AliasDeRetiro;

import com.tallerwebi.dominio.Hijos.RepositorioHijo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service("ServicioGeneradorAlias")
@Transactional
public class ServicioGeneradorAliasImpl implements ServicioGeneradorAlias {

  private static final String[] COLORES = { "ROJO", "AZUL", "VERDE", "AMARILLO", "VIOLETA" };

  private static final String[] ANIMALES = { "GATO", "PERRO", "CONEJO", "TIGRE", "PANDA" };

  private static final String[] OBJETOS = { "COMETA", "LAPIZ", "PELOTA", "BICICLETA", "TREN" };

  private RepositorioHijo repositorioHijo;

  public ServicioGeneradorAliasImpl(RepositorioHijo repositorioHijo) {
    this.repositorioHijo = repositorioHijo;
  }

  @Override
  public List<String> obtenerTodasLasCombinaciones() {
    List<String> aliases = new ArrayList<>();

    for (String color : COLORES) {
      for (String animal : ANIMALES) {
        for (String objeto : OBJETOS) {
          aliases.add(color + "." + animal + "." + objeto);
        }
      }
    }

    return aliases;
  }

  @Override
  public String generarAliasDisponible() {
    List<String> todasLasCombinaciones = obtenerTodasLasCombinaciones();

    Collections.shuffle(todasLasCombinaciones);

    for (String alias : todasLasCombinaciones) {
      if (!repositorioHijo.existeAlias(alias)) {
        return alias;
      }
    }

    return null;
  }
}
