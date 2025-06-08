package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Motherboard extends Componente {

    /*Caracteristicas generales*/
    @ManyToOne
    @JoinColumn(name = "socket_id")
    private Socket socket;
    @Column(length = 100)
    private String chipsetPrincipal;
    @Column(length = 100)
    private String plataforma;
    @Column(length = 100)
    private String factor; // ATX - Micro-ATX - ETC

    /*Conectividad*/
    private Integer cantSlotsM2;
    private Integer cantPuertosSata;
    private Integer cantPuertosUSB;

    /*Memoria*/
    @Column(length = 100)
    private String tipoMemoria;
    private Integer cantSlotsRAM;

    /*Energia*/
    @Column(length = 100)
    private String consumo;
    private Integer cantConector24Pines;
    private Integer cantConector4Pines;

    public Motherboard() {}

    public Motherboard(Long id, String nombre, Double precio, Integer stock, String marca,
                        Socket socket, String chipsetPrincipal, String plataforma, String factor,
                        Integer cantSlotsM2, Integer cantPuertosSata, Integer cantPuertosUSB,
                        String tipoMemoria, Integer cantSlotsRAM,
                        String consumo, Integer cantConector24Pines, Integer cantConector4Pines) {
        super(id, nombre, precio, stock, marca);

        this.socket = socket;
        this.chipsetPrincipal = chipsetPrincipal;
        this.plataforma = plataforma;
        this.factor = factor;

        this.cantSlotsM2 = cantSlotsM2;
        this.cantPuertosSata = cantPuertosSata;
        this.cantPuertosUSB = cantPuertosUSB;

        this.tipoMemoria = tipoMemoria;
        this.cantSlotsRAM = cantSlotsRAM;

        this.consumo = consumo;
        this.cantConector24Pines = cantConector24Pines;
        this.cantConector4Pines = cantConector4Pines;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getChipsetPrincipal() {
        return chipsetPrincipal;
    }

    public void setChipsetPrincipal(String chipsetPrincipal) {
        this.chipsetPrincipal = chipsetPrincipal;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public Integer getCantSlotsM2() {
        return cantSlotsM2;
    }

    public void setCantSlotsM2(Integer cantSlotsM2) {
        this.cantSlotsM2 = cantSlotsM2;
    }

    public Integer getCantPuertosSata() {
        return cantPuertosSata;
    }

    public void setCantPuertosSata(Integer cantPuertosSata) {
        this.cantPuertosSata = cantPuertosSata;
    }

    public Integer getCantPuertosUSB() {
        return cantPuertosUSB;
    }

    public void setCantPuertosUSB(Integer cantPuertosUSB) {
        this.cantPuertosUSB = cantPuertosUSB;
    }

    public String getTipoMemoria() {
        return tipoMemoria;
    }

    public void setTipoMemoria(String tipoMemoria) {
        this.tipoMemoria = tipoMemoria;
    }

    public Integer getCantSlotsRAM() {
        return cantSlotsRAM;
    }

    public void setCantSlotsRAM(Integer cantSlotsRAM) {
        this.cantSlotsRAM = cantSlotsRAM;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public Integer getCantConector24Pines() {
        return cantConector24Pines;
    }

    public void setCantConector24Pines(Integer cantConector24Pines) {
        this.cantConector24Pines = cantConector24Pines;
    }

    public Integer getCantConector4Pines() {
        return cantConector4Pines;
    }

    public void setCantConector4Pines(Integer cantConector4Pines) {
        this.cantConector4Pines = cantConector4Pines;
    }


}
