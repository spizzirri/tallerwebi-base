package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaCalendario extends VistaWeb {

    public VistaCalendario(Page page) {
        super(page);
        page.navigate("localhost:8081/spring/calendario");
    }

    public String obtenerTituloDelCalendario(){
        return this.obtenerTextoDelElemento(".titulo-calendario");
    }

    public void seleccionarFecha(String fecha) {
        this.escribirEnElElemento("#fecha", fecha);
    }

    public void agregarEvento(String evento) {
        this.escribirEnElElemento("#evento", evento);
    }

    public void darClickEnAgregarEvento() {
        this.darClickEnElElemento("#btn-agregar-evento");
    }

    public String obtenerMensajeDeConfirmacion() {
        return this.obtenerTextoDelElemento("p.alert.alert-success");
    }
}
