package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Producto;
import com.tallerwebi.dominio.ServicioProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ControladorProducto {

    @Autowired
    private ServicioProducto servicioProducto;

    @GetMapping("/buscar")
    public String buscar(@RequestParam(name="productoBuscado") String productoBuscado, Model modelo){
        List<Producto> resultados = servicioProducto.buscarProductos(productoBuscado);
        modelo.addAttribute("resultados",resultados);
        modelo.addAttribute("productoBuscado",productoBuscado);
        return "resultadoBusqueda";
    }
}
