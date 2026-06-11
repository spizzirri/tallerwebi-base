package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioArchivosR2;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorArchivosR2Test {

  private ServicioArchivosR2 servicioArchivosR2;
  private ControladorArchivosR2 controlador;
  private ModelMap model;
  private RedirectAttributes redirectAttributes;

  @BeforeEach
  public void init() {
    this.servicioArchivosR2 = mock(ServicioArchivosR2.class);
    this.controlador = new ControladorArchivosR2(this.servicioArchivosR2);
    this.model = new ModelMap();
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
