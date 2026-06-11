package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.ServicioGemini;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

public class ControladorGeminiTest {

  private ControladorGemini controladorGemini;
  private ServicioGemini servicioGeminiMock;
  private static final String PREGUNTA = "Hola!";

  @BeforeEach
  public void init() {
    servicioGeminiMock = mock(ServicioGemini.class);
    controladorGemini = new ControladorGemini();
    ReflectionTestUtils.setField(controladorGemini, "servicioGemini", servicioGeminiMock);
  }

  @Test
  public void preguntarDeberiaRetornarRespuestaExitosa() throws JsonProcessingException {
    GeminiDto dto = this.dadoQueTengoUnDto(PREGUNTA);
    this.dadoQueElServicioResponde("Hola, soy Gemini");

    ResponseEntity<?> response = this.cuandoLePreguntoAlServicio(dto);

    this.entoncesLaRespuestaEsExitosa(response);
  }

  @Test
  public void preguntarDeberiaRetornarBadRequestSiFallaJson() throws JsonProcessingException {
    GeminiDto dto = this.dadoQueTengoUnDto(PREGUNTA);
    this.dadoQueElServicioLanzaUnaJsonProcessingException();

    ResponseEntity<?> response = this.cuandoLePreguntoAlServicio(dto);

    this.entoncesLaRespuestaTieneStatusCode(response, HttpStatus.BAD_REQUEST);
  }

  @Test
  public void preguntarDeberiaRetornarError500SiOcurreExcepcionGenerica()
    throws JsonProcessingException {
    GeminiDto dto = this.dadoQueTengoUnDto(PREGUNTA);
    this.dadoQueElServicioLanzaUnaRuntimeException("Error inesperado");

    ResponseEntity<?> response = this.cuandoLePreguntoAlServicio(dto);

    this.entoncesObtengoUnaRespuestaDeError500(response);
  }

  @Test
  public void limpiarContextoDeberiaRetornar200() {
    ResponseEntity<Void> response = this.cuandoLimpioElContexto();

    this.entoncesLaRespuestaTieneStatusCode(response, HttpStatus.OK);
    this.entoncesSeInvocoElLimpiarContexto();
  }

  private void entoncesSeInvocoElLimpiarContexto() {
    verify(servicioGeminiMock, times(1)).limpiarContexto();
  }

  private ResponseEntity<Void> cuandoLimpioElContexto() {
    return controladorGemini.limpiarContexto();
  }

  private GeminiDto dadoQueTengoUnDto(String pregunta) {
    GeminiDto dto = new GeminiDto();
    dto.setPregunta(pregunta);
    return dto;
  }

  private void dadoQueElServicioResponde(String respuesta) throws JsonProcessingException {
    when(servicioGeminiMock.preguntar(eq(PREGUNTA), any(), anyBoolean())).thenReturn(respuesta);
  }

  private void dadoQueElServicioLanzaUnaJsonProcessingException() throws JsonProcessingException {
    when(this.servicioGeminiMock.preguntar(eq(PREGUNTA), any(), anyBoolean()))
      .thenThrow(JsonProcessingException.class);
  }

  private void dadoQueElServicioLanzaUnaRuntimeException(String mensaje)
    throws JsonProcessingException {
    when(this.servicioGeminiMock.preguntar(eq(PREGUNTA), any(), anyBoolean()))
      .thenThrow(new RuntimeException(mensaje));
  }

  private ResponseEntity<?> cuandoLePreguntoAlServicio(GeminiDto dto) {
    return controladorGemini.preguntar(dto);
  }

  private void entoncesLaRespuestaEsExitosa(ResponseEntity<?> response) {
    this.entoncesLaRespuestaTieneStatusCode(response, HttpStatus.OK);
    assertThat(((GeminiDto) response.getBody()).getRespuesta(), equalTo("Hola, soy Gemini"));
  }

  private void entoncesLaRespuestaTieneStatusCode(
    ResponseEntity<?> response,
    HttpStatus statusCode
  ) {
    assertThat(response.getStatusCodeValue(), equalTo(statusCode.value()));
  }

  private void entoncesObtengoUnaRespuestaDeError500(ResponseEntity<?> response) {
    this.entoncesLaRespuestaTieneStatusCode(response, HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(
      response.getBody().toString(),
      equalTo("Error al consultar Gemini: Error inesperado")
    );
  }
}
