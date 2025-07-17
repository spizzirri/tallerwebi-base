package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCompra;
import com.tallerwebi.dominio.ServicioPrecios;
import com.tallerwebi.dominio.ServicioProductoCarritoImpl;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller

public class ControladorPagoEfectivo {

    private ServicioProductoCarritoImpl servicioProductoCarrito;
    private ServicioCompra servicioCompra;
    private ServicioPrecios servicioPrecios;

    public ControladorPagoEfectivo(ServicioCompra servicioCompra,
                                   ServicioProductoCarritoImpl servicioProductoCarrito,
                                   ServicioPrecios servicioPrecios) {
        this.servicioCompra = servicioCompra;
        this.servicioProductoCarrito = servicioProductoCarrito;
        this.servicioPrecios = servicioPrecios;
    }

    @GetMapping("/finalizarPagoEfectivo")
    public ModelAndView mostrarPagoExitoso(HttpSession session) {
        UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

        List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);

        Double totalCompraEnDolares = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
        Double totalCompraEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(totalCompraEnDolares);

        CompraDto compraDto = new CompraDto();
        compraDto.setFecha(LocalDateTime.now());
        compraDto.setMetodoDePago((String) session.getAttribute("metodoPago"));
        compraDto.setTotal(totalCompraEnPesos);
        compraDto.setProductosComprados(convertirACompraComponenteDto(carritoSesion));
        compraDto.setFormaEntrega((String) session.getAttribute("formaEntrega"));
        compraDto.setCostoDeEnvio((Double) session.getAttribute("costo"));
        compraDto.setMoneda("pesos");
        compraDto.setTotalDolar(totalCompraEnDolares);
        compraDto.setValorConDescuento((String) session.getAttribute("totalConDescuento"));
        compraDto.setTotalConDescuentoDolar((Double) session.getAttribute("totalConDescuentoNoFormateado"));

        servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioLogueado, session);
        servicioProductoCarrito.limpiarCarrito();

        session.removeAttribute("totalConDescuento");
        session.removeAttribute("totalConDescuentoNoFormateado");

        return new ModelAndView("redirect:/pagoEfectivo");
    }

    private List<ProductoCarritoDto> obtenerCarritoDeSesion(HttpSession session) {
        List<ProductoCarritoDto> carritoSesion = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        if (carritoSesion == null) {
            carritoSesion = new ArrayList<>();
            session.setAttribute("carritoSesion", carritoSesion);
        }
        return carritoSesion;
    }

    private List<CompraComponenteDto> convertirACompraComponenteDto(List<ProductoCarritoDto> productosCarritoDto) {
        return productosCarritoDto.stream().map(productosCarrito -> {
            CompraComponenteDto compraComponenteDto = new CompraComponenteDto();
            compraComponenteDto.setCantidad(productosCarrito.getCantidad());
            Double precioEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(productosCarrito.getPrecio() * productosCarrito.getCantidad());
            compraComponenteDto.setPrecioDolar(productosCarrito.getPrecio() * productosCarrito.getCantidad());
            compraComponenteDto.setPrecioUnitario(precioEnPesos);
            compraComponenteDto.setId(productosCarrito.getId());

            if (productosCarrito instanceof ProductoCarritoArmadoDto) {
                ProductoCarritoArmadoDto productoCarritoArmadoDto = (ProductoCarritoArmadoDto) productosCarrito;
                compraComponenteDto.setEsArmado(true);
                compraComponenteDto.setNumeroDeArmado(productoCarritoArmadoDto.getNumeroDeArmadoAlQuePertenece());
            } else {
                compraComponenteDto.setEsArmado(false);
            }

            return compraComponenteDto;
        }).collect(Collectors.toList());
    }
}
