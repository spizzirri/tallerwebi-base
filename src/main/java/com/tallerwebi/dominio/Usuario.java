package com.tallerwebi.dominio;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String rol;
    private Boolean activo = false;


    // Auxiliar para el formulario
    @Transient //no persiste en bdd
    private List<String> carrerasNombres = new ArrayList<>();
    /*La otra lista de carreras que tenés en Usuario —la que es List<Carrera> y
    se persiste en la base de datos— no la tocás
    en el formulario. Esa lista se completa a partir de los nombres que vienen por el form
     */


    /*******AGREGADOS*****/
    private String nombre;
    private String apellido;
    private Integer dni;
    /*publicaciones del usuario**/
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)  /*si una publi queda sin padre usuario, se borra la publi*/
    private List<Publicacion> publicaciones = new ArrayList<>();

    /*publicaciones guardadas del usuario**/
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_publicaciones_guardadas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "publicacion_id")
    )
    private List<Publicacion> publicacionesGuardadas= new ArrayList<>();

    /*relacion con likes*/
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likesDados = new ArrayList<>();



    /*relacion con carrera*/
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_carrera",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "carrera_id")
    )
    private List<Carrera> carreras = new ArrayList<>();



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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

    public boolean activo() {
        return activo;
    }

    public void activar() {
        activo = true;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre(){
        return nombre;
    }
    public void setApellido(String apellido) {
        this.apellido =apellido;
    }
    public String getApellido(){
        return apellido;
    }


  public void setDni(Integer dni) {
        this.dni = dni;
  }
  public Integer getDni() {
        return dni;
  }

  public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
  }

  public List<Carrera> getCarreras() {
        return carreras;
  }

    public void darLike(Like like) {
        if (!likesDados.contains(like)) {
            likesDados.add(like);
        }
    }


  /*auxiliares*/

    public List<String> getCarrerasNombres() {
        return carrerasNombres;
    }

    public void setCarrerasNombres(List<String> carrerasNombres) {
        this.carrerasNombres = carrerasNombres;
    }

}
