package com.tallerwebi.dominio;

import com.tallerwebi.config.HibernateTestConfig;
import com.tallerwebi.config.SpringWebTestConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class AutoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void debeGuardarElUsuarioAlmismoTiempoQueGuardaElAuto(){
        Auto auto = dadoQueSeGuardaUnAuto();
        List<Usuario> usuarios = cuandoBuscoAlUsuarioDelAutoPorEmail(auto.getUsuario().getEmail());
        entoncesEncuentroAlUsuario(usuarios);
    }

    private Auto dadoQueSeGuardaUnAuto(){
        Usuario usuario = new Usuario("dami@test.com", "123456", "ADMIN", true);
        Auto auto = new Auto(Marca.FIAT, "AA123DD", usuario);
        this.guardarAuto(auto);
        return auto;
    }

    private List<Usuario> cuandoBuscoAlUsuarioDelAutoPorEmail(String email){
        return this.buscarUsuarioPorEmail(email);
    }

    private void entoncesEncuentroAlUsuario(List<Usuario> usuarios){
        assertThat(usuarios, hasSize(1));
    }

    @Transactional
    private void guardarAuto(Auto auto){
        Session session = sessionFactory.openSession();
        session.save(auto);
    }

    @Transactional
    private List<Usuario> buscarUsuarioPorEmail(String email){
        Session session = sessionFactory.openSession();
        Query query = session.createQuery("FROM Usuario u WHERE u.email = :email");
        query.setParameter("email", email);
        List<Usuario> usuarios = (List<Usuario>) query.list();
        return usuarios;
    }
}
