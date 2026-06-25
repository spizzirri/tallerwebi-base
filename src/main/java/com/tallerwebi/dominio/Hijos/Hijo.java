package com.tallerwebi.dominio.Hijos;

import com.tallerwebi.dominio.Usuario.Usuario;
import java.util.Date;
import javax.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Hijo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false, unique = true)
  private Long dni;

  @Column
  private String fotoPerfil;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column
  private Date fechaNac;

  //Hijos * -------1 padre
  @ManyToOne
  @JoinColumn(name = "idPadre")
  private Usuario padre;

  @Column(nullable = false)
  private String apellido;

  @Column(unique = true)
  private String aliasRetiro;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Curso curso;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFotoPerfil() {
    return fotoPerfil;
  }

  public void setFotoPerfil(String fotoPerfil) {
    this.fotoPerfil = fotoPerfil;
  }

  public Date getFechaNac() {
    return fechaNac;
  }

  public void setFechaNac(Date fechaNac) {
    this.fechaNac = fechaNac;
  }

  public Curso getCurso() {
    return curso;
  }

  public void setCurso(Curso curso) {
    this.curso = curso;
  }

  public Usuario getPadre() {
    return padre;
  }

  public void setPadre(Usuario padre) {
    this.padre = padre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getAliasRetiro() {
    return aliasRetiro;
  }

  public void setAliasRetiro(String aliasRetiro) {
    this.aliasRetiro = aliasRetiro;
  }

  public Long getDni() {
    return dni;
  }

  public void setDni(Long dni) {
    this.dni = dni;
  }
}
