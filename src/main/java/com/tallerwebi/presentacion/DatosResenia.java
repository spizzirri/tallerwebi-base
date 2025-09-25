package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;

public class DatosResenia {
    private Short calificacion;
    private Long hamburguesaId;
    private String comentario;

    public DatosResenia(){};

    public DatosResenia(Short calificacion, Long hamburguesaId, String comentario) {
        this.calificacion = calificacion;
        this.hamburguesaId = hamburguesaId;
        this.comentario = comentario;
    }

    public Short getCalificacion() {return calificacion;}
    public void setCalificacion(Short calificacion) {this.calificacion = calificacion;}
    public Long getHamburguesaId() {return hamburguesaId;}
    public void setHamburguesaId(Long hamburguesaId) {this.hamburguesaId = hamburguesaId;}
    public String getComentario() {return comentario;}
    public void setComentario(String comentario) {this.comentario = comentario;}
}
