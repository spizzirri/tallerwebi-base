package com.tallerwebi.presentacion;

public class DatosLoginDto {
    private String email;
    private String password;

    public DatosLoginDto() {
    }

    public DatosLoginDto(String email, String password) {
        this.email = email;
        this.password = password;
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
}

