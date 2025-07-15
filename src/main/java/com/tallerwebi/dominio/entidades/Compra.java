package com.tallerwebi.dominio.entidades;

import com.tallerwebi.dominio.Usuario;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompra;

    @Column(length = 100)
    private LocalDate fecha;

    @Column(length = 100)
    private Double total;

    @Column(length = 100)
    private String metodoDePago;

    @Column(length = 100)
    private String cp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    private Usuario idUsuario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraComponente> productosComprados = new ArrayList<>();

    public void setIdCompra(Long idCompra) {
        this.idCompra = idCompra;
    }

    public String getCp() {return cp;}

    public void setCp(String cp) {this.cp = cp;}

    public Long getIdCompra() {
        return idCompra;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setTotal(Double total) {
        this.total = total;
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


}
