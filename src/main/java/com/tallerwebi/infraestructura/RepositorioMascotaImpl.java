package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.mascotas.Mascota;
import com.tallerwebi.dominio.mascotas.RepositorioMascota;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioMascotaImpl implements RepositorioMascota {
    @Override
    public List<Mascota> obtener() {
        /*List<Mascota> mascotas = new ArrayList<>();
        mascotas.add(new Mascota(1L));
        mascotas.add(new Mascota(2L));
        mascotas.add(new Mascota(3L));
        mascotas.add(new Mascota(4L));
        return mascotas;
        */
         return null;
    }

    @Override
    public Mascota obtener(Long id) {
        //return new Mascota(id);
        return null;
    }
}
