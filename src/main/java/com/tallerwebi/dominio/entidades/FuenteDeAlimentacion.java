package com.tallerwebi.dominio.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class FuenteDeAlimentacion extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String formato;
    @Column(length = 100)
    private String wattsNominales;
    @Column(length = 100)
    private String wattsReales;
    @Column(length = 100)
    private String certificacion80Plus;
    @Column(length = 100)
    private String tipoDeCableado;

    /*Cableado*/
    private Boolean conector24Pines;
    private Integer cantConectoresCpu4Pines;
    private Integer cantConectoresCpu4PinesPlus;
    private Integer cantConectoresCpu6Pines;
    private Integer cantConectoresCpu2PinesPlus;
    private Integer cantConexionesSata;
    private Integer cantConexionesMolex;
    private Integer cantConexionesPcie16Pines;



    public FuenteDeAlimentacion() {}

    public FuenteDeAlimentacion(Long id, String nombre, Double precio, Integer stock, String marca,
                                String formato, String wattsNominales, String wattsReales, String certificacion80Plus, String tipoDeCableado,
                                Boolean conector24Pines, Integer cantConectoresCpu4Pines, Integer cantConectoresCpu4PinesPlus, Integer cantConectoresCpu6Pines,
                                Integer cantConectoresCpu2PinesPlus, Integer cantConexionesSata, Integer cantConexionesMolex, Integer cantConexionesPcie16Pines) {
        super(id, nombre, precio, stock, marca);

        this.formato = formato;
        this.wattsNominales = wattsNominales;
        this.wattsReales = wattsReales;
        this.certificacion80Plus = certificacion80Plus;
        this.tipoDeCableado = tipoDeCableado;

        this.conector24Pines = conector24Pines;
        this.cantConectoresCpu4Pines = cantConectoresCpu4Pines;
        this.cantConectoresCpu4PinesPlus = cantConectoresCpu4PinesPlus;
        this.cantConectoresCpu6Pines = cantConectoresCpu6Pines;
        this.cantConectoresCpu2PinesPlus = cantConectoresCpu2PinesPlus;
        this.cantConexionesSata = cantConexionesSata;
        this.cantConexionesMolex = cantConexionesMolex;
        this.cantConexionesPcie16Pines = cantConexionesPcie16Pines;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getWattsNominales() {
        return wattsNominales;
    }

    public void setWattsNominales(String wattsNominales) {
        this.wattsNominales = wattsNominales;
    }

    public String getWattsReales() {
        return wattsReales;
    }

    public void setWattsReales(String wattsReales) {
        this.wattsReales = wattsReales;
    }

    public String getCertificacion80Plus() {
        return certificacion80Plus;
    }

    public void setCertificacion80Plus(String certificacion80Plus) {
        this.certificacion80Plus = certificacion80Plus;
    }

    public String getTipoDeCableado() {
        return tipoDeCableado;
    }

    public void setTipoDeCableado(String tipoDeCableado) {
        this.tipoDeCableado = tipoDeCableado;
    }

    public Boolean getConector24Pines() {
        return conector24Pines;
    }

    public void setConector24Pines(Boolean conector24Pines) {
        this.conector24Pines = conector24Pines;
    }

    public Integer getCantConectoresCpu4Pines() {
        return cantConectoresCpu4Pines;
    }

    public void setCantConectoresCpu4Pines(Integer cantConectoresCpu4Pines) {
        this.cantConectoresCpu4Pines = cantConectoresCpu4Pines;
    }

    public Integer getCantConectoresCpu4PinesPlus() {
        return cantConectoresCpu4PinesPlus;
    }

    public void setCantConectoresCpu4PinesPlus(Integer cantConectoresCpu4PinesPlus) {
        this.cantConectoresCpu4PinesPlus = cantConectoresCpu4PinesPlus;
    }

    public Integer getCantConectoresCpu6Pines() {
        return cantConectoresCpu6Pines;
    }

    public void setCantConectoresCpu6Pines(Integer cantConectoresCpu6Pines) {
        this.cantConectoresCpu6Pines = cantConectoresCpu6Pines;
    }

    public Integer getCantConectoresCpu2PinesPlus() {
        return cantConectoresCpu2PinesPlus;
    }

    public void setCantConectoresCpu2PinesPlus(Integer cantConectoresCpu2PinesPlus) {
        this.cantConectoresCpu2PinesPlus = cantConectoresCpu2PinesPlus;
    }

    public Integer getCantConexionesSata() {
        return cantConexionesSata;
    }

    public void setCantConexionesSata(Integer cantConexionesSata) {
        this.cantConexionesSata = cantConexionesSata;
    }

    public Integer getCantConexionesMolex() {
        return cantConexionesMolex;
    }

    public void setCantConexionesMolex(Integer cantConexionesMolex) {
        this.cantConexionesMolex = cantConexionesMolex;
    }

    public Integer getCantConexionesPcie16Pines() {
        return cantConexionesPcie16Pines;
    }

    public void setCantConexionesPcie16Pines(Integer cantConexionesPcie16Pines) {
        this.cantConexionesPcie16Pines = cantConexionesPcie16Pines;
    }
}
