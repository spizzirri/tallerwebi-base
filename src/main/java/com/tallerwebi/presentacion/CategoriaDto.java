package com.tallerwebi.presentacion;

public class CategoriaDto {
    private String nombre;
    private String imagen;

    public CategoriaDto(String nombre, String imagen) {
        this.nombre = nombre;
        this.imagen = imagen;
    }

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getImagen() {return imagen;}
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
