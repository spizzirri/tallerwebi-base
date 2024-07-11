package com.tallerwebi.dominio.perfil;

import com.tallerwebi.dominio.usuario.Usuario;

import javax.persistence.*;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerfil;

    private Integer edad;
    private Double peso; // en kilogramos
    private Integer altura; // en centímetros
    private String genero;
    private String objetivoFitness;
    private String condicionesAlternas;
    private String experienciaEjercicio;
    private String suplementos;
    @Column(columnDefinition = "TEXT")
    private String recomendacion;
    @OneToOne(mappedBy = "perfil")
    private Usuario usuario;

    // Constructor por defecto
    public Perfil() {
    }

    // Constructor con parámetros
    public Perfil(Integer edad, Double peso, Integer altura, String genero,
                  String objetivoFitness, String condicionesAlternas, String experienciaEjercicio,
                  String suplementos) {
        this.edad = edad;
        this.peso = peso;
        this.altura = altura;
        this.genero = genero;
        this.objetivoFitness = objetivoFitness;
        this.condicionesAlternas = condicionesAlternas;
        this.experienciaEjercicio = experienciaEjercicio;
        this.suplementos = suplementos;
    }

    public Perfil(Integer edad, Double peso, Integer altura, String genero, String objetivoFitness, String condicionesAlternas, String experienciaEjercicio, String suplementos, String recomendacion, Usuario usuario) {
        this.edad = edad;
        this.peso = peso;
        this.altura = altura;
        this.genero = genero;
        this.objetivoFitness = objetivoFitness;
        this.condicionesAlternas = condicionesAlternas;
        this.experienciaEjercicio = experienciaEjercicio;
        this.suplementos = suplementos;
        this.recomendacion = recomendacion;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Long idPerfil) {
        this.idPerfil = idPerfil;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getObjetivoFitness() {
        return objetivoFitness;
    }

    public void setObjetivoFitness(String objetivoFitness) {
        this.objetivoFitness = objetivoFitness;
    }

    public String getCondicionesAlternas() {
        return condicionesAlternas;
    }

    public void setCondicionesAlternas(String condicionesAlternas) {
        this.condicionesAlternas = condicionesAlternas;
    }

    public String getExperienciaEjercicio() {
        return experienciaEjercicio;
    }

    public void setExperienciaEjercicio(String experienciaEjercicio) {
        this.experienciaEjercicio = experienciaEjercicio;
    }

    public String getSuplementos() {
        return suplementos;
    }

    public void setSuplementos(String suplementos) {
        this.suplementos = suplementos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    @Override
    public String toString() {
        return "Perfil{" +
                ", edad=" + edad +
                ", peso=" + peso +
                ", altura=" + altura +
                ", genero='" + genero + '\'' +
                ", nivelActividad='" + '\'' +
                ", objetivoFitness='" + objetivoFitness + '\'' +
                ", condicionesAlternas='" + condicionesAlternas + '\'' +
                ", experienciaEjercicio='" + experienciaEjercicio + '\'' +
                ", suplementos='" + suplementos + '\'' +
                '}';
    }
}
