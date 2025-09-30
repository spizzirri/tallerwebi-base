package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioPublicacionImpl implements RepositorioPublicacion {

    private List<Publicacion> publicaciones = new ArrayList<>(); // ejemplo en memoria

    @Override
    public Publicacion publicacion(String descripcion, Usuario usuario) throws UsuarioExistente {
        Publicacion p = new Publicacion();
       /* if (buscarPublicacionExistente(p) != null) {  // CREA P VACIO
            throw new UsuarioExistente();
        }
        publicaciones.add(p);
        return p; */
        p.setDescripcion(descripcion);
        p.setUsuario(usuario);

        if (buscarPublicacionExistente(p) != null) {
            throw new UsuarioExistente();
        }

        // Agregar la publicación a la lista
        publicaciones.add(p);

        // Devolver la publicación creada
        return p;
    }

    @Override
    public void realizada(Publicacion publicacion) {
        // marcar como realizada, por ejemplo agregando a lista
        publicaciones.add(publicacion);
    }

    @Override
    public Publicacion buscarPublicacionExistente(Publicacion publicacion) {
        // buscar en la lista por igualdad (puede ser por id o contenido)
        return publicaciones.stream()
                .filter(p -> p.equals(publicacion))
                .findFirst()
                .orElse(null);
    }





}
