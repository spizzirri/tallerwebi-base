package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carrera;

import java.util.List;

public class DatosUsuario {

    private String nombre;
    private String apellido;
    private List<Carrera> carreras;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre= nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido= apellido;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }


    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }
}


