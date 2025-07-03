package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

    private SessionFactory sessionFactory;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.sessionFactory = sessionFactory;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Override
    public Usuario buscarUsuario(String email) {

        final Session session = sessionFactory.getCurrentSession();
        return (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public Usuario buscarUsuarioPorEmailYPassword(String email, String password) {
        final Session session = sessionFactory.getCurrentSession();
        Usuario encontrado = buscarUsuario(email);
        if (encontrado != null && bCryptPasswordEncoder.matches(password, encontrado.getPassword()) ) {
            return encontrado;
        } else {
            return null;
        }
    }

//    @Override
//    public Usuario buscarUsuarioPorEmailYPassword(String email, String password) {
//        final Session session = sessionFactory.getCurrentSession();
//        return  (Usuario) session.createCriteria(Usuario.class)
//                        .add(Restrictions.eq("email", email))
//                        .add(Restrictions.eq("password", password))
//                        .uniqueResult();
//    }

    @Override
    public void guardar(Usuario usuario) {
        usuario.setPassword(bCryptPasswordEncoder.encode(usuario.getPassword()));
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        return (Usuario) sessionFactory.getCurrentSession().createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public void modificar(Usuario usuario) {

        sessionFactory.getCurrentSession().update(usuario);
    }

    @Override
    public void eliminarUsuario(Long idUsuario) {
        Session session = sessionFactory.getCurrentSession();
    }



}
