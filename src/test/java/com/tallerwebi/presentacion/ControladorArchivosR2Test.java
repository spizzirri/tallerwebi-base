package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioArchivosR2;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorArchivosR2Test {

  private ServicioArchivosR2 servicioArchivosR2;
  private ControladorArchivosR2 controlador;
  private Map<String, Object> model;
  private RedirectAttributes redirectAttributes;

  @BeforeEach
  public void init() {
    this.servicioArchivosR2 = mock(ServicioArchivosR2.class);
    this.controlador = new ControladorArchivosR2(this.servicioArchivosR2);
    this.model = new HashMap<>();
    this.redirectAttributes = mock(RedirectAttributes.class);
  }

  @Test
  public void alSubirArchivoExitosamenteDeberiaRedirigirConMensaje() throws IOException {
    MultipartFile archivo = this.dadoQueTengoUnArchivoASubir();

    String vista = this.cuandoSuboElArchivo(archivo);

    this.entoncesLaVistaEsRedireccionAGestor(vista);
    this.entoncesSeLlamoAlServicio(1);
    verify(this.redirectAttributes).addFlashAttribute("mensaje", "Archivo subido correctamente");
  }

  @Test
  public void alSubirArchivoConErrorDeberiaRedirigirConError() throws IOException {
    MultipartFile archivo = this.dadoQueTengoUnArchivoASubir();
    this.dadoQueElServicioFalla();

    String vista = this.cuandoSuboElArchivo(archivo);

    this.entoncesLaVistaEsRedireccionAGestor(vista);
    verify(this.redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  public void alDescargarArchivoExitosamenteDeberiaRetornarBytes() throws IOException {
    this.dadoQueExisteUnArchivoEnElServidor("test.txt", "contenido");

    ResponseEntity<byte[]> response = this.cuandoSolicitoLaDescarga("test.txt");

    this.entoncesElContenidoEsCorrecto(response, "contenido");
  }

  @Test
  public void alIrAlGestorDeberiaMostrarLaVistaConLosArchivos() {
    List<String> archivos = List.of("a.txt", "b.pdf");
    when(this.servicioArchivosR2.listarArchivos()).thenReturn(archivos);

    String vista = this.controlador.irAGestorArchivos(this.model);

    assertEquals("gestor-archivos", vista);
    assertEquals(archivos, this.model.get("archivos"));
  }

  @Test
  public void alDescargarArchivoDeberiaRetornarHeadersDeDescarga() {
    this.dadoQueExisteUnArchivoEnElServidor("informe ñ.txt", "contenido");

    ResponseEntity<byte[]> response = this.cuandoSolicitoLaDescarga("informe%20%C3%B1.txt");

    HttpHeaders headers = response.getHeaders();
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, headers.getContentType());
    ContentDisposition disposicion = headers.getContentDisposition();
    assertTrue(disposicion.isAttachment());
    assertEquals("informe ñ.txt", disposicion.getFilename());
  }

  @Test
  public void alDescargarArchivoConErrorDeLecturaDeberiaRetornarError500() throws IOException {
    InputStream inputStreamConError = mock(InputStream.class);
    when(inputStreamConError.readAllBytes()).thenThrow(new IOException("Error R2"));
    when(this.servicioArchivosR2.obtenerArchivo("test.txt")).thenReturn(inputStreamConError);

    ResponseEntity<byte[]> response = this.cuandoSolicitoLaDescarga("test.txt");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNull(response.getBody());
  }

  private MultipartFile dadoQueTengoUnArchivoASubir() {
    return new MockMultipartFile("file", "test.txt", "text/plain", "contenido".getBytes());
  }

  private void dadoQueElServicioFalla() throws IOException {
    doThrow(new IOException("Error R2"))
      .when(this.servicioArchivosR2)
      .subirArchivo(anyString(), any(MultipartFile.class));
  }

  private void dadoQueExisteUnArchivoEnElServidor(String nombre, String contenido) {
    InputStream inputStream = new ByteArrayInputStream(contenido.getBytes());
    when(this.servicioArchivosR2.obtenerArchivo(nombre)).thenReturn(inputStream);
  }

  private String cuandoSuboElArchivo(MultipartFile archivo) {
    return this.controlador.subirArchivo(archivo, this.redirectAttributes);
  }

  private ResponseEntity<byte[]> cuandoSolicitoLaDescarga(String nombre) {
    return this.controlador.descargarArchivo(nombre);
  }

  private void entoncesLaVistaEsRedireccionAGestor(String vista) {
    assertEquals("redirect:/gestor-archivos", vista);
  }

  private void entoncesSeLlamoAlServicio(int veces) throws IOException {
    verify(this.servicioArchivosR2, times(veces))
      .subirArchivo(anyString(), any(MultipartFile.class));
  }

  private void entoncesElContenidoEsCorrecto(
    ResponseEntity<byte[]> response,
    String contenidoEsperado
  ) {
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(contenidoEsperado, new String(response.getBody()));
  }
}
