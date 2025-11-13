package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Orden orden;
    private Integer cantidad;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setNombre(String mouse) {
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Orden getOrden() {
       return orden;

    }
}
