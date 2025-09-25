package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioHamburgueserias;
import com.tallerwebi.dominio.Hamburgueseria;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioHamburgueserias")
public class RepositorioHamburgueseriasImpl implements RepositorioHamburgueserias {

    @Autowired
    public RepositorioHamburgueseriasImpl() {
    }

    @Override
    public List<Hamburgueseria> buscarHamburgueseriasCercanas(Double latitud, Double longitud) {
        List<Hamburgueseria> hamburgueserias = new java.util.ArrayList<Hamburgueseria>();
        hamburgueserias.add(crearHamburgueseriaMock(1L, "Burger King", -34.6037, -58.3816, 4.2, true));
        hamburgueserias.add(crearHamburgueseriaMock(2L, "McDonald's", -34.6040, -58.3820, 4.0, true));
        hamburgueserias.add(crearHamburgueseriaMock(3L, "Mostaza", -34.6050, -58.3830, 3.8, true));
        hamburgueserias.add(crearHamburgueseriaMock(4L, "Wendy's", -34.6060, -58.3840, 4.5, true));
        hamburgueserias.add(crearHamburgueseriaMock(5L, "Carl's Jr.", -34.6070, -58.3850, 4.1, false));
        hamburgueserias.add(crearHamburgueseriaMock(6L, "Hard Rock Cafe", -34.6080, -58.3860, 4.3, false));
        hamburgueserias.add(crearHamburgueseriaMock(7L, "El Club de la Milanesa", -34.6090, -58.3870, 4.0, false));
        hamburgueserias.add(crearHamburgueseriaMock(8L, "La Birra Bar", -34.6100, -58.3880, 4.7, false));
        hamburgueserias.add(crearHamburgueseriaMock(9L, "Dean & Dennys", -34.6110, -58.3890, 3.9, false));
        hamburgueserias.add(crearHamburgueseriaMock(10L, "Pony Line", -34.6120, -58.3900, 4.4, false));

        return hamburgueserias;
    }

    private Hamburgueseria crearHamburgueseriaMock(Long id, String nombre, double latitud, double longitud,
            double puntuacion, boolean esComercioAdherido) {
        Hamburgueseria h = new Hamburgueseria();
        h.setId(id);
        h.setNombre(nombre);
        h.setLatitud(latitud);
        h.setLongitud(longitud);
        h.setPuntuacion(puntuacion);
        h.setDireccion("Direccion de " + nombre);
        h.setEsComercioAdherido(esComercioAdherido);
        return h;
    }
}
