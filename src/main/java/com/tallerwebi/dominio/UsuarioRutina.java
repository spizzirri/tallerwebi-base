package com.tallerwebi.dominio;

import com.tallerwebi.dominio.rutina.Rutina;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "usuario_rutina")
public class UsuarioRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "rutina_id")
    private Rutina rutina;

    @Column(name = "fecha_inicio")
    private Date fechaInicio;

    @Column(name = "activo", columnDefinition = "boolean default false")
    private boolean activo;

    public UsuarioRutina() {
    }

    public UsuarioRutina(Usuario usuario, Rutina rutina) {
        this.usuario = usuario;
        this.rutina = rutina;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
}