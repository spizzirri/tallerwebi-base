package com.tallerwebi.integracion;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.integracion.config.TestRestClientConfig;
import com.tallerwebi.presentacion.GeminiDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, TestRestClientConfig.class })
@TestPropertySource(properties = "GEMINI_API_KEY=" + ControladorGeminiIntegrationTest.API_KEY)
public class ControladorGeminiIntegrationTest {

  static final String API_KEY = "key-de-prueba";

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private MockRestServiceServer servidorGemini;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    this.servidorGemini.reset();
  }

  @Test
  public void alConsultarDeberiaEnviarApiKeyInyectadaEnHeaders() throws Exception {
    this.dadoQueLaApiRespondeSiRecibeLaApiKey(API_KEY);

    this.cuandoConsultoAlControlador("Hola, ¿qué tal?");

    this.entoncesSeEnvioLaPeticionEsperada();
  }

  private void dadoQueLaApiRespondeSiRecibeLaApiKey(String apiKey) {
    String jsonResponse = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Respuesta\"}]}}]}";
    this.servidorGemini.expect(method(HttpMethod.POST))
      .andExpect(header("X-goog-api-key", apiKey))
      .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
  }

  private void cuandoConsultoAlControlador(String pregunta) throws Exception {
    GeminiDto dto = new GeminiDto();
    dto.setPregunta(pregunta);

    ObjectMapper mapper = new ObjectMapper();

    this.mockMvc.perform(
        post("/api/gemini/preguntar")
          .contentType(MediaType.APPLICATION_JSON)
          .content(mapper.writeValueAsString(dto))
      )
      .andExpect(status().isOk());
  }

  private void entoncesSeEnvioLaPeticionEsperada() {
    this.servidorGemini.verify();
  }
}
