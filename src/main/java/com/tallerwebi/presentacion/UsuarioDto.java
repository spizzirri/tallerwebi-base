package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;

public class UsuarioDto {
    
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String dni;
    private String rol;
    private String password;
    
    public UsuarioDto(String nombre, String apellido, String email, String telefono, String dni, String rol, String password) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
        this.rol = rol;
        this.password = password;
    }

    public UsuarioDto(){};

    public String getPassword(){
        return password;
    }
    public String getRol(){
        return rol;
    }
    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
