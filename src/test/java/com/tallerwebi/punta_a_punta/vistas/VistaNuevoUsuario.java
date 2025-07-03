package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaNuevoUsuario extends VistaWeb{
    public VistaNuevoUsuario(Page page) {
        super(page);
    }

    public void ir() {
        page.navigate("http://localhost:8080/nuevo-usuario");
    }

    public void escribirEMAIL(String email){
        this.escribirEnElElemento("#email", email);
    }

    public void escribirClave(String clave){
        this.escribirEnElElemento("#password", clave);
    }

    public void escribirNOMBRE(String nombre){
        this.escribirEnElElemento("#nombre", nombre);
    }

    public void escribirAPELLIDO(String apellido){
        this.escribirEnElElemento("#apellido", apellido);
    }

    public void escribirTELEFONO(String telefono){
        this.escribirEnElElemento("#telefono", telefono);
    }

    public void escribirDNI(String dni){
        this.escribirEnElElemento("#dni", dni);
    }

    public void darClickEnRegistrarme(){
        this.darClickEnElElemento("#btn-registrarme");
    }

    public String obtenerMensajeDeError(){
        return this.obtenerTextoDelElemento("#mensajeErrorRegistrarse");
    }

}
