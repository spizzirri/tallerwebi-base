package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.ServiceCategorias;
import com.tallerwebi.dominio.ServicioBuscarProducto;
import com.tallerwebi.dominio.ServicioProductoCarrito;
import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class ControladorMostrarProductos {

    @Autowired
    private ServiceCategorias categoriasService;
    @Autowired
    private ServicioBuscarProducto productoService;
    @Autowired
    private ServicioProductoCarrito servicioProductoCarrito;
    @Autowired
    private RepositorioComponente repositorioComponente;


//    #1 Equipos y Notebook
//    #2 Monitores
//    #3 Televisores
//    #4 Placas de Video
//    #5 Sillas Gamers
//    #6 Perificos
//    #7 Mothers
//    #8 Procesadores

    public ControladorMostrarProductos(ServicioBuscarProducto productoService) {
        this.productoService = productoService;
    }


    @GetMapping("/productos")
    public ModelAndView showProductos() {
        ModelMap model = new ModelMap();
        List<ProductoDto> productos = productoService.getProductosEnStock();
        model.addAttribute("productos", productos);
        return new ModelAndView("productos", model);
    }

    @GetMapping("/productos/search")
    public ModelAndView  buscarProductos(@RequestParam("q") String query) {
        ModelMap model = new ModelMap();

        List<ProductoDto> productos = productoService.getProductosPorQuery(query);
        model.addAttribute("productosBuscados", productos);
        if (query != null && !query.trim().isEmpty()) {
            if (productos.isEmpty()) {
                model.addAttribute("productosBuscados", false);
                model.addAttribute("mensaje", "No se encontraron resultados para \"" + query + "\". Mostrando todos los productos.");
            }
        }

        return new ModelAndView("productos", model);
    }

//    @PostMapping("/agregarAlCarrito")
//    @ResponseBody
//    public Map<String, Object> agregarProductoAlCarrito(
//            @RequestParam Long componenteId,
//            @RequestParam(defaultValue = "1") Integer cantidad) {
//
//        Map<String, Object> response = new HashMap<>();
//        try {
//            if (!servicioProductoCarrito.verificarStock(componenteId, cantidad)) {
//                response.put("success", false);
//                response.put("error", "Stock insuficiente");
//            } else {
//                servicioProductoCarrito.agregarProducto(componenteId, cantidad);
//
//                response.put("success", true);
//                response.put("mensaje", "Producto agregado al carrito!");
//                response.put("cantidadEnCarrito", servicioProductoCarrito.getProductos().size());
//            }
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("mensaje", "Error al agregar producto al carrito!");
//        }
//        return response;
//    }


}

