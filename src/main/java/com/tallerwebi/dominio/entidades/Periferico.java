package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Periferico extends Componente {

    /*Caracteristicas Generales*/
    private Integer usbRequeridos;
    @Column(length = 100)
    private String tipoDeConexion;
    private Boolean conexionBluetooth;
    private Boolean receptorBluetoothIncluido;
    private Boolean conexionWireless;
    private Boolean receptorWirelessIncluido;
    @Column(length = 100)
    private String tipoDeCable;
    private Boolean cableExtraible;
    @Column(length = 100)
    private String largoDelCable;

    public Periferico() {}

    public Periferico(Long id, String nombre, Double precio, Integer stock, String marca,
                      Integer usbRequeridos, String tipoDeConexion, Boolean conexionBluetooth, Boolean receptorBluetoothIncluido,
                      Boolean conexionWireless, Boolean receptorWirelessIncluido, String tipoDeCable, Boolean cableExtraible, String largoDelCable) {
        super(id, nombre, precio, stock, marca);

        this.usbRequeridos = usbRequeridos;
        this.tipoDeConexion = tipoDeConexion;
        this.conexionBluetooth = conexionBluetooth;
        this.receptorBluetoothIncluido = receptorBluetoothIncluido;
        this.conexionWireless = conexionWireless;
        this.receptorWirelessIncluido = receptorWirelessIncluido;
        this.tipoDeCable = tipoDeCable;
        this.cableExtraible = cableExtraible;
        this.largoDelCable = largoDelCable;
    }

    public Integer getUsbRequeridos() {
        return usbRequeridos;
    }

    public void setUsbRequeridos(Integer usbRequeridos) {
        this.usbRequeridos = usbRequeridos;
    }

    public String getTipoDeConexion() {
        return tipoDeConexion;
    }

    public void setTipoDeConexion(String tipoDeConexion) {
        this.tipoDeConexion = tipoDeConexion;
    }

    public Boolean getConexionBluetooth() {
        return conexionBluetooth;
    }

    public void setConexionBluetooth(Boolean conexionBluetooth) {
        this.conexionBluetooth = conexionBluetooth;
    }

    public Boolean getReceptorBluetoothIncluido() {
        return receptorBluetoothIncluido;
    }

    public void setReceptorBluetoothIncluido(Boolean receptorBluetoothIncluido) {
        this.receptorBluetoothIncluido = receptorBluetoothIncluido;
    }

    public Boolean getConexionWireless() {
        return conexionWireless;
    }

    public void setConexionWireless(Boolean conexionWireless) {
        this.conexionWireless = conexionWireless;
    }

    public Boolean getReceptorWirelessIncluido() {
        return receptorWirelessIncluido;
    }

    public void setReceptorWirelessIncluido(Boolean receptorWirelessIncluido) {
        this.receptorWirelessIncluido = receptorWirelessIncluido;
    }

    public String getTipoDeCable() {
        return tipoDeCable;
    }

    public void setTipoDeCable(String tipoDeCable) {
        this.tipoDeCable = tipoDeCable;
    }

    public Boolean getCableExtraible() {
        return cableExtraible;
    }

    public void setCableExtraible(Boolean cableExtraible) {
        this.cableExtraible = cableExtraible;
    }

    public String getLargoDelCable() {
        return largoDelCable;
    }

    public void setLargoDelCable(String largoDelCable) {
        this.largoDelCable = largoDelCable;
    }
}
