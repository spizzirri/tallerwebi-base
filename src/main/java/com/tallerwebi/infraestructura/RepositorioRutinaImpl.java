package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioRutinaImpl implements RepositorioRutina {
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    public RepositorioRutinaImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public RepositorioRutinaImpl(){
    }
    @Override
    public List<Rutina> getRutinas() {

        String hql = "FROM Rutina";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return (List<Rutina>) query.getResultList();
    }

    @Override
    public Rutina getRutinaByObjetivo(Objetivo objetivo) {
        String hql = "FROM Rutina WHERE objetivo = :objetivo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("objetivo",objetivo);

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public List<Rutina> getRutinasByObjetivo(Objetivo objetivo) {

        String hql = "FROM Rutina WHERE objetivo = :objetivo";
        return sessionFactory.getCurrentSession().createQuery(hql, Rutina.class)
                .setParameter("objetivo", objetivo)
                .list();
    }

    @Override
    public Rutina getRutinaParaUsuario(Usuario usuario) {
        String hql = "FROM Rutina WHERE objetivo = :objetivo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("objetivo",usuario.getObjetivo());

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void guardarRutina(Rutina rutina) {
        this.sessionFactory.getCurrentSession().save(rutina);
    }

    @Override
    public void guardarEjercicio(Ejercicio ejercicio) {
        this.sessionFactory.getCurrentSession().save(ejercicio);
    }

    @Override
    public void guardarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina) {
        Rutina rutinaAAsignar = this.buscarRutinaPorId(rutina.getIdRutina());
        Ejercicio ejercicioAAsignar = this.getEjercicioById(ejercicio.getIdEjercicio());

        rutinaAAsignar.getEjercicios().add(ejercicioAAsignar);
        this.sessionFactory.getCurrentSession().save(rutinaAAsignar);
    }

    @Override
    public void actualizar(Rutina rutina) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(rutina);
    }

    @Override
    public void eliminarRutina(Rutina rutina) {
        sessionFactory.getCurrentSession().delete(rutina);
    }

    @Override
    public void actualizar(Ejercicio ejercicio) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(ejercicio);
    }

    @Override
    public void eliminarEjercicio(Ejercicio ejercicio) {
        sessionFactory.getCurrentSession().delete(ejercicio);
    }

    @Override
    public Ejercicio buscarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina) {
        String hql = "SELECT e FROM Rutina r JOIN r.ejercicios e WHERE r.id = :rutinaId AND e.id = :ejercicioId";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("rutinaId",rutina.getIdRutina());
        query.setParameter("ejercicioId",ejercicio.getIdEjercicio());

        try {
            return (Ejercicio) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void asignarRutinaAUsuario(Rutina rutina, Usuario usuario) {
        Rutina rutinaAAsignar = this.buscarRutinaPorId(rutina.getIdRutina());
        Usuario usuarioAAsignar = this.getUsuarioPorId(usuario.getId());

        usuarioAAsignar.getRutinas().add(rutinaAAsignar);
        this.sessionFactory.getCurrentSession().update(usuarioAAsignar);
    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        this.sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public List<Rutina> getRutinasDeUsuario(Usuario usuario) {
        String hql = "SELECT r FROM Usuario u JOIN u.rutinas r WHERE u.id = :idUsuario ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idUsuario",usuario.getId());

        return (List<Rutina>) query.getResultList();

    }

    @Override
    public Rutina buscarRutinaEnUsuario(Rutina rutina, Usuario usuario) {
        String hql = "SELECT r FROM Usuario u JOIN u.rutinas r WHERE u.id = :idUsuario AND r.idRutina = :idRutina ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idUsuario",usuario.getId());
        query.setParameter("idRutina",rutina.getIdRutina());

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void eliminarEjercicioDeRutina(Ejercicio ejercicio, Rutina rutina) {
        String hql = "DELETE FROM Rutina r WHERE :ejercicio MEMBER OF r.ejercicios AND r.idRutina = :rutinaId";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("ejercicio", ejercicio);
        query.setParameter("rutinaId", rutina.getIdRutina());
        query.executeUpdate();
    }

    @Override
    public void eliminarRutinaDeUsuario(Rutina rutina, Usuario usuario) {
        String hql = "DELETE FROM Usuario u WHERE :rutina MEMBER OF u.rutinas AND u.id = :usuarioId";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("rutina", rutina);
        query.setParameter("usuarioId", usuario.getId());
        query.executeUpdate();
    }

    @Override //falta testear
    public Ejercicio buscarEjercicioPorId(Long idEjercicio) {
        String hql = "FROM Ejercicio WHERE idEjercicio = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id",idEjercicio);

        try {
            return (Ejercicio) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Rutina buscarRutinaPorId(Long idRutina) {
        String hql = "FROM Rutina WHERE idRutina = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id",idRutina);

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Rutina> getRutinasPorObjetivoDeUsuario(Usuario usuario) {
        String hql = "FROM Rutina WHERE objetivo = :objetivo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("objetivo",usuario.getObjetivo());


        return (List<Rutina>) query.getResultList();

    }

    @Override
    public Usuario getUsuarioPorId(Long id) {
        String hql = "FROM Usuario WHERE id = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id",id);

        return (Usuario) query.getSingleResult();

    }

    @Override
    public List<Ejercicio> getEjerciciosDeRutina(Rutina rutina) {
        String hql = "SELECT e FROM Rutina r JOIN r.ejercicios e WHERE r.idRutina = :rutinaId";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("rutinaId", rutina.getIdRutina());
        return (List<Ejercicio>)query.getResultList();
    }

    @Override
    public Rutina getRutinaActualDeLUsuario(Usuario usuario) {
        return null;
    }

    @Override
    public Ejercicio getEjercicioById(Long idEjercicio) {
        String hql = "FROM Ejercicio WHERE idEjercicio = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id",idEjercicio);

        return (Ejercicio) query.getSingleResult();
    }



}
