package com.tallerwebi.dominio;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Implementación de la gestión de archivos en R2.
 */
@Service("servicioArchivosR2")
public class ServicioArchivosR2Impl implements ServicioArchivosR2 {

  private final S3Client s3Client;
  private final String bucketName;

  /**
   * Constructor del servicio.
   *
   * @param s3Client Cliente S3 inyectado.
   * @param bucketName Nombre del bucket desde properties.
   */
  @Autowired
  public ServicioArchivosR2Impl(S3Client s3Client, @Value("${r2.bucketName}") String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }

  @Override
  public void subirArchivo(String nombreArchivo, MultipartFile archivo) throws IOException {
    PutObjectRequest putObjectRequest = PutObjectRequest
      .builder()
      .bucket(this.bucketName)
      .key(nombreArchivo)
      .contentType(archivo.getContentType())
      .build();

    this.s3Client.putObject(
        putObjectRequest,
        RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize())
      );
  }

  @Override
  public InputStream obtenerArchivo(String nombreArchivo) {
    GetObjectRequest getObjectRequest = GetObjectRequest
      .builder()
      .bucket(this.bucketName)
      .key(nombreArchivo)
      .build();

    return this.s3Client.getObject(getObjectRequest);
  }

  @Override
  public List<String> listarArchivos() {
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request
      .builder()
      .bucket(this.bucketName)
      .build();

    return this.s3Client.listObjectsV2(listObjectsRequest)
      .contents()
      .stream()
      .map(S3Object::key)
      .collect(Collectors.toList());
  }
}
