package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Socket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nombre;

    @OneToMany(mappedBy = "socket")
    private List<Procesador> procesadores = new ArrayList<>();

    @OneToMany(mappedBy = "socket")
    private List<Motherboard> motherboards = new ArrayList<>();

    @ManyToMany(mappedBy = "sockets")
    private Set<CoolerCPU> coolersCPU = new HashSet<>();

    public Socket() {}

    public Socket(Long id, String nombre, List<Procesador> procesadores ,List<Motherboard> motherboards, Set<CoolerCPU> coolersCPU) {
        this.id = id;
        this.nombre = nombre;
        this.procesadores = procesadores;
        this.motherboards = motherboards;
        this.coolersCPU = coolersCPU;
    }

    public Socket(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.procesadores = new ArrayList<>();
        this.motherboards = new ArrayList<>();
        // los many to many deberian ser Set segun el profe (corregir)
        this.coolersCPU = new HashSet<>();
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

    public List<Procesador> getProcesadores() {
        return procesadores;
    }

    public void setProcesadores(List<Procesador> procesadores) {
        this.procesadores = procesadores;
    }


    public List<Motherboard> getMotherboards() {
        return motherboards;
    }

    public void setMotherboards(List<Motherboard> motherboards) {
        this.motherboards = motherboards;
    }

    public Set<CoolerCPU> getCoolersCPU() {
        return coolersCPU;
    }

    public void setCoolersCPU(Set<CoolerCPU> coolersCPU) {
        this.coolersCPU = coolersCPU;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
