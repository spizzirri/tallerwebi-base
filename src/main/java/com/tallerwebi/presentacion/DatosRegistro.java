package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.Usuario;

public class DatosRegistro {

  private String nombre;
  private String apellido;
  private String dni;
  private String telefono;
  private String email;
  private String password;
  private Mascota mascota = new Mascota();
  private Long planId;

  public Long getPlanId() {
    return planId;
  }

  public void setPlanId(Long planId) {
    this.planId = planId;
  }

  public Socio crearSocio() {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword(password);

    Socio socio = new Socio();
    socio.setNombre(nombre);
    socio.setApellido(apellido);
    socio.setDni(dni);
    socio.setTelefono(telefono);
    socio.setUsuario(usuario);
    return socio;
  }

  public Mascota crearMascota() {
    return mascota;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getDni() {
    return dni;
  }

  public void setDni(String dni) {
    this.dni = dni;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
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

  public Mascota getMascota() {
    return mascota;
  }

  public void setMascota(Mascota mascota) {
    this.mascota = mascota;
  }
}
