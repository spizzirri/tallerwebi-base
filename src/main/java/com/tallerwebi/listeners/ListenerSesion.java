package com.tallerwebi.listeners;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.List;

@Component
public class ListenerSesion implements HttpSessionListener {

    private RepositorioComponente repositorioComponente;

    @Autowired
    public ListenerSesion(RepositorioComponente repositorioComponente) {
        this.repositorioComponente = repositorioComponente;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        HttpSession session = se.getSession();
        List<ProductoCarritoDto> productosCarrito = (List<ProductoCarritoDto>)session.getAttribute("carritoSesion");

        if(productosCarrito != null) {
            for (ProductoCarritoDto productoCarritoDto : productosCarrito) {
                repositorioComponente.devolverStockDeUnComponente(productoCarritoDto.getId(), productoCarritoDto.getCantidad());
            }
        }

    }

}
