package com.tallerwebi.dominio.mascotas;
import com.tallerwebi.infraestructura.RepositorioMascotaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioMascotaImpl implements ServicioMascota {

    private final RepositorioMascota repositorioMascota;

    @Autowired
    public ServicioMascotaImpl(RepositorioMascota repositorioMascota){
        this.repositorioMascota =  repositorioMascota;
    }

    @Override
    public List<Mascota> obtener() {
        return repositorioMascota.obtener();
    }

    @Override
    public Mascota obtener(Long id) {
        return repositorioMascota.obtener(id);
    }

}
