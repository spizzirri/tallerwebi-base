package com.tallerwebi.dominio;

import com.tallerwebi.dominio.RepositorioSubasta;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.RepositorioSubasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioSubasta")
@Transactional
public class ServicioSubastaImpl implements ServicioSubasta {

    private RepositorioSubasta repositorioSubasta;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioSubastaImpl(RepositorioSubasta repositorioSubasta,  RepositorioUsuario repositorioUsuario) {
        this.repositorioSubasta = repositorioSubasta;
        this.repositorioUsuario = repositorioUsuario;
    }


    @Override
    public void crearSubasta(Subasta subasta, String creador) {
        subasta.setCreador(repositorioUsuario.buscar(creador));
        repositorioSubasta.guardar(subasta);
    }
}
