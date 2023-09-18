package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioViaje {
    List<Viaje> obtenerViajes();

    List<Viaje> obtenerViajesPorDestino(String destino);

    List<Viaje> obtenerViajesPorOrigen(String origen);

    List<Viaje> obtenerViajesPorFecha(String fecha);
}
