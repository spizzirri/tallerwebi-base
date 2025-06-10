package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class CoolerCPU extends Componente {

    /*Caracteristicas generales*/
    @Column(length = 100)
    private String consumo;
    @Column(length = 100)
    private String tdpPredeterminado;
    @Column(length = 100)
    private String tipoDeDisipacion;

    /*Compatibilidad*/
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cooler_socket",
            joinColumns = @JoinColumn(name = "cooler_id"),
            inverseJoinColumns = @JoinColumn(name = "socket_id")
    )
    private Set<Socket> sockets = new HashSet<>();

    /*Coolers y Disipadores*/
    private Integer cantCoolersIncluidos;
    @Column(length = 100)
    private String tamanioCoolers;
    @Column(length = 100)
    private String tipoDeIlumninacion;
    @Column(length = 100)
    private String nivelMaximoDeRuido;

    public CoolerCPU() {}

    public CoolerCPU(Long id, String nombre, Double precio, Integer stock, String marca,
                        String consumo, String tdpPredeterminado, String tipoDeDisipacion,
                        Set<Socket> sockets,
                        Integer cantCoolersIncluidos, String tamanioCoolers, String tipoDeIlumninacion, String nivelMaximoDeRuido) {
        super(id, nombre, precio, stock, marca);

        this.consumo = consumo;
        this.tdpPredeterminado = tdpPredeterminado;
        this.tipoDeDisipacion = tipoDeDisipacion;

        this.sockets = sockets;

        this.cantCoolersIncluidos = cantCoolersIncluidos;
        this.tamanioCoolers = tamanioCoolers;
        this.tipoDeIlumninacion = tipoDeIlumninacion;
        this.nivelMaximoDeRuido = nivelMaximoDeRuido;
    }

    public String getConsumo() {
        return consumo;
    }

    public void setConsumo(String consumo) {
        this.consumo = consumo;
    }

    public String getTdpPredeterminado() {
        return tdpPredeterminado;
    }

    public void setTdpPredeterminado(String tdpPredeterminado) {
        this.tdpPredeterminado = tdpPredeterminado;
    }

    public String getTipoDeDisipacion() {
        return tipoDeDisipacion;
    }

    public void setTipoDeDisipacion(String tipoDeDisipacion) {
        this.tipoDeDisipacion = tipoDeDisipacion;
    }

    public Set<Socket> getSockets() {
        return sockets;
    }

    public void setSockets(Set<Socket> sockets) {
        this.sockets = sockets;
    }

    public Integer getCantCoolersIncluidos() {
        return cantCoolersIncluidos;
    }

    public void setCantCoolersIncluidos(Integer cantCoolersIncluidos) {
        this.cantCoolersIncluidos = cantCoolersIncluidos;
    }

    public String getTamanioCoolers() {
        return tamanioCoolers;
    }

    public void setTamanioCoolers(String tamanioCoolers) {
        this.tamanioCoolers = tamanioCoolers;
    }

    public String getTipoDeIlumninacion() {
        return tipoDeIlumninacion;
    }

    public void setTipoDeIlumninacion(String tipoDeIlumninacion) {
        this.tipoDeIlumninacion = tipoDeIlumninacion;
    }

    public String getNivelMaximoDeRuido() {
        return nivelMaximoDeRuido;
    }

    public void setNivelMaximoDeRuido(String nivelMaximoDeRuido) {
        this.nivelMaximoDeRuido = nivelMaximoDeRuido;
    }


}
