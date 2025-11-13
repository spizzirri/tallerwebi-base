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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.tallerwebi.dominio.Cancion;
import com.tallerwebi.dominio.Letra;
import com.tallerwebi.dominio.RepositorioCancion;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestInfraestructuraConfig.class })
public class RepositorioCancionImplTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioCancion repositorioCancion;

    @BeforeEach
    public void init() {
        this.repositorioCancion = new RepositorioCancionImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    public void deberiaGuardarUnaCancionConLetraAsociada() {
        // Cancion: titulo, duracion (en segundos), Letra
        Letra letra = this.dadoQueExisteUnaLetra();
        Cancion cancion = this.dadoQueTenemosUnaCancion(letra);

        this.repositorioCancion.guardar(cancion);

        this.entoncesPuedoObtenerLaCancion(cancion);
    }

    private void entoncesPuedoObtenerLaCancion(Cancion cancion) {
        String hql = "SELECT c FROM Cancion c INNER JOIN c.letra WHERE c.letra.codigo = :codigo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("codigo", "A1234");

        Cancion cancionObtenida = (Cancion) query.getSingleResult();

        assertThat(cancionObtenida, is(equalTo(cancion)));
    }

    private Cancion dadoQueTenemosUnaCancion(Letra letra) {
        Cancion cancion = new Cancion();
        cancion.setTitulo("Mi cancion");
        cancion.setDuracion(400);
        cancion.setLetra(letra);
        return cancion;
    }

    private Letra dadoQueExisteUnaLetra() {
        Letra letra = new Letra();
        letra.setCodigo("A1234");
        letra.setContenido("la letra de la cancion");
        letra.setCantidadDeVersos(4);
        this.sessionFactory.getCurrentSession().save(letra);
        return letra;
    }

}
