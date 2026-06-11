package com.tallerwebi.integracion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.presentacion.GeminiDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(
  classes = {
    SpringWebTestConfig.class, com.tallerwebi.integracion.config.TestRestTemplateConfig.class,
  }
)
@TestPropertySource(properties = "GEMINI_API_KEY=key-de-prueba")
public class ControladorGeminiIntegrationTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private RestTemplate restTemplate;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    reset(this.restTemplate);
  }

  @Test
  public void alConsultarDeberiaEnviarApiKeyInyectadaEnHeaders() throws Exception {
    this.dadoQueLaApiRespondeConExito();

    this.cuandoConsultoAlControlador("Hola, ¿qué tal?");

    this.entoncesLaPeticionContieneLaApiKey("test-api-key");
  }

  private void dadoQueLaApiRespondeConExito() {
    String jsonResponse = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Respuesta\"}]}}]}";
    when(this.restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(jsonResponse);
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

  private void entoncesLaPeticionContieneLaApiKey(String apiKey) {
    verify(this.restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
  }
}
