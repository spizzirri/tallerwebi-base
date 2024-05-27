package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reto")
public class ControladorReto {

    @Autowired
    private ServicioReto servicioReto;

    @Autowired
    public ControladorReto(ServicioReto servicioReto) {this.servicioReto = servicioReto;
    }

    @GetMapping("/home")
    public ResponseEntity<String> empezarReto(@RequestParam Long retoId) {
        try {
            servicioReto.empezarReto(retoId);
            return ResponseEntity.ok("El reto ha comenzado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar el reto: " + e.getMessage());
        }
    }

}
