package com.tallerwebi.dominio;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Viaje {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String descripcion;
    private  Integer cantidad;
    private  String fecha_hora;
    private  String destino;
    //TO DO: Que el atributo id_usuario sea unique .
    private  Long id_usuario;

    private String origen;

    public Viaje(Long id, String origen, String destino, String fecha_hora, Integer cantidad, String descripcion, Long creador) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fecha_hora = fecha_hora;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        this.id_usuario =  creador;
    }

    public Viaje(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getFecha_hora() {
        return this.fecha_hora;
    }

    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getDestino() {
        return this.destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Long getId() {
        return this.id;
    }

    public String getOrigen() {
        return this.origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public Long getIdUsuario() {
        return this.id_usuario;
    }
}
