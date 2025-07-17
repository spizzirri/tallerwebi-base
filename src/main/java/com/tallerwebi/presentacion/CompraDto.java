package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Compra;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CompraDto {

    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private String metodoDePago;
    private Long idUsuario;
    private List<CompraComponenteDto> productosComprados;
    private String cp;
    private String formaEntrega;
    private Double costoDeEnvio;
    private String moneda;
    private Double totalDolar;
    private String documento;
    private String nombreTitular;
    private String valorConDescuento;
    private Double totalConDescuentoDolar;

    public CompraDto(Compra compra) {
        this.id = compra.getIdCompra();
        this.fecha = LocalDateTime.now();
        this.total = compra.getTotal();
        this.metodoDePago = compra.getMetodoDePago();
        this.idUsuario = compra.getIdUsuario().getId();
        this.cp = compra.getCp();
        this.productosComprados = compra.getProductosComprados().stream()
                .map(CompraComponenteDto::new)
                .collect(Collectors.toList());
        this.formaEntrega = compra.getFormaEntrega();
        this.costoDeEnvio = compra.getCostoDeEnvio();
        this.moneda = compra.getMoneda();
        this.totalDolar = compra.getTotalDolar();
        this.documento = compra.getDocumento();
        this.nombreTitular = compra.getNombreTitular();
        this.valorConDescuento = compra.getValorConDescuento();
        this.totalConDescuentoDolar = compra.getTotalConDescuentoDolar();
    }

    public CompraDto() {
    }

    public List<CompraComponenteDto> getProductosComprados() {
        return productosComprados;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public Double getTotal() {
        return total;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setProductosComprados(List<CompraComponenteDto> productosComprados) {
        this.productosComprados = productosComprados;
    }

    public void setFormaEntrega(String formaEntrega) {
        this.formaEntrega = formaEntrega;
    }

    public String getFormaEntrega() {
        return formaEntrega;
    }

    public String getCp() {
        return this.cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public Double getCostoDeEnvio() {
        return costoDeEnvio;
    }

    public void setCostoDeEnvio(Double costoDeEnvio) {
        this.costoDeEnvio = costoDeEnvio;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public void setTotalDolar(Double totalCompraEnDolares) {
        this.totalDolar = totalCompraEnDolares;
    }

    public Double getTotalDolar() {
        return totalDolar;
    }

    public void setValorConDescuento(String valorConDescuento) {
        this.valorConDescuento = valorConDescuento;
    }

    public String getValorConDescuento() {
        return valorConDescuento;
    }

    public void setTotalConDescuentoDolar(Double totalConDescuentoDolar) {
        this.totalConDescuentoDolar = totalConDescuentoDolar;
    }

    public Double getTotalConDescuentoDolar() {
        return totalConDescuentoDolar;
    }
}
