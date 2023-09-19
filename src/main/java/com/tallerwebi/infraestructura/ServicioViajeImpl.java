package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.ServicioViaje;
import com.tallerwebi.dominio.Viaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioViajeImpl implements ServicioViaje {

    @Autowired
    private RepositorioViaje repositorioViaje;

    public ServicioViajeImpl(RepositorioViaje repositorioViaje) {
        this.repositorioViaje = repositorioViaje;
    }

    @Override
    public List<Viaje> obtenerViajes() {
        return repositorioViaje.listarViajes();
    }

    @Override
    public List<Viaje> obtenerViajesPorDestino(String destino) {
        return repositorioViaje.buscarPorDestino(destino);
    }

    @Override
    public List<Viaje> obtenerViajesPorOrigen(String origen) {
        return repositorioViaje.buscarPorOrigen(origen);
    }

    @Override
    public List<Viaje> obtenerViajesPorFecha(String fecha) {
        return repositorioViaje.buscarPorFecha(fecha);
    }

    @Override
    public List<Viaje> obtenerViajesPorFiltroMultiple(String origen, String destino, String fecha) {
        return repositorioViaje.buscarPorOrigenDestinoYfecha(origen,destino,fecha);
    }
}
