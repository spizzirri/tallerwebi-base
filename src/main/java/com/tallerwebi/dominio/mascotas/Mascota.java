package com.tallerwebi.dominio.mascotas;

public class Mascota {
    private Long id;

    public Mascota(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
