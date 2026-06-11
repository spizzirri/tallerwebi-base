package com.tallerwebi.integracion.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuración de prueba que proporciona un mock de S3Client.
 * Es necesaria para evitar que los tests de integración intenten cargar la
 * configuración productiva (R2Config), la cual requiere propiedades de entorno
 * externas y una conexión real a Cloudflare R2.
 */
@Configuration
public class R2TestConfig {

  @Bean
  public S3Client s3Client() {
    return Mockito.mock(S3Client.class);
  }
}
