package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.tallerwebi.dominio.RepositorioCarta;
import com.tallerwebi.dominio.entidades.Carta;
import com.tallerwebi.dominio.entidades.Tipo;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
@Transactional
public class RepositorioCartaTest {
    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioCarta repositorioCarta;

    @BeforeEach
    public void init() {
        this.repositorioCarta = new RepositorioCartaImpl(this.sessionFactory);
    }

    @Test
    @Rollback
    public void cuandoCreoUnaCartaConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos(){
        Tipo tipo = new Tipo();
        tipo.setNombre("Rayo");
        this.sessionFactory.getCurrentSession().save(tipo);
        
        Carta carta = new Carta();
        carta.setNombre("MiCarta");
        carta.setTipo(tipo);

        Boolean guardada = this.repositorioCarta.crear(carta);

        String hql = "FROM Carta WHERE nombre = :nombre";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "MiCarta");
        Carta obtenida = (Carta)query.getSingleResult();

        assertThat(guardada, is(true));
        assertThat(obtenida, equalTo(carta));
    }

    @Test
    @Rollback
    public void dadoQueExisteUnaCartaEnLaBDCuandoLaObtengoPorIdMeDevuelveLaCartaCorrespondiente(){
        Carta carta = new Carta();
        carta.setNombre("MiCarta");
        this.sessionFactory.getCurrentSession().save(carta);

        String hql = "FROM Carta WHERE nombre = :nombre";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "MiCarta");
        Carta guardada = (Carta)query.getSingleResult();

        Carta obtenida = this.repositorioCarta.obtenerPor(guardada.getId());

        assertThat(obtenida, equalTo(carta));
    }
}
