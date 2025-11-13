package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany (mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<Item>();
    private String descripcion;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void agregarItem(Item item) {
      this.items.add(item);
      item.setOrden(this);
    }

    public List<Item> getItems() {
        return items;
    }

    public void eliminarItem(Item itemAEliminar) {
        this.items.remove(itemAEliminar);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Orden orden = (Orden) o;
        return Objects.equals(descripcion, orden.descripcion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(descripcion);
    }
}
