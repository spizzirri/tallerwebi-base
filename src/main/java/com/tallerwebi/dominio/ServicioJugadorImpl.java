package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("servicioJugador")
@Transactional
public class ServicioJugadorImpl implements ServicioJugador {
    private final RepositorioJugador repositorioJugador;

    @Autowired
    public ServicioJugadorImpl(RepositorioJugador repositorioJugador) {
        this.repositorioJugador = repositorioJugador;
    }

    @Override
    public List<Anuncio> getAnuncios() {
        List<Anuncio> anuncios = new ArrayList<Anuncio>();
        anuncios.add(new Anuncio().setTitle("Release 0.0.1-ALPHA").setContent("Anuncio inicial mockeado"));
        anuncios.add(new Anuncio().setTitle("Release 0.0.2-ALPHA").setContent("Segundo update mockeado, mostrar arriba de todo"));
        return anuncios;
    }

    @Override
    public Jugador getJugadorActual() {
        return this.repositorioJugador.buscar(1L);
    }
}
