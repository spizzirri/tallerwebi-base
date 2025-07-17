package com.tallerwebi.dominio.entidades;

import com.tallerwebi.dominio.Usuario;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompra;

    @Column(length = 100)
    private LocalDateTime fecha;

    @Column(length = 100)
    private Double total;

    @Column(length = 100)
    private String metodoDePago;

    @Column(length = 100)
    private String cp;

    @Column(length = 100)
    private String formaEntrega;

    @Column(length = 100)
    private String nombreTitular;

    @Column(length = 100)
    private String documento;

    @Column(length = 100)
    private Double costoDeEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    private Usuario idUsuario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraComponente> productosComprados = new ArrayList<>();

    @Column(length = 20)
    private String moneda;

    @Column(length = 100)
    private Double totalDolar;

    @Column(length = 100)
    private String valorConDescuento;

    @Column(length = 100)
    private Double totalConDescuentoDolar;

    public Compra() {}

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public Long getIdCompra() {
        return idCompra;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public Double getTotal() {
        return total;
    }

    public void setMetodoDePago(String metodoDePago) {
        this.metodoDePago = metodoDePago;
    }

    public String getMetodoDePago() {
        return metodoDePago;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setProductosComprados(List<CompraComponente> productosComprados) {
        this.productosComprados = productosComprados;
    }

    public List<CompraComponente> getProductosComprados() {
        return productosComprados;
    }

    public void setFormaEntrega(String formaEntrega) {
        this.formaEntrega = formaEntrega;
    }

    public String getFormaEntrega() {
        return formaEntrega;
    }

    public void setCostoDeEnvio(Double costoDeEnvio) {
        this.costoDeEnvio = costoDeEnvio;
    }

    public Double getCostoDeEnvio() {
        return costoDeEnvio;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setTotalDolar(Double totalDolar) {
        this.totalDolar = totalDolar;
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
