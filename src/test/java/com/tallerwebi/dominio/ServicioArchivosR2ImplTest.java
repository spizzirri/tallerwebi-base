package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class ServicioArchivosR2ImplTest {

  private S3Client s3Client;
  private ServicioArchivosR2 servicioArchivosR2;
  private final String bucketName = "test-bucket";

  @BeforeEach
  public void init() {
    this.s3Client = mock(S3Client.class);
    this.servicioArchivosR2 = new ServicioArchivosR2Impl(this.s3Client, this.bucketName);
  }

  @Test
  public void alSubirArchivoDeberiaLlamarS3Client() throws IOException {
    MultipartFile archivo = this.dadoQueTengoUnArchivoParaSubir();

    this.cuandoSuboElArchivo("test.txt", archivo);

    this.entoncesSeLlamoAlServicioS3(1);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void alObtenerArchivoDeberiaLlamarS3Client() {
    this.dadoQueExisteUnArchivoEnElBucket("test.txt");

    InputStream inputStream = this.cuandoObtengoElArchivo("test.txt");

    this.entoncesElResultadoEsValido(inputStream);
    this.entoncesSeLlamoAlServicioS3ParaObtener(1);
  }

  @Test
  public void alListarArchivosDeberiaRetornarListaDeNombres() {
    this.dadoQueElBucketTieneArchivos("test.txt");

    List<String> nombres = this.cuandoListarArchivos();

    this.entoncesLaListaDeNombresEsCorrecta(nombres, "test.txt");
  }

  private MultipartFile dadoQueTengoUnArchivoParaSubir() {
    return new MockMultipartFile("file", "test.txt", "text/plain", "contenido".getBytes());
  }

  private void dadoQueExisteUnArchivoEnElBucket(String nombre) {
    ResponseInputStream<GetObjectResponse> mockResponseInputStream = mock(
      ResponseInputStream.class
    );
    when(this.s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseInputStream);
  }

  private void dadoQueElBucketTieneArchivos(String nombre) {
    ListObjectsV2Response mockResponse = mock(ListObjectsV2Response.class);
    S3Object mockObject = mock(S3Object.class);
    when(mockObject.key()).thenReturn(nombre);
    when(mockResponse.contents()).thenReturn(Collections.singletonList(mockObject));
    when(this.s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);
  }

  private void cuandoSuboElArchivo(String nombre, MultipartFile archivo) throws IOException {
    this.servicioArchivosR2.subirArchivo(nombre, archivo);
  }

  private InputStream cuandoObtengoElArchivo(String nombre) {
    return this.servicioArchivosR2.obtenerArchivo(nombre);
  }

  private List<String> cuandoListarArchivos() {
    return this.servicioArchivosR2.listarArchivos();
  }

  private void entoncesSeLlamoAlServicioS3(int veces) {
    verify(this.s3Client, times(veces))
      .putObject(
        any(PutObjectRequest.class),
        any(software.amazon.awssdk.core.sync.RequestBody.class)
      );
  }

  private void entoncesSeLlamoAlServicioS3ParaObtener(int veces) {
    verify(this.s3Client, times(veces)).getObject(any(GetObjectRequest.class));
  }

  private void entoncesElResultadoEsValido(InputStream result) {
    assertNotNull(result);
  }

  private void entoncesLaListaDeNombresEsCorrecta(List<String> nombres, String nombreEsperado) {
    assertEquals(1, nombres.size());
    assertEquals(nombreEsperado, nombres.get(0));
  }
}
