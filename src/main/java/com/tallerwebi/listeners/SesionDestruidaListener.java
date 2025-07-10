package com.tallerwebi.listeners;

import com.tallerwebi.context.ApplicationContextProvider;
import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import com.tallerwebi.presentacion.dto.ProductoCarritoArmadoDto;
import org.springframework.context.ApplicationContext;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import java.util.List;


@WebListener
public class SesionDestruidaListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {}

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        List<ProductoCarritoDto> carrito = (List<ProductoCarritoDto>) session.getAttribute("carritoSesion");
        List<ProductoCarritoArmadoDto> reservaArmado = (List<ProductoCarritoArmadoDto>) session.getAttribute("reservaArmado");

        if (carrito != null) {
            ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            RepositorioComponente repo = ctx.getBean(RepositorioComponente.class);

            for (ProductoCarritoDto producto : carrito) {
                repo.devolverStockDeUnComponente(producto.getId(), producto.getCantidad());
            }
        }

        if (reservaArmado != null) {
            ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            RepositorioComponente repo = ctx.getBean(RepositorioComponente.class);

            for (ProductoCarritoArmadoDto producto : reservaArmado) {
                repo.devolverStockDeUnComponente(producto.getId(), producto.getCantidad());
            }
        }
    }
}


