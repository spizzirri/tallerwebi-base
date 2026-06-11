package com.tallerwebi.dominio;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interfaz para la gestión de archivos en R2.
 */
public interface ServicioArchivosR2 {
  /**
   * Sube un archivo a R2.
   *
   * @param nombreArchivo Nombre del archivo en el bucket.
   * @param archivo El archivo a subir.
   * @throws IOException Si ocurre un error al procesar el archivo.
   */
  void subirArchivo(String nombreArchivo, MultipartFile archivo) throws IOException;

  /**
   * Obtiene un flujo de entrada para descargar un archivo desde R2.
   *
   * @param nombreArchivo Nombre del archivo en el bucket.
   * @return InputStream del archivo.
   */
  InputStream obtenerArchivo(String nombreArchivo);

  /**
   * Lista los nombres de todos los archivos en el bucket.
   *
   * @return Lista de nombres de archivos.
   */
  java.util.List<String> listarArchivos();
}
