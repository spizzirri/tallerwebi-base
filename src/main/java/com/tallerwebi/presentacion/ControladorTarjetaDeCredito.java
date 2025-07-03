package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioProductoCarritoImpl;
import com.tallerwebi.dominio.ServicioTarjetaDeCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ControladorTarjetaDeCredito {

    @Autowired
    private ServicioTarjetaDeCredito servicioTarjeta;

    @Autowired
    private ServicioProductoCarritoImpl productoService;

    @PostMapping("/tarjetaDeCredito/validar")
    public ModelAndView validar(@RequestParam String numeroTarjeta,
                                @RequestParam String vencimiento,
                                @RequestParam String codigoSeguridad) {

        ModelMap modelo = new ModelMap();
        TarjetaDeCreditoDto tarjeta = new TarjetaDeCreditoDto(numeroTarjeta, vencimiento, codigoSeguridad);

        Boolean tarjetaValida = servicioTarjeta.validarLongitudDeNumeroDeTarjeta(tarjeta);
        Boolean fechaDeVencimiento = servicioTarjeta.validarVencimiento(tarjeta);
        Boolean codigoDeSeguridad = servicioTarjeta.codigoDeSeguridad(tarjeta);

        boolean hayError = false;

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

        if (hayError) {
            return new ModelAndView("tarjetaDeCredito", modelo);
        } else {
            String fechaCompra = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            modelo.addAttribute("fechaCompra", fechaCompra);
            return new ModelAndView("pagoExitoso", modelo);
        }
    }

    @GetMapping("/tarjetaDeCredito")
    public ModelAndView mostrarFormularioTarjeta() {
        ModelMap modelo = new ModelMap();
        return new ModelAndView("tarjetaDeCredito", modelo);
    }
}