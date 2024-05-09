package com.tallerwebi.dominio;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String password;
    private String rol;
    private Boolean activo = false;
    private Objetivo objetivo;
    private Boolean isInstructor = false;
    public Usuario() {

    }
    public Usuario(String nombre, Objetivo objetivo) {
        this.nombre = nombre;
        this.objetivo = objetivo;
    }

    public Usuario(String nombre, String apellido, String email, String password) {
        this.nombre = nombre;
        this.apellido =  apellido;
        this.email = email;
        this.password = password;
    }

    public Usuario(String nombre, String apellido, String email, String password, Boolean isInstructor) {
        this.nombre = nombre;
        this.apellido =  apellido;
        this.email = email;
        this.password = password;
        this.isInstructor = isInstructor;
    }

    public Usuario(String nombre, String apellido, String email, String password, Objetivo objetivo) {
        this.nombre = nombre;
        this.apellido =  apellido;
        this.email = email;
        this.password = password;
        this.objetivo = objetivo;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
    public Boolean getActivo() {
        return activo;
    }
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public boolean activo() {
        return activo;
    }

    public void activar() {
        activo = true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }


    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    public Boolean isInstructor() {
        return isInstructor;
    }

}