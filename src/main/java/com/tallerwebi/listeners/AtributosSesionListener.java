//package com.tallerwebi.listeners;
//
//import com.tallerwebi.context.ApplicationContextProvider;
//import com.tallerwebi.dominio.RepositorioComponente;
//import com.tallerwebi.presentacion.ProductoCarritoDto;
//import org.springframework.context.ApplicationContext;
//
//import javax.servlet.annotation.WebListener;
//import javax.servlet.http.HttpSessionAttributeListener;
//import javax.servlet.http.HttpSessionBindingEvent;
//import java.util.List;
//
//@WebListener
//public class AtributosSesionListener implements HttpSessionAttributeListener {
//
//    @Override
//    public void attributeAdded(HttpSessionBindingEvent event) {}
//
//    @Override
//    public void attributeReplaced(HttpSessionBindingEvent event){}
//
//    @Override
//    public void attributeRemoved(HttpSessionBindingEvent event) {
//        String nombre = event.getName();
//
//
//        if (nombre.equals("carritoSesion")){
//            List<ProductoCarritoDto> carrito = ((List<ProductoCarritoDto>)event.getValue());
//
//            // Verificamos el ServletContext
//            ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
//
//            if (ctx == null) {
//                System.out.println("ERROR: el WebApplicationContext es null");
//                try {
//                    // Pausa de 5 segundos (10,000 milisegundos)
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return;
//            }
//
//            RepositorioComponente repositorioComponente = ctx.getBean(RepositorioComponente.class);
//
//            for (ProductoCarritoDto productoCarrito : carrito) {
//                repositorioComponente.devolverStockDeUnComponente(productoCarrito.getId(), productoCarrito.getCantidad());
//            }
//
//        }
//    }
//}