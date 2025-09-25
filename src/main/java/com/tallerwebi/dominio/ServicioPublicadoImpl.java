package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.PublicacionFallida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioPublicado")
@Transactional
public class ServicioPublicadoImpl implements ServicioPublicado {

    private RepositorioPublicacion repositorioPublicacion;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ServicioPublicadoImpl(RepositorioPublicacion repositorioPublicacion){
        this.repositorioPublicacion = repositorioPublicacion;
    }

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
        repositorioPublicacion.realizada(publicacion);
    }

}