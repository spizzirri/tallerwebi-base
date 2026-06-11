package com.tallerwebi.integracion.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestRestTemplateConfig {

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return mock(RestTemplate.class);
  }
}
