package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

public class ServicioGeminiTest {

  private ServicioGeminiImpl servicioGemini;
  private RestTemplate restTemplateMock;
  private static final String PREGUNTA = "¿Hola?";
  private static final String RESPUESTA_ESPERADA = "Respuesta Gemini";
  private static final String JSON_RESPONSE =
    "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + RESPUESTA_ESPERADA + "\"}]}}]}";

  @BeforeEach
  public void init() {
    this.restTemplateMock = mock(RestTemplate.class);
    this.servicioGemini = new ServicioGeminiImpl(restTemplateMock);
  }

  @Test
  public void preguntarDeberiaRetornarRespuestaDesdeApi() throws JsonProcessingException {
    this.dadoQueLaApiResponde(JSON_RESPONSE);

    String respuesta = this.cuandoPregunto(PREGUNTA, null, false);

    this.entoncesLaRespuestaEs(respuesta, RESPUESTA_ESPERADA);
  }

  @Test
  public void preguntarDeberiaRetornarMensajeDeErrorSiElJsonEsInvalido()
    throws JsonProcessingException {
    String jsonInvalido = "{\"error\": \"formato incorrecto\"}";
    this.dadoQueLaApiResponde(jsonInvalido);

    String respuesta = this.cuandoPregunto(PREGUNTA, null, false);

    assertThat(respuesta, startsWith("Error procesando respuesta de Gemini:"));
  }

  @Test
  public void configurarDeberiaEstablecerElContextoYUsarloEnPreguntar()
    throws JsonProcessingException {
    String contexto = "Eres un experto en Java";
    servicioGemini.configurar(contexto);
    this.dadoQueLaApiResponde(JSON_RESPONSE);

    String respuesta = this.cuandoPregunto(PREGUNTA, null, false);

    this.entoncesLaRespuestaEs(respuesta, RESPUESTA_ESPERADA);
    this.entoncesLaPeticionContieneElContexto(contexto);
  }

  @Test
  public void preguntarConContextoAdicionalDeberiaCombinarContextos()
    throws JsonProcessingException {
    servicioGemini.setSystemInstruction("Base");
    this.dadoQueLaApiResponde(JSON_RESPONSE);

    String respuesta = this.cuandoPregunto(PREGUNTA, "Adicional", true);

    this.entoncesLaRespuestaEs(respuesta, RESPUESTA_ESPERADA);
    this.entoncesLaPeticionContieneElContexto("Base. Adicional");
  }

  @Test
  public void limpiarContextoDeberiaVaciarLasInstrucciones() {
    this.dadoQueElContextoEstaConfigurado("Contexto persistente");

    this.cuandoLimpioElContexto();

    this.entoncesElContextoEstaVacio();
  }

  private void dadoQueElContextoEstaConfigurado(String contexto) {
    this.servicioGemini.setSystemInstruction(contexto);
  }

  private void cuandoLimpioElContexto() {
    this.servicioGemini.limpiarContexto();
  }

  private void entoncesElContextoEstaVacio() {
    assertThat(this.servicioGemini.getSystemInstructions(), equalTo(""));
  }

  private void dadoQueLaApiResponde(String json) {
    when(this.restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);
  }

  private String cuandoPregunto(String mensaje, String contexto, boolean persistir)
    throws JsonProcessingException {
    return servicioGemini.preguntar(mensaje, contexto, persistir);
  }

  private void entoncesLaRespuestaEs(String respuestaActual, String respuestaEsperada) {
    assertThat(respuestaActual, equalTo(respuestaEsperada));
  }

  private void entoncesLaPeticionContieneElContexto(String contexto) {
    verify(restTemplateMock)
      .postForObject(
        anyString(),
        argThat((HttpEntity<String> entity) -> {
          String body = entity.getBody();
          return body != null && body.contains("system_instruction") && body.contains(contexto);
        }),
        eq(String.class)
      );
  }
}
