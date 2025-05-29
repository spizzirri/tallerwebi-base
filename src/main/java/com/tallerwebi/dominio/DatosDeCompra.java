package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatosDeCompra {

    private Long idCompra;
    private String codigoDeTransaccion;
    private String emailUsuario;
    private LocalDateTime fechaCompra;
    private String estado;
    private List<ProductoCarritoDto> productos;

    public DatosDeCompra(String codigoDeTransaccion, String emailUsuario) {
        this.codigoDeTransaccion = codigoDeTransaccion;
        this.emailUsuario = emailUsuario;
        this.estado = "pendiente";
        this.fechaCompra = LocalDateTime.now();
        this.productos = new ArrayList<ProductoCarritoDto>();
    }

    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public String getCodigoDeTransaccion() {
        return codigoDeTransaccion;
    }

    public void setCodigoDeTransaccion(String codigoDeTransaccion) {
        this.codigoDeTransaccion = codigoDeTransaccion;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<ProductoCarritoDto> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoCarritoDto> productos) {
        this.productos = productos;
    }

    public void agregarProducto(ProductoCarritoDto productoDto){
        this.productos.add(productoDto);
    }
}
