package com.tallerwebi.dominio;

import javax.persistence.*;

@Entity
public class Resenia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long hamburguesaId;
    private short calificacion;
    private String comentario;

    public Resenia(Long hamburguesaId, short calificacion, String comentario) {
        this.hamburguesaId = hamburguesaId;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public Resenia() {

    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getHamburguesaId() {return hamburguesaId;}
    public void setHamburguesaId(Long hamburguesaId) {this.hamburguesaId = hamburguesaId;}
    public short getCalificacion() {return calificacion;}
    public void setCalificacion(short calificacion) {this.calificacion = calificacion; }
    public String getComentario() {return comentario;}
    public void setComentario(String comentario) {this.comentario = comentario;}
}

