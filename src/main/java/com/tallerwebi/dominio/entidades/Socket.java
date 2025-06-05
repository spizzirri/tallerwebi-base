package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Socket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nombre;

    @ManyToMany(mappedBy = "sockets")
    private List<Motherboard> motherboards = new ArrayList<>();

    @ManyToMany(mappedBy = "sockets")
    private List<CoolerCPU> coolersCPU = new ArrayList<>();

    public Socket() {}

    public Socket(Long id, String nombre, List<Motherboard> motherboards, List<CoolerCPU> coolersCPU) {
        this.id = id;
        this.nombre = nombre;
        this.motherboards = motherboards;
        this.coolersCPU = coolersCPU;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Motherboard> getMotherboards() {
        return motherboards;
    }

    public void setMotherboards(List<Motherboard> motherboards) {
        this.motherboards = motherboards;
    }

    public List<CoolerCPU> getCoolersCPU() {
        return coolersCPU;
    }

    public void setCoolersCPU(List<CoolerCPU> coolersCPU) {
        this.coolersCPU = coolersCPU;
    }


}
