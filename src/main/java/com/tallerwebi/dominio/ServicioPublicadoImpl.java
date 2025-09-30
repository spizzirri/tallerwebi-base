package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("servicioPublicado")
@Transactional
public class ServicioPublicadoImpl implements ServicioPublicado {

    private RepositorioPublicacion repositorioPublicacion;
    private long nextId = 1;



    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ServicioPublicadoImpl(RepositorioPublicacion repositorioPublicacion){
        this.repositorioPublicacion = repositorioPublicacion;
    }

    // Lista en memoria para almacenar las publicaciones
    private List<Publicacion> publicaciones = new ArrayList<>();

    @Override
    public Publicacion publicacionEntera (String descripcion, Usuario usuario) throws UsuarioExistente {
        return repositorioPublicacion.publicacion(descripcion, usuario);
    }

    @Override
    public void realizar(Publicacion publicacion) throws PublicacionFallida {
        Publicacion publicacionNueva = repositorioPublicacion.buscarPublicacionExistente(publicacion);
        if(publicacionNueva != null){
            throw new PublicacionFallida();
        }
        publicacion.setId(nextId++);

        repositorioPublicacion.realizada(publicacion);
        publicaciones.add(publicacion);
    }

    @Override
    public List<Publicacion> findAll() {
        return publicaciones;
    }
    @Override
    public Publicacion obtenerPublicacionPorId(long id) {
        return publicaciones.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
    }

    @Override
    public int obtenerCantidadDeLikes(long id) {
        Publicacion publiEncontrada = obtenerPublicacionPorId(id);
        return publiEncontrada.getLikes();
    }

}