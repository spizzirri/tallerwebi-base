package com.tallerwebi.infraestructura;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.tallerwebi.dominio.MensajeConversacion;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Componente gestionado por Spring (Bean Singleton).
 * Permite que Spring administre su ciclo de vida y lo inyecte
 * mediante dependencias en servicios o controladores sin hacer "new".
 */
@Component
public class ClienteGemini {

  /* Modelo de gemini usado */
  private static final String MODELO = "gemini-3.5-flash-lite";

  /* Prompt */
  private static final String INSTRUCCION =
    """
    Sos un orientador veterinario de primera consulta en una app de socios con mascotas.
    Respondés en español, claro y breve.
    No diagnosticás ni recetás. Ante signos graves (convulsiones, dificultad para respirar,
    sangrado abundante, intoxicación) indicá acudir de inmediato a una veterinaria de guardia.
    Recordá que esto no reemplaza una consulta presencial.

    Cuando recibas un bloque [CONTEXTO] con mascotas, usá solo esa lista para responder
    sobre mascotas del usuario. Si preguntan por mascotas y la lista está vacía, decílo.
    No inventes nombres ni especies.
    """;

  private final Client client;
  private final GenerateContentConfig config;

  /* constructor */
  public ClienteGemini() {
    String apiKey = obtenerApiKeyPrivada();
    this.client = Client.builder().apiKey(apiKey).build();
    this.config =
      GenerateContentConfig
        .builder()
        .systemInstruction(Content.fromParts(Part.fromText(INSTRUCCION)))
        .maxOutputTokens(1024)
        .build();
  }

  public String completar(List<MensajeConversacion> historial, String mensajeUsuario) {
    List<Content> contents = new ArrayList<>();
    for (MensajeConversacion m : historial) {
      contents.add(
        Content.builder().role(m.getRol()).parts(List.of(Part.fromText(m.getTexto()))).build()
      );
    }
    contents.add(
      Content.builder().role("user").parts(List.of(Part.fromText(mensajeUsuario))).build()
    );
    GenerateContentResponse response = client.models.generateContent(MODELO, contents, config);
    return response.text();
  }

  private static String obtenerApiKeyPrivada() {
    String key = System.getenv("GEMINI_API_KEY");

    if (key == null || key.isBlank()) {
      throw new IllegalStateException("Definí GEMINI_API_KEY en el entorno");
    }
    return key;
  }
}
