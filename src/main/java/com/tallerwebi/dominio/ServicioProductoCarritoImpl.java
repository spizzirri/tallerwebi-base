package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.presentacion.ProductoCarritoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service("servicioProductoCarrito")
@Transactional
public class ServicioProductoCarritoImpl {

    private List<ProductoCarritoDto> productos;
    public Double valorTotal = 0.0;
    public Double valorTotalConDescuento = 0.0;

    @Autowired
    private RepositorioComponente repositorioComponente;

    public void init(){
        this.productos = new ArrayList<ProductoCarritoDto>();
//        this.productos.add(new ProductoCarritoDto(1L,"Mouse inalambrico", 8000.000,2,"teclado mecanico"));
//        this.productos.add(new ProductoCarritoDto(2L,"Teclado mecanico", 15000.00,2,"teclado mecanico"));
    }

    public void inicializarSiEsNecesario() {
        if (this.productos == null) {
            this.productos = new ArrayList<>();
        }
    }

    public ProductoCarritoDto buscarPorId(Long id) {
        for (ProductoCarritoDto productoCarritoDto : this.getProductos()) {
            if (productoCarritoDto.getId().equals(id)) {
                return productoCarritoDto;
            }
        }
        return null;
    }

    public void agregarProducto(Long componenteId, Integer cantidad) {
        if(this.productos == null){
            this.productos = new ArrayList<>();
        }

        ProductoCarritoDto existente = this.buscarPorId(componenteId);
        if(existente != null){
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            Componente componente = repositorioComponente.obtenerComponentePorId(componenteId);

            if (componente != null) {
                ProductoCarritoDto nuevoProductoCarrito = new ProductoCarritoDto(componente, cantidad);
                this.productos.add(nuevoProductoCarrito);
            }
        }
    }

    public boolean verificarStock(Long componenteId, Integer cantidadDeseada) {
        Componente componente = repositorioComponente.obtenerComponentePorId(componenteId);
        return componente.getStock() >= cantidadDeseada;
    }

    public Double calcularValorTotalDeLosProductos() {
        if (this.productos == null || this.productos.isEmpty()) {
            this.valorTotal = 0.0;
            return this.valorTotal;
        }

        Double total = 0.0;
        for(ProductoCarritoDto producto : this.productos){
            if (producto.getPrecio() != null && producto.getCantidad() != null) {
                total += producto.getPrecio() * producto.getCantidad();
            }
        }

        BigDecimal valorTotalConDosDecimales = new BigDecimal(total);
        valorTotalConDosDecimales = valorTotalConDosDecimales.setScale(2, RoundingMode.UP);
        this.valorTotal = valorTotalConDosDecimales.doubleValue();

        return this.valorTotal;
    }

    public List<ProductoCarritoDto> getProductos() {
        inicializarSiEsNecesario();
        return productos;
    }

    public Double calcularDescuento(Integer codigoDescuentoExtraido) {
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

    public Integer calcularCantidadTotalDeProductos() {
        if (this.productos == null || this.productos.isEmpty()) {
            return 0;
        }

        Integer cantidadTotal = 0;
        for (ProductoCarritoDto producto : this.productos) {
            cantidadTotal += producto.getCantidad();
        }
        return cantidadTotal;
    }
}
