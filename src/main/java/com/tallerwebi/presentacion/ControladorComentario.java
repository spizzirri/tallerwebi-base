package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioComentario;
import com.tallerwebi.dominio.excepcion.ComentarioInvalido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorComentario {

  private final ServicioComentario servicioComentario;

  @Autowired
  public ControladorComentario(ServicioComentario servicioComentario) {
    this.servicioComentario = servicioComentario;
  }

  @RequestMapping(path = "/comentarios", method = RequestMethod.GET)
  public ModelAndView irAComentarios() {
    ModelAndView modelAndView = new ModelAndView("comentarios");
    this.agregarComentariosAlModelo(modelAndView);
    return modelAndView;
  }

  @RequestMapping(path = "/publicar-comentario", method = RequestMethod.POST)
  public ModelAndView publicarComentario(
    @RequestParam(name = "contenido", required = false) String contenido
  ) {
    ModelAndView modelAndView = new ModelAndView("comentarios");
    try {
      this.servicioComentario.publicar(contenido);
      modelAndView.addObject("mensaje", "Comentario publicado");
    } catch (ComentarioInvalido excepcion) {
      modelAndView.addObject("error", excepcion.getMessage());
    }
    this.agregarComentariosAlModelo(modelAndView);
    return modelAndView;
  }

  private void agregarComentariosAlModelo(ModelAndView modelAndView) {
    try {
      modelAndView.addObject("comentarios", this.servicioComentario.listar());
    } catch (Exception excepcion) {
      modelAndView.addObject("error", "No se pudieron cargar los comentarios");
    }
  }
}
