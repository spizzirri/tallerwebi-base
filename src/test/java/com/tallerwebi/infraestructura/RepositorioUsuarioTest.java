package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.usuario.RepositorioUsuario;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.infraestructura.config.HibernateTestInfraestructuraConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestInfraestructuraConfig.class})
public class RepositorioUsuarioTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioUsuario repositorioUsuario;

    @BeforeEach
    public void init(){
        this.repositorioUsuario = new RepositorioUsuarioImpl(this.sessionFactory);
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUnEspacioEnLaBaseDeDatosQueSePuedaGuardarUnUsuario() {
        // preparación
        Session session = sessionFactory.getCurrentSession();
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("password");

        // ejecución
        repositorioUsuario.guardar(usuario);

        // verificación
        Usuario usuarioGuardado = (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", "test@example.com"))
                .uniqueResult();

        assertNotNull(usuarioGuardado);
        assertEquals("test@example.com", usuarioGuardado.getEmail());
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUsuariosGuardadosQueSePuedaBuscarUnUsuarioPorEmailYPassword() {
        // preparación
        Session session = sessionFactory.getCurrentSession();
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPassword("password");

        session.save(usuario);

        session = sessionFactory.getCurrentSession();

        // ejecución
        Usuario usuarioBuscado = repositorioUsuario.buscarUsuario("test@example.com", "password");

        // verificación
        assertNotNull(usuarioBuscado);
        assertEquals("test@example.com", usuarioBuscado.getEmail());
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUsuariosGuardadosQueSePuedaBuscarUnUsuarioPorEmail() {
        // preparación
        Session session = sessionFactory.getCurrentSession();
        Usuario usuario = new Usuario();
        usuario.setEmail("test2@example.com");
        usuario.setPassword("password");

        session.save(usuario);

        session = sessionFactory.getCurrentSession();

        // ejecución
        Usuario usuarioBuscado = repositorioUsuario.buscar("test2@example.com");

        // verificación
        assertNotNull(usuarioBuscado);
        assertEquals("test2@example.com", usuarioBuscado.getEmail());
    }

    @Test
    @Transactional
    @Rollback
    public void dadoQueExisteUsuariosGuardadosQueSePuedaModificarUnUsuario() {
        // preparación
        Session session = sessionFactory.getCurrentSession();
        Usuario usuario = new Usuario();
        usuario.setEmail("test3@example.com");
        usuario.setPassword("password");

        session.save(usuario);

        session = sessionFactory.getCurrentSession();
        usuario.setPassword("newpassword");

        // ejecución
        repositorioUsuario.modificar(usuario);

        // verificación
        Usuario usuarioModificado = (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", "test3@example.com"))
                .uniqueResult();

        assertNotNull(usuarioModificado);
        assertEquals("newpassword", usuarioModificado.getPassword());
    }

}
