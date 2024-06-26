package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;

import javax.persistence.*;

@Entity
public class EstadoEjercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Ejercicio ejercicio;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    public enum Estado {
        COMPLETO,
        INCOMPLETO,
        NO_EMPEZADO
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicio ejercicio) {
        this.ejercicio = ejercicio;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}