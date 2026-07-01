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

  private final ServicioPalabrasAlias servicioPalabrasAlias;

  private RepositorioHijo repositorioHijo;

  public ServicioGeneradorAliasImpl(
    ServicioPalabrasAlias servicioPalabrasAlias,
    RepositorioHijo repositorioHijo
  ) {
    this.servicioPalabrasAlias = servicioPalabrasAlias;
    this.repositorioHijo = repositorioHijo;
  }

  @Override
  public List<String> obtenerTodasLasCombinaciones() {
    List<String> aliases = new ArrayList<>();

    for (String color : servicioPalabrasAlias.obtenerColores()) {
      for (String animal : servicioPalabrasAlias.obtenerAnimales()) {
        for (String objeto : servicioPalabrasAlias.obtenerObjetos()) {
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
