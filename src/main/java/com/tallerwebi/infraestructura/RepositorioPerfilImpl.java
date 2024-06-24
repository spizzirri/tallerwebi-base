package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.perfil.Perfil;
import com.tallerwebi.dominio.perfil.RepositorioPerfil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository("repositorioPerfil")
public class RepositorioPerfilImpl implements RepositorioPerfil {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioPerfilImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public Perfil obtenerPerfilPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Perfil.class, id);
    }

    @Override
    @Transactional
    public void guardarPerfil(Perfil perfil) {
        Session session = sessionFactory.getCurrentSession();
        session.save(perfil);
    }

    @Override
    @Transactional
    public void actualizarPerfil(Perfil perfilActualizado) {
        Session session = sessionFactory.getCurrentSession();
        session.update(perfilActualizado);
    }



}
