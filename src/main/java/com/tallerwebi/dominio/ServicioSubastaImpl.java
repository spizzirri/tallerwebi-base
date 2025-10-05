package com.tallerwebi.dominio;

import com.tallerwebi.dominio.RepositorioSubasta;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.RepositorioSubasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioSubasta")
@Transactional
public class ServicioSubastaImpl implements ServicioSubasta {

    private RepositorioSubasta repositorioSubasta;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioCategorias repositorioCategorias;

    @Autowired
    public ServicioSubastaImpl(RepositorioSubasta repositorioSubasta,  RepositorioUsuario repositorioUsuario, RepositorioCategorias repositorioCategorias) {
        this.repositorioSubasta = repositorioSubasta;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCategorias = repositorioCategorias;
    }

    @Override
    public void crearSubasta(Subasta subasta, String creador) {
        subasta.setCreador(repositorioUsuario.buscar(creador));
        subasta.setFechaInicio();
        subasta.setFechaFin(repositorioSubasta.obtenerTiempoFin(subasta.getEstadoSubasta()));   //Subasta en curso
        subasta.setEstadoSubasta(10);
        repositorioSubasta.guardar(subasta);
    }

    @Override
    public List<Categorias> listarCategoriasDisponibles() {
        return repositorioCategorias.listarCategorias();
    }

}
