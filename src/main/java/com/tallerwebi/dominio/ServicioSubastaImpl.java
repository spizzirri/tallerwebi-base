package com.tallerwebi.dominio;

import com.tallerwebi.dominio.RepositorioSubasta;
import com.tallerwebi.dominio.ServicioSubasta;
import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.RepositorioSubasta;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.Base64;
import java.util.List;

@Service("servicioSubasta")
@Transactional
public class ServicioSubastaImpl implements ServicioSubasta {

    private RepositorioSubasta repositorioSubasta;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioCategorias repositorioCategorias;

    @Autowired
    public ServicioSubastaImpl(RepositorioSubasta repositorioSubasta,  RepositorioUsuario repositorioUsuario, RepositorioCategorias repositorioCategorias) {
        this.repositorioSubasta     = repositorioSubasta;
        this.repositorioUsuario     = repositorioUsuario;
        this.repositorioCategorias  = repositorioCategorias;
    }

    @Override
    public void crearSubasta(Subasta subasta, String creador, MultipartFile imagen) throws IOException {
        if(creador == null || creador.isEmpty()){
            throw new RuntimeException("Usuario no definido.");
        }

        if(imagen == null || imagen.isEmpty()){
            throw new RuntimeException("Imagen no definida.");
        }

        if(     subasta.getEstadoSubasta() != 0 &&
                subasta.getEstadoSubasta() != 1 &&
                subasta.getEstadoSubasta() != 2 &&
                subasta.getEstadoSubasta() != 3){
            throw new RuntimeException("Categoria no definida.");
        }

        subasta.setCreador(repositorioUsuario.buscar(creador));
        subasta.setImagen(Base64.getEncoder().encodeToString(imagen.getBytes()));
        subasta.setFechaInicio();
        subasta.setFechaFin(repositorioSubasta.obtenerTiempoFin(subasta.getEstadoSubasta()));   //Subasta en curso
        subasta.setEstadoSubasta(10);
        repositorioSubasta.guardar(subasta);
    }

    @Override
    public List<Categoria> listarCategoriasDisponibles() {
        return repositorioCategorias.listarCategorias();
    }

    @Override
    public Subasta buscarSubasta(Long idSubasta) {return repositorioSubasta.obtenerSubasta(idSubasta);}

}
