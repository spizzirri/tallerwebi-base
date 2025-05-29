package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormularioDePagoDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "El correo es obligatorio")
    private String email;
    @NotBlank(message = "El correo repetido es obligatorio")
    private String emailRepetido;
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;
    private String dni;
    private String codigoDescuento;

    public FormularioDePagoDTO() {
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmailRepetido() {
        return emailRepetido;
    }
    public void setEmailRepetido(String emailRepetido) {
        this.emailRepetido = emailRepetido;
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

    public boolean correosCoinciden() {
        if(this.email == null && this.emailRepetido == null){
            return false;
        }
        assert this.email != null;
        return this.email.equals(this.emailRepetido);
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }

    public void setCodigoDescuento(String codigoDescuento) {
        this.codigoDescuento = codigoDescuento;
    }
}

