package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Socio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String apellido;

  @Column(unique = true)
  private String dni;

  private String telefono;

  // Las credenciales (email y password) viven en Usuario para que Socio y
  // Veterinaria compartan el mismo login.
  @OneToOne(cascade = CascadeType.ALL)
  private Usuario usuario;

  @OneToMany(mappedBy = "socio", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Mascota> mascotas = new ArrayList<>();

  public void agregarMascota(Mascota mascota) {
    mascota.setSocio(this);
    mascotas.add(mascota);
  }

  public Mascota buscarMascota(Long mascotaId) {
    return mascotas.stream().filter(m -> m.getId().equals(mascotaId)).findFirst().orElse(null);
  }

  public Mascota buscarPorIdParaSocio(Socio socio, Long mascotaId) {
    if (socio == null || mascotaId == null) return null;
    return socio.buscarMascota(mascotaId);
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

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public List<Mascota> getMascotas() {
    return mascotas;
  }
}
