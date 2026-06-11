package com.tallerwebi.dominio;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ServicioGemini {
  String preguntar(String mensajeUsuario, String reglaAdicional, boolean persistir)
    throws JsonProcessingException;
  void configurar(String systemInstructions);
  void setSystemInstruction(String instruction);
  void appendSystemInstruction(String instruction);
  String getSystemInstructions();
  void limpiarContexto();
}
