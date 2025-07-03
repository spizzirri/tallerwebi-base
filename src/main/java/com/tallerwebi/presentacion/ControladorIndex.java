package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCategorias;
import com.tallerwebi.dominio.ServicioBuscarProducto;

import com.tallerwebi.dominio.ServicioPrecios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorIndex {

    @Autowired
    private ServicioCategorias categoriasService;
    @Autowired
    private ServicioBuscarProducto productoService;

    private ServicioPrecios servicioPrecios;


//    #1 Equipos y Notebook
//    #2 Monitores
//    #3 Televisores
//    #4 Placas de Video
//    #5 Sillas Gamers
//    #6 Perificos
//    #7 Mothers
//    #8 Procesadores

    public ControladorIndex(ServicioBuscarProducto productoService, ServicioCategorias categoriasService, ServicioPrecios servicioPrecios) {
        this.productoService = productoService;
        this.categoriasService = categoriasService;
        this.servicioPrecios = servicioPrecios;
    }


    @GetMapping("/index")
    public ModelAndView irAlIndex(@RequestParam(required = false) String mensajeRegistroExitoso) {
        List<CategoriaDto> categoriasAMostrar = categoriasService.getCategorias();

        ModelMap model = new ModelMap();
        model.put("mensajeRegistroExitoso", mensajeRegistroExitoso);
        CategoriaDto categoriaDestacada = categoriasAMostrar.get(0);
        List<CategoriaDto> otrasCategorias = categoriasAMostrar.subList(1, categoriasAMostrar.size());
        List<CategoriaDto> categoriasLimitada = otrasCategorias.stream().limit(7).collect(Collectors.toList());
        model.addAttribute("categoriaDestacada", categoriaDestacada);
        model.addAttribute("otrasCategorias", categoriasLimitada);

        return new ModelAndView("index", model);
    }

    @GetMapping("/productos/tipoComponente/{id}")
    @Transactional
    public ModelAndView cargarProductosPorCategoria(@PathVariable Integer id) {
        ModelMap model = new ModelMap();

        String tipoComponente = convertirIdATipoComponente(id);
        List<ProductoDto> productosDestacados = productoService.getProductosPorTipo(tipoComponente);
        Collections.shuffle(productosDestacados);
        List<ProductoDto> productosLimitados = productosDestacados.stream().limit(8).collect(Collectors.toList());

        for (ProductoDto producto : productosDestacados) {
            String totalFormateado = this.servicioPrecios.conversionDolarAPeso(producto.getPrecio());
            producto.setPrecioFormateado(totalFormateado);
        }

        model.addAttribute("productosDestacados", productosLimitados);

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
            default:
                return "Componente";
        }
    }


}