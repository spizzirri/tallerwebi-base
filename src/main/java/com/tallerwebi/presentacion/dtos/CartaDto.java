package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Carta;

public class CartaDto {

    private Long id;
    private String nombre;

    private CartaDto(){}

    public CartaDto(Carta carta){
        this.id = carta.getId();
        this.nombre = carta.getNombre();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Carta obtenerEntidad(){
        Carta carta = new Carta();
        return this.obtenerEntidad(carta);
    }

    public Carta obtenerEntidad(Carta carta){
        carta.setId(this.id);
        carta.setNombre(this.nombre);
        return carta;
    }
}
