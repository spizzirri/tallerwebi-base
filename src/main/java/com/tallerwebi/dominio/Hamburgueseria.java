package com.tallerwebi.dominio;

public class Hamburgueseria {
    private Long id;
	private double puntuacion;
	private double latitud;
	private double longitud;
	private String nombre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
	public double getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(double puntuacion) {
		this.puntuacion = puntuacion;
	}

	public double getLatitud() {
		return latitud;
	}

	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
