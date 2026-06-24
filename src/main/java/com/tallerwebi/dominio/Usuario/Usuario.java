package com.tallerwebi.dominio.Usuario;

import com.tallerwebi.dominio.Hijos.Hijo;
import java.util.List;
import javax.persistence.*;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = true)
  private String password;

  @Embedded
  private DatosPersonales datosPersonales = new DatosPersonales();

  @Column(nullable = false)
  private String rol;

  @Column(nullable = false)
  private Boolean activo = false;

  //Usuario 1 -------- * Hijos
  @OneToMany(mappedBy = "padre")
  private List<Hijo> hijos;

  //getter y setter
  public List<Hijo> getHijos() {
    return hijos;
  }

  public void setHijos(List<Hijo> hijos) {
    this.hijos = hijos;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DatosPersonales getDatosPersonales() {
    return datosPersonales;
  }

  public void setDatosPersonales(DatosPersonales datosPersonales) {
    this.datosPersonales = datosPersonales;
  }

  public Long getDni() {
    return datosPersonales.getDni();
  }

  public void setDni(Long dni) {
    datosPersonales.setDni(dni);
  }

  public String getNombre() {
    return datosPersonales.getNombre();
  }

  public void setNombre(String nombre) {
    datosPersonales.setNombre(nombre);
  }

  public String getApellido() {
    return datosPersonales.getApellido();
  }

  public void setApellido(String apellido) {
    datosPersonales.setApellido(apellido);
  }

  public Long getCelular() {
    return datosPersonales.getCelular();
  }

  public void setCelular(Long celular) {
    datosPersonales.setCelular(celular);
  }

  public String getFotoPerfil() {
    return datosPersonales.getFotoPerfil();
  }

  public void setFotoPerfil(String foto) {
    datosPersonales.setFotoPerfil(foto);
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public void activar() {
    activo = true;
  }
}
