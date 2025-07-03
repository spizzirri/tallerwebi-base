package com.tallerwebi.presentacion;

public class TarjetaDeCreditoDto {

    private String numeroDeTarjeta;
    private String nombreTitular;
    private String vencimiento;
    private String codigoDeSeguridad;
    private String DNI;


    public TarjetaDeCreditoDto( String numeroDeTarjeta, String vencimiento, String codigoDeSeguridad ) {

        this.numeroDeTarjeta = numeroDeTarjeta;
        this.vencimiento = vencimiento;
        this.codigoDeSeguridad = codigoDeSeguridad;

    }

    public String getNumeroDeTarjeta() {
        return numeroDeTarjeta;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public String getVencimiento() {
        return vencimiento;
    }

    public String getCodigoDeSeguridad() {
        return codigoDeSeguridad;
    }

    public String getDNI() {
        return DNI;
    }
}
