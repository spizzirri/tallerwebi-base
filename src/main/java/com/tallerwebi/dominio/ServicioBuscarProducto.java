package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ProductoDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioBuscarProducto {

    private List<ProductoDto> productos;

    public ServicioBuscarProducto() {
        this.productos = new ArrayList<ProductoDto>();
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6, "mouse.png"));
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6 , "mouse.png"));
        this.productos.add(new ProductoDto("Notebook cheta", 79.99, 1, "notebook.png"));
        this.productos.add(new ProductoDto("Notebook media pelo", 29.99, 1 , "notebook.png"));
        this.productos.add(new ProductoDto("Silla gamer", 79.99, 5, "silla.jpg"));
        this.productos.add(new ProductoDto("Plaquita de video", 29.99, 4 ,"auriculares.png"));
        this.productos.add(new ProductoDto("Mother earr", 79.99, 7,"auriculares.png"));
        this.productos.add(new ProductoDto("Procesadoromon", 29.99, 8 ,"auriculares.png"));
        this.productos.add(new ProductoDto("Monitor", 79.99, 2,"monitor.jpg"));
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6, "mouse.png" ));
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6 , "mouse.png"));
        this.productos.add(new ProductoDto("Notebook cheta", 79.99, 1, "notebook.png"));
        this.productos.add(new ProductoDto("Notebook media pelo", 29.99, 1 , "notebook.png"));
        this.productos.add(new ProductoDto("Silla gamer", 79.99, 5, "silla.jpg"));
        this.productos.add(new ProductoDto("Plaquita de video", 29.99, 4 ,"auriculares.png"));
        this.productos.add(new ProductoDto("Mother earr", 79.99, 7,"auriculares.png"));
        this.productos.add(new ProductoDto("Procesadoromon", 29.99, 8 ,"auriculares.png"));
        this.productos.add(new ProductoDto("Monitor", 79.99, 2, "monitor.jpg"));
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6 , "mouse.png"));
        this.productos.add(new ProductoDto("Mouse inalámbrico", 29.99, 6, "mouse.png" ));
        this.productos.add(new ProductoDto("Notebook cheta", 79.99, 1, "notebook.png"));
        this.productos.add(new ProductoDto("Notebook media pelo", 29.99, 1 , "notebook.png"));
        this.productos.add(new ProductoDto("Silla gamer", 79.99, 5, "silla.jpg"));
        this.productos.add(new ProductoDto("Plaquita de video", 29.99, 4,"auriculares.png" ));
        this.productos.add(new ProductoDto("Mother earr", 79.99, 7,"auriculares.png"));
        this.productos.add(new ProductoDto("Procesadoromon", 29.99, 8 ,"auriculares.png"));
        this.productos.add(new ProductoDto("Monitor", 79.99, 2,"monitor.jpg"));
        this.productos.add(new ProductoDto("Monitor", 79.99, 2,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
        this.productos.add(new ProductoDto("aaaaaa", 79.99, 3,"monitor.jpg"));
    }

    public List<ProductoDto> buscarPorCategoria(Integer num){
        List<ProductoDto> productosDestacados = new ArrayList<>();
        for (ProductoDto productoDto : this.productos) {
            if (productoDto.getCategoria().equals(num)) {
                productosDestacados.add(productoDto);
            }
        }
        return productosDestacados;
    }

    public List<ProductoDto> getProductos() {
        return productos;
    }
}
