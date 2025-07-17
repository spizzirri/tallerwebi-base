package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.dto.ComponenteDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ControladorTarjetaDeCredito {

    @Autowired
    ServicioTarjetaDeCredito servicioTarjeta;
    @Autowired
    private ServicioProductoCarritoImpl servicioProductoCarrito;
    private ServicioCompra servicioCompra;
    private ServicioPrecios servicioPrecios;

    public ControladorTarjetaDeCredito(ServicioTarjetaDeCredito servicioTarjeta,
                                       ServicioCompra servicioCompra,
                                       ServicioProductoCarritoImpl servicioProductoCarrito,
                                       ServicioPrecios servicioPrecios) {
        this.servicioTarjeta = servicioTarjeta;
        this.servicioCompra = servicioCompra;
        this.servicioProductoCarrito = servicioProductoCarrito;
        this.servicioPrecios = servicioPrecios;
    }

    @PostMapping("/tarjetaDeCredito/validar")
    public ModelAndView validar(@RequestParam String numeroTarjeta,
                                @RequestParam String vencimiento,
                                @RequestParam String codigoSeguridad,
                                @RequestParam String metodoDePago,
                                @RequestParam String moneda,
                                @RequestParam String nombreCliente,
                                @RequestParam String ivaCliente,
                                @RequestParam String documento,
                                HttpSession session) {

        ModelMap modelo = new ModelMap();
        TarjetaDeCreditoDto tarjeta = new TarjetaDeCreditoDto(numeroTarjeta, vencimiento, codigoSeguridad);

        Boolean tarjetaValida = servicioTarjeta.validarLongitudDeNumeroDeTarjeta(tarjeta);
        Boolean fechaDeVencimiento = servicioTarjeta.validarVencimiento(tarjeta);
        Boolean codigoDeSeguridad = servicioTarjeta.codigoDeSeguridad(tarjeta);

        boolean hayError = false;
        session.setAttribute("iva", ivaCliente);
        if (!tarjetaValida) {
            modelo.addAttribute("mensajeTarjeta", "Número de tarjeta inválido");
            hayError = true;
        }
        if (!fechaDeVencimiento) {
            modelo.addAttribute("mensajeVencimiento", "Fecha de vencimiento inválida");
            hayError = true;
        }
        if (!codigoDeSeguridad) {
            modelo.addAttribute("mensajeCodigo", "Código de seguridad inválido");
            hayError = true;
        }
        modelo.addAttribute("tarjeta", tarjeta);
        session.setAttribute("tarjeta", tarjeta);
        if (hayError) {
            return new ModelAndView("tarjetaDeCredito", modelo);
        } else {
            UsuarioDto usuarioLogueado = (UsuarioDto) session.getAttribute("usuario");

            List<ProductoCarritoDto> carritoSesion = obtenerCarritoDeSesion(session);
            session.setAttribute("moneda", moneda);

            Double totalCompraEnDolares = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
            Double totalCompraEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(totalCompraEnDolares);

            CompraDto compraDto = new CompraDto();
            compraDto.setDocumento(documento);
            compraDto.setNombreTitular(nombreCliente);
            compraDto.setFecha(LocalDateTime.now());
            compraDto.setMetodoDePago(metodoDePago);
            compraDto.setTotal(totalCompraEnPesos);
            compraDto.setProductosComprados(convertirACompraComponenteDto(carritoSesion));
            compraDto.setFormaEntrega((String) session.getAttribute("formaEntrega"));
            compraDto.setCostoDeEnvio((Double) session.getAttribute("costo"));
            compraDto.setMoneda(moneda);
            compraDto.setTotalDolar(totalCompraEnDolares);
            compraDto.setValorConDescuento((String) session.getAttribute("totalConDescuento"));
            compraDto.setTotalConDescuentoDolar((Double) session.getAttribute("totalConDescuentoNoFormateado"));

            servicioCompra.guardarCompraConUsuarioLogueado(compraDto, usuarioLogueado, session );
            servicioProductoCarrito.limpiarCarrito();

            session.removeAttribute("totalConDescuento");
            session.removeAttribute("totalConDescuentoNoFormateado");
            return new ModelAndView("redirect:/pagoExitoso");
        }
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
            compraComponenteDto.setPrecioUnitario(precioEnPesos);
            compraComponenteDto.setPrecioDolar(productosCarrito.getPrecio() * productosCarrito.getCantidad());
            compraComponenteDto.setId(productosCarrito.getId());

            if (productosCarrito instanceof ProductoCarritoArmadoDto){
                ProductoCarritoArmadoDto productoCarritoArmadoDto =  (ProductoCarritoArmadoDto) productosCarrito;
                compraComponenteDto.setEsArmado(true);
                compraComponenteDto.setNumeroDeArmado(productoCarritoArmadoDto.getNumeroDeArmadoAlQuePertenece());
            } else {
                compraComponenteDto.setEsArmado(false);
            }

            return compraComponenteDto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/tarjetaDeCredito")
    public ModelAndView mostrarFormularioTarjeta(HttpSession session) {
        ModelMap modelo = new ModelMap();
        Double totalCompraEnDolares = this.servicioProductoCarrito.calcularValorTotalDeLosProductos();
        Double totalCompraEnPesos = this.servicioPrecios.conversionDolarAPesoDouble(totalCompraEnDolares);
        modelo.put("precioDolar", this.servicioPrecios.obtenerPrecioFormateado(totalCompraEnDolares));
        modelo.put("precioPesos", totalCompraEnPesos);
        modelo.put("moneda", session.getAttribute("moneda"));

        return new ModelAndView("tarjetaDeCredito", modelo);
    }
}