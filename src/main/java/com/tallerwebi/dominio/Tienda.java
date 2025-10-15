package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Tienda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    // Relación unidireccional Many-to-Many
    @ManyToMany
    private Set<Producto> productos = new HashSet<Producto>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
