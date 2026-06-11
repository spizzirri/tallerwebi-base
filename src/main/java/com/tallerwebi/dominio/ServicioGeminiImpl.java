package com.tallerwebi.dominio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioGeminiImpl implements ServicioGemini {

  @Value("${GEMINI_API_KEY:default}")
  private String apiKey;

  private final RestTemplate restTemplate;

  private String systemInstructions = "";

  // Modelos disponibles:
  //
  // Nivel Gratuito (Limitado por cuotas, mejora la IA con tus datos):
  // - gemini-3-flash: Recomendado, mejor equilibrio actual.
  // - gemini-2.5-flash: Estable y rápido.
  // - gemini-2.5-flash-lite: Máxima velocidad, mayores límites de peticiones.
  //
  // Nivel De Pago (Pay-as-you-go, datos privados):
  // - gemini-3.1-pro: Razonamiento complejo y tareas avanzadas.
  // - gemini-2.5-pro: Tareas de análisis y razonamiento avanzado.
  //
  // Métodos disponibles (usar después de los dos puntos en la URL):
  // - generateContent: Generación síncrona (espera la respuesta completa)
  // - streamGenerateContent: Generación en tiempo real (streaming, ideal para UI)
  // - countTokens: Verifica el costo/uso de tokens antes de procesar
  // - embedContent: Convierte texto en vectores (para búsqueda semántica)
  private static final String URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

  @Autowired
  public ServicioGeminiImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public void configurar(String systemInstructions) {
    this.systemInstructions = systemInstructions;
  }

  @Override
  public void setSystemInstruction(String instruction) {
    this.systemInstructions = instruction;
  }

  @Override
  public void appendSystemInstruction(String instruction) {
    if (this.systemInstructions == null || this.systemInstructions.isEmpty()) {
      this.systemInstructions = instruction;
    } else {
      this.systemInstructions += ". " + instruction;
    }
  }

  @Override
  public String getSystemInstructions() {
    return this.systemInstructions;
  }

  @Override
  public void limpiarContexto() {
    this.systemInstructions = "";
  }

  @Override
  public String preguntar(String mensajeUsuario, String reglaAdicional, boolean persistir)
    throws JsonProcessingException {
    if (reglaAdicional != null && !reglaAdicional.isEmpty()) {
      if (persistir) {
        appendSystemInstruction(reglaAdicional);
      } else {
        return ejecutarConContexto(
          mensajeUsuario,
          (this.systemInstructions.isEmpty())
            ? reglaAdicional
            : this.systemInstructions + ". " + reglaAdicional
        );
      }
    }
    return ejecutarConContexto(mensajeUsuario, this.systemInstructions);
  }

  private String ejecutarConContexto(String mensaje, String contexto)
    throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-goog-api-key", this.apiKey);

    Map<String, Object> body = new HashMap<>();

    if (contexto != null && !contexto.isEmpty()) {
      Map<String, Object> systemInstructionPart = new HashMap<>();
      systemInstructionPart.put("parts", List.of(Map.of("text", contexto)));
      body.put("system_instruction", systemInstructionPart);
    }

    Map<String, Object> contents = new HashMap<>();
    Map<String, String> part = new HashMap<>();
    part.put("text", mensaje);
    contents.put("parts", List.of(part));
    body.put("contents", List.of(contents));

    ObjectMapper mapper = new ObjectMapper();
    String requestBody = mapper.writeValueAsString(body);

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

    String response = restTemplate.postForObject(URL, request, String.class);

    return extraerRespuesta(response);
  }

  private String extraerRespuesta(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);
      return root
        .path("candidates")
        .get(0)
        .path("content")
        .path("parts")
        .get(0)
        .path("text")
        .asText();
    } catch (Exception e) {
      return "Error procesando respuesta de Gemini: " + e.getMessage();
    }
  }
}
