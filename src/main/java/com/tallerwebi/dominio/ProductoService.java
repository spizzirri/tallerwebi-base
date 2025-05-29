package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoService {

    private final List<ProductoCarritoDto> productos;
    public Double valorTotal = 0.0;
    public Double valorTotalConDescuento = 0.0;

    @Autowired
    public ProductoService(RepositorioUsuario repositorioUsuario){
        this.productos = new ArrayList<ProductoCarritoDto>();
        this.productos.add(new ProductoCarritoDto("1","Mouse inalambrico", 29.99,2,"teclado mecanico"));
        this.productos.add(new ProductoCarritoDto("2","Teclado mecanico", 79.99,2,"teclado mecanico"));

    }

    public Double calcularValorTotalDeLosProductos() {
        Double total = 0.0;
        for(ProductoCarritoDto productoCarritoDto : this.productos){
            total += productoCarritoDto.getPrecio() * productoCarritoDto.getCantidad();
        }

        BigDecimal valorTotalConDosDecimales = new BigDecimal(total);
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP); //convierto el numero para que tenga dos decimales y redondee para arriba
        this.valorTotal = valorTotalConDosDecimales.doubleValue();

        return this.valorTotal;
    }

    public List<ProductoCarritoDto> getProductos() {
        return productos;
    }

    public Double calcularDescuento(Integer codigoDescuentoExtraido){
        Double total = this.calcularValorTotalDeLosProductos();
        this.valorTotalConDescuento = total;

        switch (codigoDescuentoExtraido) {
            case 5:
                this.valorTotalConDescuento = total - (total * 0.05);
                break;
            case 10:
                this.valorTotalConDescuento = total - (total * 0.10);
                break;
            case 15:
                this.valorTotalConDescuento = total - (total * 0.15);
                break;
        }

        BigDecimal valorTotalConDosDecimales = new BigDecimal( this.valorTotalConDescuento);
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP);
        this.valorTotalConDescuento = valorTotalConDosDecimales.doubleValue();

        return this.valorTotalConDescuento;
    }

}
