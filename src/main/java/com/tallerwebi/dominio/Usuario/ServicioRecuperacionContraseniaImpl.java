package com.tallerwebi.dominio.Usuario;

import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class ServicioRecuperacionContraseniaImpl implements ServicioRecuperacionContrasenia {

  @Override
  public String generarCodigo() {
    int codigo = 100000 + new Random().nextInt(900000);
    return String.valueOf(codigo);
  }
}
