package com.tallerwebi.integracion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@Configuration
public class TestRestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  public MockRestServiceServer servidorApiExterna(RestClient.Builder restClientBuilder) {
    return MockRestServiceServer.bindTo(restClientBuilder).build();
  }

  // El servidor mock debe vincularse al builder antes de construir el cliente
  @Bean
  @Primary
  @DependsOn("servidorApiExterna")
  public RestClient restClient(RestClient.Builder restClientBuilder) {
    return restClientBuilder.build();
  }
}
