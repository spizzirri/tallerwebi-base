package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.infraestructura.RepositorioComponenteImpl;
import com.tallerwebi.presentacion.ProductoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("servicioBuscarProducto")
@Transactional
public class ServicioBuscarProducto {

    private List<ProductoDto> productos;

    private RepositorioComponente repositorioComponente;
   @Autowired
    public ServicioBuscarProducto(RepositorioComponente repositorioComponente) {
        this.repositorioComponente = repositorioComponente;
        this.productos = getComponentes();
    }

    public List<ProductoDto> getProductosPorTipo(String tipo) {
        List<ProductoDto> productos = new ArrayList<>();

        List<Componente> productosComponente = repositorioComponente.obtenerComponentesPorTipo(tipo);
        for (Componente componente : productosComponente) {
            productos.add(new ProductoDto(componente));
        }

        return productos;
    }

    public List<ProductoDto> getProductosMenoresAUnPrecio(Double precio) {
        List<ProductoDto> productos = new ArrayList<>();
        List<Componente> productosComponentes = repositorioComponente.obtenerComponentesMenoresDelPrecioPorParametro(precio);
        for(Componente componente : productosComponentes){
            productos.add(new ProductoDto(componente));
        }
       return productos;
    }
    private List<ProductoDto> getComponentes(){
        List<Componente> componentes = repositorioComponente.obtenerComponentes();
        List<ProductoDto> productos = new ArrayList<>();
        for(Componente componente : componentes){
            productos.add(new ProductoDto(componente));
        }
        return productos;
    }
    public List<ProductoDto> getProductos() {
        return productos;
    }

    public List<ProductoDto> getProductosEnStock(){
        List<Componente> componentes = repositorioComponente.obtenerComponentesEnStock();
        List<ProductoDto> productos = new ArrayList<>();
        for(Componente componente : componentes){
            productos.add(new ProductoDto(componente));
        }
        return productos;
    }

    public List<ProductoDto> getProductosPorQuery(String query){
        List<Componente> componentes = repositorioComponente.obtenerComponentesPorQuery(query);
        List<ProductoDto> productos = new ArrayList<>();
        for(Componente componente : componentes){
            productos.add(new ProductoDto(componente));
        }
        return productos;
    }
}
