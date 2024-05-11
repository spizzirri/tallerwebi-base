package com.tallerwebi.dominio.objetivo;

import com.tallerwebi.dominio.Usuario;

import javax.persistence.*;

@Entity
public class ObjetivoUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String objetivo;


    public void setUsuario(Usuario usuario) {
    }

    public void setObjetivo(String objetivo) {

    }

    public void setId(long l) {
    }

    public Long getId() {
        return 0L;
    }
}