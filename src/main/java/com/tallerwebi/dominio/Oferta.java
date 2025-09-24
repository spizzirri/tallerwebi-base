package com.tallerwebi.dominio;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Oferta {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @OneToOne
        @JoinColumn(name = "usuario_id")
        private Usuario ofertadorID;
        private String montoOfertado;
        private LocalDateTime fechaOferta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getOfertadorID() {
        return ofertadorID;
    }

    public void setOfertadorID(Usuario ofertadorID) {
        this.ofertadorID = ofertadorID;
    }

    public LocalDateTime getFechaOferta() {
        return fechaOferta;
    }

    public void setFechaOferta( LocalDateTime fechaOferta) {
        this.fechaOferta = fechaOferta;
    }

    public String getMontoOfertado() {
        return montoOfertado;
    }

    public void setMontoOfertado(String montoOfertado) {
        this.montoOfertado = montoOfertado;
    }
}
