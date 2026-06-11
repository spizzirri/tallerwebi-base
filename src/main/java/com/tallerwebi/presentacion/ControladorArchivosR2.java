package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioArchivosR2;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la gestión de archivos (subida/descarga) en R2.
 */
@Controller
public class ControladorArchivosR2 {

  private final ServicioArchivosR2 servicioArchivosR2;

  /**
   * Constructor del controlador.
   *
   * @param servicioArchivosR2 Servicio inyectado.
   */
  @Autowired
  public ControladorArchivosR2(ServicioArchivosR2 servicioArchivosR2) {
    this.servicioArchivosR2 = servicioArchivosR2;
  }

  /**
   * Muestra la vista del gestor de archivos con la lista actual.
   *
   * @param model Modelo para la vista.
   * @return Vista del gestor.
   */
  @GetMapping("/gestor-archivos")
  public String irAGestorArchivos(ModelMap model) {
    model.put("archivos", this.servicioArchivosR2.listarArchivos());
    return "gestor-archivos";
  }

  /**
   * Sube un archivo mediante una solicitud POST y redirige.
   * Utiliza RedirectAttributes para pasar mensajes de estado a través de la redirección
   * siguiendo el patrón POST-Redirect-GET para evitar duplicidad de envíos.
   *
   * @param archivo Archivo a subir.
   * @param redirectAttributes Atributos para la redirección.
   * @return Redirección a la vista del gestor.
   */
  @PostMapping("/subir-archivo")
  public String subirArchivo(
    @RequestParam("archivo") MultipartFile archivo,
    RedirectAttributes redirectAttributes
  ) {
    try {
      this.servicioArchivosR2.subirArchivo(archivo.getOriginalFilename(), archivo);
      // addFlashAttribute guarda el mensaje en sesión, sobrevive a la redirección
      // y se elimina automáticamente después de ser consumido.
      redirectAttributes.addFlashAttribute("mensaje", "Archivo subido correctamente");
    } catch (IOException e) {
      redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
    }
    return "redirect:/gestor-archivos";
  }

  /**
   * Descarga un archivo desde R2.
   * Se utiliza ResponseEntity en lugar de una vista (como ModelAndView) debido a que
   * esta operación devuelve datos binarios, permitiendo el control directo sobre los
   * encabezados HTTP (Content-Type y Content-Disposition) para forzar la descarga.
   *
   * @param nombreArchivo Nombre del archivo a descargar.
   * @return ResponseEntity con el archivo en bytes.
   */
  @GetMapping("/descargar-archivo/{nombreArchivo:.+}")
  public ResponseEntity<byte[]> descargarArchivo(
    @PathVariable("nombreArchivo") String nombreArchivo
  ) {
    String nombreDecodificado = URLDecoder.decode(nombreArchivo, StandardCharsets.UTF_8);

    try (InputStream inputStream = this.servicioArchivosR2.obtenerArchivo(nombreDecodificado)) {
      byte[] bytes = inputStream.readAllBytes();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentDispositionFormData("attachment", nombreDecodificado);
      return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
