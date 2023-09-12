package com.tallerwebi.dominio.mascotas;
import java.util.List;

public interface ServicioMascota {
    List<Mascota> obtener();

    Mascota obtener(Long id);
}
