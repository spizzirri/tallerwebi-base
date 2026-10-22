package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class ServicioGeminiTest {

  private ServicioGeminiImpl servicioGemini;
  private MockRestServiceServer servidorGemini;
  private static final String PREGUNTA = "¿Hola?";
  private static final String RESPUESTA_ESPERADA = "Respuesta Gemini";
  private static final String JSON_RESPONSE =
    "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + RESPUESTA_ESPERADA + "\"}]}}]}";

  @BeforeEach
  public void init() {
    RestClient.Builder restClientBuilder = RestClient.builder();
    this.servidorGemini = MockRestServiceServer.bindTo(restClientBuilder).build();
    this.servicioGemini = new ServicioGeminiImpl(restClientBuilder.build());
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
    this.dadoQueLaApiRespondeSiLaPeticionContieneElContexto(contexto, JSON_RESPONSE);

    String respuesta = this.cuandoPregunto(PREGUNTA, null, false);

    this.entoncesLaRespuestaEs(respuesta, RESPUESTA_ESPERADA);
    this.entoncesSeEnvioLaPeticionEsperada();
  }

  @Test
  public void preguntarConContextoAdicionalDeberiaCombinarContextos()
    throws JsonProcessingException {
    servicioGemini.setSystemInstruction("Base");
    this.dadoQueLaApiRespondeSiLaPeticionContieneElContexto("Base. Adicional", JSON_RESPONSE);

    String respuesta = this.cuandoPregunto(PREGUNTA, "Adicional", true);

    this.entoncesLaRespuestaEs(respuesta, RESPUESTA_ESPERADA);
    this.entoncesSeEnvioLaPeticionEsperada();
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
    this.servidorGemini.expect(method(HttpMethod.POST))
      .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
  }

  private void dadoQueLaApiRespondeSiLaPeticionContieneElContexto(String contexto, String json) {
    this.servidorGemini.expect(method(HttpMethod.POST))
      .andExpect(
        content().string(allOf(containsString("system_instruction"), containsString(contexto)))
      )
      .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
  }

  private String cuandoPregunto(String mensaje, String contexto, boolean persistir)
    throws JsonProcessingException {
    return servicioGemini.preguntar(mensaje, contexto, persistir);
  }

  private void entoncesLaRespuestaEs(String respuestaActual, String respuestaEsperada) {
    assertThat(respuestaActual, equalTo(respuestaEsperada));
  }

  private void entoncesSeEnvioLaPeticionEsperada() {
    this.servidorGemini.verify();
  }
}
