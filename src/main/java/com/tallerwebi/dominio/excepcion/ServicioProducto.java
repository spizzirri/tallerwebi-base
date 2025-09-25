package com.tallerwebi.dominio.excepcion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioProducto {
    @Autowired
    private RepositorioProducto repositorioProducto;

    public List<Producto> buscarProductos(String nombre){
        return repositorioProducto.buscarProductoPorNombre(nombre);
    }
}
