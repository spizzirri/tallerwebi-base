package com.tallerwebi.presentacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.ServicioGemini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class ControladorGemini {

  @Autowired
  private ServicioGemini servicioGemini;

  @PostMapping("/preguntar")
  public ResponseEntity<?> preguntar(@RequestBody GeminiDto geminiDto) {
    try {
      boolean persistir =
        geminiDto.getReglaAdicional() != null && !geminiDto.getReglaAdicional().isEmpty();
      String respuesta = servicioGemini.preguntar(
        geminiDto.getPregunta(),
        geminiDto.getReglaAdicional(),
        persistir
      );
      geminiDto.setRespuesta(respuesta);
      geminiDto.setContextoActual(servicioGemini.getSystemInstructions());
      return ResponseEntity.ok(geminiDto);
    } catch (JsonProcessingException exception) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    } catch (Exception e) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al consultar Gemini: " + e.getMessage());
    }
  }

  @PostMapping("/limpiar")
  public ResponseEntity<Void> limpiarContexto() {
    this.servicioGemini.limpiarContexto();
    return ResponseEntity.ok().build();
  }
}
