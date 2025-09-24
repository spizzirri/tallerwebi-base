package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Oferta;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioOfertaImpl;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/ofertar")

public class ControladorOfertar {


    private final ServicioOfertaImpl servicioOferta;
    private final RepositorioUsuario repositorioUsuario;


    @Autowired
    public ControladorOfertar(ServicioOfertaImpl servicioOferta, RepositorioUsuario repositorioUsuario) {
        this.servicioOferta = servicioOferta;
        this.repositorioUsuario = repositorioUsuario;
    }


    // Mostrar la página de ofertar
    @GetMapping("/nuevaOferta")
    public String mostrarFormularioOferta(Model model) {
        model.addAttribute("oferta", new Oferta());
        return "nuevaOferta"; // Thymeleaf buscará nuevaOferta.html
    }

    // Guardar la oferta
    @PostMapping("/guardar")
    @Transactional
    public String guardarOferta(@ModelAttribute Oferta oferta,
                                @RequestParam String emailDelOfertador,
                                Model model) {

        // buscar en tu repositorio de usuarios

        Usuario usuario = repositorioUsuario.buscar(emailDelOfertador);

        if (usuario == null) {
            // Si por alguna razón no existe, mostrar error en la vista
            model.addAttribute("error", "No se encontró un usuario con ese email.");
            return "nuevaOferta";
        }

        // Setear campos de la oferta
        oferta.setOfertadorID(usuario);
        oferta.setFechaOferta(LocalDateTime.now());

        // Guardar la oferta
        servicioOferta.crearOferta(oferta);

        // Agregar última oferta al modelo para mostrarla en la vista
        model.addAttribute("ultimaOferta", oferta);

        return "resultadoOferta"; // Thymeleaf buscará resultadoOferta.html


}}
