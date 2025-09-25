package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;

public class DatosPublicacion {
    private Usuario usuario;
    private String descripcion;

    public DatosPublicacion() {
    }

    public DatosPublicacion(Usuario usuario, String descripcion) {
        this.usuario = usuario;
        this.descripcion = descripcion;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}

