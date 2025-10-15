package com.tallerwebi.dominio;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Letra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String codigo;

    private String contenido;
    
    private int cantidadDeVersos;

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setCantidadDeVersos(int cantidadDeVersos) {
        this.cantidadDeVersos = cantidadDeVersos;
    }

}
