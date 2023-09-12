package com.tallerwebi.dominio.mascotas;

import java.util.List;

public interface RepositorioMascota {
    List<Mascota> obtener();

    Mascota obtener(Long i);
}
