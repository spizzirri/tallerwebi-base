package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrito.Carrito;
import com.tallerwebi.dominio.Carrito.ServicioCarrito;
import com.tallerwebi.dominio.Productos.CategoriaProductos;
import com.tallerwebi.dominio.Productos.Producto;
import com.tallerwebi.dominio.Productos.ServicioProducto;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.ProductoNoEncontradoException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeControlador {

  private static final String VISTA_HOME = "home";
  private final ServicioProducto servicioProducto;
  private final ServicioCarrito servicioCarrito;

  @Autowired
  public HomeControlador(ServicioProducto servicioProducto, ServicioCarrito servicioCarrito) {
    this.servicioProducto = servicioProducto;
    this.servicioCarrito = servicioCarrito;
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome(
    HttpSession session,
    @RequestParam(value = "categoria", required = false) String categoria,
    @RequestParam(value = "busqueda", required = false) String busqueda
  ) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");
    String rol = (String) session.getAttribute("ROL");

    // Validación estricta: Si no es CLIENTE, no pasa.
    if (usuario == null || !"CLIENTE".equals(rol)) {
      return new ModelAndView("redirect:/login");
    }

    ModelMap modelo = new ModelMap();
    //LLAMO A LOS METODOS PRIVADOS REFACTORIZADOS
    this.cargarDatosUsuario(modelo, usuario);
    this.cargarCategorias(modelo);
    this.cargarProductos(modelo, categoria);
    this.cargarResultadoBusqueda(modelo, busqueda);
    this.cargarIdsCarrito(modelo, usuario);

    return new ModelAndView(VISTA_HOME, modelo);
  }

  private void cargarDatosUsuario(ModelMap modelo, Usuario usuario) {
    modelo.put("usuario", usuario);
  }

  private void cargarCategorias(ModelMap modelo) {
    List<CategoriaProductos> categorias = this.servicioProducto.obtenerListadoCategorias();
    modelo.put("categorias", categorias);
  }

  private void cargarProductos(ModelMap modelo, String categoria) {
    try {
      List<Producto> productos;
      if (categoria != null && !categoria.isEmpty()) {
        productos = this.servicioProducto.obtenerListadoProductosFiltrado(categoria);
      } else {
        productos = this.servicioProducto.obtenerListadoProductos();
      }
      modelo.put("productos", productos);
    } catch (ProductoNoEncontradoException e) {
      modelo.put("errorCargaProductos", e.getMessage());
    }
  }

  private void cargarResultadoBusqueda(ModelMap modelo, String busqueda) {
    try {
      if (busqueda != null && !busqueda.trim().isEmpty()) {
        List<Producto> buscados = this.servicioProducto.buscarProductosPorNombre(busqueda);
        modelo.put("productosBuscados", buscados);
      }
    } catch (ProductoNoEncontradoException e) {
      modelo.put("errorBusquedaProductos", e.getMessage());
    }
  }

  private void cargarIdsCarrito(ModelMap modelo, Usuario usuario) {
    Carrito carrito = servicioCarrito.obtenerOCrearCarrito(usuario.getId());
    List<Long> idsEnCarrito = carrito
      .getItems()
      .stream()
      .map(item -> item.getProducto().getId())
      .collect(Collectors.toList());
    modelo.put("idsEnCarrito", idsEnCarrito);
  }
}
