package com.tallerwebi.dominio.reto;

import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioRetoImpl implements ServicioReto{

    private RepositorioReto repositorioReto;

    public ServicioRetoImpl(RepositorioReto repositorioReto){
        this.repositorioReto = repositorioReto;
    }

    @Override
    public Reto obtenerRetoDisponible() {
        return repositorioReto.obtenerYMarcarReto();
    }


}
