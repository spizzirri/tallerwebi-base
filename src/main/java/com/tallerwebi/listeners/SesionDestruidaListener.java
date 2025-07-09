package com.tallerwebi.listeners;

import com.tallerwebi.context.ApplicationContextProvider;
import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.presentacion.ProductoCarritoDto;
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

        if (carrito != null) {
            ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
            RepositorioComponente repo = ctx.getBean(RepositorioComponente.class);

            if (ctx == null) {
                System.out.println("ERROR: el WebApplicationContext es null");
                try {
                    // Pausa de 5 segundos (5,000 milisegundos)
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }

            for (ProductoCarritoDto producto : carrito) {
                repo.devolverStockDeUnComponente(producto.getId(), producto.getCantidad());
            }
        }
    }
}


