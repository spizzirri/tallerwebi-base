package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.SubtipoObjeto;
import com.tallerwebi.dominio.enums.TipoObjeto;

import javax.persistence.*;

@Entity
public class Objeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private TipoObjeto tipo;
    private SubtipoObjeto subtipo;
    private Integer rango;
    private Long valor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoObjeto getTipo() {
        return tipo;
    }

    public void setTipo(TipoObjeto tipo) {
        this.tipo = tipo;
    }

    public SubtipoObjeto getSubtipo() {
        return subtipo;
    }

    public void setSubtipo(SubtipoObjeto subtipo) {
        this.subtipo = subtipo;
    }

    public Integer getRango() {
        return rango;
    }

    public void setRango(Integer rango) {
        this.rango = rango;
    }

    public Long getValor() {
        return valor;
    }

    public void setValor(Long valor) {
        this.valor = valor;
    }
}
