package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Jugador;
import com.tallerwebi.dominio.Objeto;
import com.tallerwebi.dominio.ObjetoInventario;
import com.tallerwebi.dominio.RepositorioJugador;
import com.tallerwebi.dominio.enums.SubtipoObjeto;
import com.tallerwebi.dominio.enums.TipoObjeto;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository("repositorioJugador")
public class RepositorioJugadorImpl implements RepositorioJugador {
    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioJugadorImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Jugador buscar(Long jugadorId) {
        Jugador jugador = new Jugador();
        jugador.setNombre("MockPlayer");

        Objeto objetoMock = new Objeto();
        objetoMock.setNombre("Pocion de Curacion");
        objetoMock.setTipo(TipoObjeto.CONSUMIBLE);
        objetoMock.setSubtipo(SubtipoObjeto.POCION);
        objetoMock.setRango(2);
        objetoMock.setValor(1000L);

        ObjetoInventario objetoInventarioMock = new ObjetoInventario();
        objetoInventarioMock.setObjeto(objetoMock);
        objetoInventarioMock.setCantidad(3);

        ArrayList<ObjetoInventario> inventarioMock = new ArrayList<>();
        inventarioMock.add(objetoInventarioMock);

        jugador.setObjetos(inventarioMock);
        return jugador;
    }

    @Override
    public void guardar(Jugador jugador) {
        this.sessionFactory.getCurrentSession().save(jugador);
    }

    @Override
    public void modificar(Jugador jugador) {
        this.sessionFactory.getCurrentSession().update(jugador);
    }
}
