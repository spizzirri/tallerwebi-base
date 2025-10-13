package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorSubasta {

    private ServicioSubasta servicioSubasta;

    @Autowired
    public  ControladorSubasta(ServicioSubasta servicioSubasta) {
        this.servicioSubasta = servicioSubasta;
    }

    @RequestMapping(path = "/nuevaSubasta", method = RequestMethod.GET)
    public ModelAndView irANuevaSubasta() {
        ModelMap model = new ModelMap();
        model.put("subasta", new Subasta());
        List<Categoria> cat = servicioSubasta.listarCategoriasDisponibles();
        model.put("listaCategorias", cat);
        return new ModelAndView("nuevaSubasta", model);
    }

    @RequestMapping(path = "/crearSubasta", method = RequestMethod.POST)
    public ModelAndView crearSubasta(@ModelAttribute("subasta") Subasta subasta, @RequestParam("imagenSubasta") MultipartFile imagenSubasta, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        String creadorEmail = (String) request.getSession().getAttribute("USUARIO");
        try{
            servicioSubasta.crearSubasta(subasta, creadorEmail, imagenSubasta);
        }catch (Exception e){
            model.put("error", e.getMessage());
            List<Categoria> cat = servicioSubasta.listarCategoriasDisponibles();
            model.put("listaCategorias", cat);
            return new ModelAndView("nuevaSubasta", model);
        }
        return new ModelAndView("redirect:/confirmacion-subasta");
    }

    @RequestMapping(value = "/{subastaID}/imagen")
    public ResponseEntity<byte[]> getImagenSubasta(@PathVariable("subastaID") Long subastaID, HttpServletRequest request) {
        Subasta s = servicioSubasta.buscarSubasta(subastaID);
        if(s.getId() != null && s.getId().equals(subastaID)){
            byte[] imagenBytes = java.util.Base64.getDecoder().decode(s.getImagen());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imagenBytes, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND );
    }

    @RequestMapping(path = "/confirmacion-subasta", method = RequestMethod.GET)
    public String confirmacionDeSubastaRealizada(Model model) {
        return "confirmacion-subasta";
    }
}
