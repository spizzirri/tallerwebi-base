package com.tallerwebi.dominio;

public class MensajeEnviado {
    private String content;

    public MensajeEnviado() {
    }

    public MensajeEnviado(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
