package com.tallerwebi.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuración para la integración con Cloudflare R2 utilizando AWS S3 SDK.
 */
@Configuration
public class R2Config {

  @Value("${r2.accessKey}")
  private String accessKey;

  @Value("${r2.secretKey}")
  private String secretKey;

  @Value("${r2.endpoint}")
  private String endpoint;

  /**
   * Crea un cliente S3 configurado para apuntar a Cloudflare R2.
   *
   * @return Cliente S3 configurado.
   */
  @Bean
  public S3Client s3Client() {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
    return S3Client
      .builder()
      .endpointOverride(URI.create(this.endpoint))
      .region(Region.US_EAST_1)
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .build();
  }
}
