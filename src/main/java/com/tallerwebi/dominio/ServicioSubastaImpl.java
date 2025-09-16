package com.tallerwebi.dominio;

import com.tallerwebi.dominio.RepositorioSubasta;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioSubasta")
@Transactional
public class ServicioSubastaImpl implements ServicioSubasta {

    private RepositorioSubasta repositorioSubasta;

    @Autowired
    public ServicioSubastaImpl(RepositorioSubasta repositorioSubasta){this.repositorioSubasta = repositorioSubasta;}

    @Override
    public void crearSubasta(Subasta subasta){
        repositorioSubasta.guardar(subasta);
    }
}
