package com.tallerwebi.dominio;

import java.util.ArrayList;

public class ServicioHamburgueserias {
    public ArrayList<Hamburgueseria> obtenerHamburgueseriasCercanas(Double latitud, Double longitud) {
        ArrayList<Hamburgueseria> hamburgueserias = new ArrayList<>();
        hamburgueserias.add(crearHamburgueseriaMock(1L, "Burger King", -34.6037, -58.3816, 4.2));
        hamburgueserias.add(crearHamburgueseriaMock(2L, "McDonald's", -34.6040, -58.3820, 4.0));
        hamburgueserias.add(crearHamburgueseriaMock(3L, "Mostaza", -34.6050, -58.3830, 3.8));
        hamburgueserias.add(crearHamburgueseriaMock(4L, "Wendy's", -34.6060, -58.3840, 4.5));
        hamburgueserias.add(crearHamburgueseriaMock(5L, "Carl's Jr.", -34.6070, -58.3850, 4.1));
        hamburgueserias.add(crearHamburgueseriaMock(6L, "Hard Rock Cafe", -34.6080, -58.3860, 4.3));
        hamburgueserias.add(crearHamburgueseriaMock(7L, "El Club de la Milanesa", -34.6090, -58.3870, 4.0));
        hamburgueserias.add(crearHamburgueseriaMock(8L, "La Birra Bar", -34.6100, -58.3880, 4.7));
        hamburgueserias.add(crearHamburgueseriaMock(9L, "Dean & Dennys", -34.6110, -58.3890, 3.9));
        hamburgueserias.add(crearHamburgueseriaMock(10L, "Pony Line", -34.6120, -58.3900, 4.4));

        return hamburgueserias;
    }

    private Hamburgueseria crearHamburgueseriaMock(Long id, String nombre, double latitud, double longitud, double puntuacion) {
        Hamburgueseria h = new Hamburgueseria();
        h.setId(id);
        h.setNombre(nombre);
        h.setLatitud(latitud);
        h.setLongitud(longitud);
        h.setPuntuacion(puntuacion);
        return h;
    }
}
