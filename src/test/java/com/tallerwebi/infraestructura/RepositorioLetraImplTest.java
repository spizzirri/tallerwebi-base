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

import com.tallerwebi.dominio.Letra;
import com.tallerwebi.dominio.RepositorioLetra;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestInfraestructuraConfig.class })
public class RepositorioLetraImplTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioLetra repositorioLetra;

    @BeforeEach
    public void init() {
        this.repositorioLetra = new RepositorioLetraImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    @Rollback
    public void deberiaGuardarUnaLetra() {
        // Letra: codigo, contenido, cantidad de versos

        Letra letra = new Letra();
        letra.setCodigo("A1234");
        letra.setContenido("la letra de la cancion");
        letra.setCantidadDeVersos(4);

        this.repositorioLetra.guardar(letra);

        String hql = "FROM Letra WHERE codigo = :codigo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("codigo", "A1234");

        Letra letraObtenida = (Letra) query.getSingleResult();
        // repositorioLetra.buscarPorCodigo("A1234");

        assertThat(letraObtenida, is(equalTo(letra)));
    }

}
