package com.tallerwebi.punta_a_punta.vistas;


import com.microsoft.playwright.Page;
public class VistaRegistrarme extends VistaWeb {

    public VistaRegistrarme(Page page) {
        super(page);
        page.navigate("http://localhost:8080/registrarme");
    }

    public void escribirEMAIL(String email){
        this.escribirEnElElemento("#email", email);
    }

    public void escribirClave(String clave){
        this.escribirEnElElemento("#password", clave);
    }

    public void darClickEnIniciarSesion(){
        this.darClickEnElElemento("#btn-login");
    }
    public void darClickEnRegistarteVistaRegistrarme(){
        this.darClickEnElElemento("#ir-a-registrarme");
    }
}
