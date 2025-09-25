package com.tallerwebi.dominio;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RepositorioProducto {

    private List<Producto> productos = List.of(
                new Producto(1, "Mouse",5000.0, "Mouse inalámbrico"),
                new Producto(2, "Teclado",30000.0, "Teclado ergonimico"),
                new Producto(3, "Monitor",50000.0, "Monitor gaming")
        );
    public List<Producto> buscarProductoPorNombre(String nombre){
        return productos.stream().filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase())).collect(Collectors.toList());
    }

}
