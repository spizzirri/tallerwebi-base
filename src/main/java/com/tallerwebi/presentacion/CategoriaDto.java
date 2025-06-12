package com.tallerwebi.presentacion;

public class CategoriaDto {
    private String nombre;
    private String imagen;
    private String descripcion;
    public CategoriaDto(String nombre, String descripcion, String imagen) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;} ;
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getImagen() {return imagen;}
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
