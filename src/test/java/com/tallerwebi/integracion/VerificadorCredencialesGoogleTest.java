package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Prueba de integración para verificar la carga correcta de las credenciales de Google
 * en el contexto de Spring.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class })
@TestPropertySource(
  properties = { "GOOGLE_CLIENT_ID=test-client-id", "GOOGLE_CLIENT_SECRET=test-client-secret" }
)
public class VerificadorCredencialesGoogleTest {

  @Value("${GOOGLE_CLIENT_ID:not-set}")
  private String idCliente;

  @Value("${GOOGLE_CLIENT_SECRET:not-set}")
  private String secretoCliente;

  @Test
  public void lasCredencialesDeberianEstarDisponiblesEnElContexto() {
    assertThat(idCliente, equalTo("test-client-id"));
    assertThat(secretoCliente, equalTo("test-client-secret"));
    assertThat(idCliente, not(equalTo("not-set")));
  }
}
