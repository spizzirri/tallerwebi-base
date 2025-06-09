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
public class ControladorIndex {

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

    public ControladorIndex(ServicioBuscarProducto productoService) {
        this.productoService = productoService;
    }


    @GetMapping("/index")
    public ModelAndView irAlIndex() {
        List<CategoriaDto> categoriasAMostrar = categoriasService.getCategorias();

        ModelMap model = new ModelMap();
        CategoriaDto categoriaDestacada = categoriasAMostrar.get(0);
        List<CategoriaDto> otrasCategorias = categoriasAMostrar.subList(1, categoriasAMostrar.size());

        model.addAttribute("categoriaDestacada", categoriaDestacada);
        model.addAttribute("otrasCategorias", otrasCategorias);

        List<ProductoDto> productosDescuento = productoService.getProductosMenoresAUnPrecio(150000D);

        model.addAttribute("productosDescuento", productosDescuento);

        return new ModelAndView("index", model);
    }

    @GetMapping("/productos/tipoComponente/{id}")
    @Transactional
    public ModelAndView cargarProductosPorCategoria(@PathVariable Integer id) {

        ModelMap model = new ModelMap();

            String tipoComponente = convertirIdATipoComponente(id);
            List<Componente> componentes = repositorioComponente.obtenerComponentesPorTipo(tipoComponente);

            List<ProductoDto> productosDestacados = componentes.stream()
                    .map(ProductoDto::new).collect(Collectors.toList());

            model.addAttribute("productosDestacados", productosDestacados);
            return new ModelAndView("cargarProductosDinamicos", model);

    }

    private String convertirIdATipoComponente(Integer id) {
        switch (id) {
            case 1:
                return "Componente";
            case 2:
                return "Procesador";
            case 3:
                return "Motherboard";
            case 4:
                return "CoolerCPU";
            case 5:
                return "MemoriaRAM";
            case 6:
                return "PlacaDeVideo";
            case 7:
                return "Almacenamiento";
            case 8:
                return "FuenteDeAlimentacion";
            case 9:
                return "Gabinete";
        }
        return "Componente";
    }

    @PostMapping("/agregarAlCarrito")
    @ResponseBody
    public Map<String, Object> agregarProductoAlCarrito(
            @RequestParam Long componenteId,
            @RequestParam(defaultValue = "1") Integer cantidad) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (!servicioProductoCarrito.verificarStock(componenteId, cantidad)) {
                response.put("success", false);
                response.put("error", "Stock insuficiente");
            } else {
                servicioProductoCarrito.agregarProducto(componenteId, cantidad);

                response.put("success", true);
                response.put("mensaje", "Producto agregado al carrito!");
                response.put("cantidadEnCarrito", servicioProductoCarrito.getProductos().size());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "Error al agregar producto al carrito!");
        }
        return response;
    }


}

