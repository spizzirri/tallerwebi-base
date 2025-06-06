package com.tallerwebi.dominio;

import com.tallerwebi.presentacion.ProductoCarritoDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioProductoCarrito {

    private List<ProductoCarritoDto> productos;
    public Double valorTotal = 0.0;
    public Double valorTotalConDescuento = 0.0;

    public void init(){
        this.productos = new ArrayList<ProductoCarritoDto>();
        this.productos.add(new ProductoCarritoDto(1L,"Mouse inalambrico", 8000.000,2,"teclado mecanico"));
        this.productos.add(new ProductoCarritoDto(2L,"Teclado mecanico", 15000.00,2,"teclado mecanico"));
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

    public void setProductos(List<ProductoCarritoDto> productos) {
        this.productos = productos;
    }

    public void limpiarCarrito() {
        this.setProductos(null);
    }
}
