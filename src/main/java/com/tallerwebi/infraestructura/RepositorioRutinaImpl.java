package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.UsuarioRutina;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

        // Create a new UsuarioRutina entity
        UsuarioRutina usuarioRutina = new UsuarioRutina();
        usuarioRutina.setUsuario(usuarioAAsignar);
        usuarioRutina.setRutina(rutinaAAsignar);
        usuarioRutina.setFechaInicio(new Date());
        usuarioRutina.setActivo(true);

        // Persist the UsuarioRutina entity
        this.sessionFactory.getCurrentSession().save(usuarioRutina);
    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        this.sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public List<Rutina> getRutinasDeUsuario(Usuario usuario) {
        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario.id = :idUsuario";
        Query<Rutina> query = this.sessionFactory.getCurrentSession().createQuery(hql, Rutina.class);
        query.setParameter("idUsuario", usuario.getId());

        return query.getResultList();
    }

    @Override
    public Rutina buscarRutinaEnUsuario(Rutina rutina, Usuario usuario) {
        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario.id = :idUsuario AND ur.rutina.idRutina = :idRutina";
        Query<Rutina> query = this.sessionFactory.getCurrentSession().createQuery(hql, Rutina.class);
        query.setParameter("idUsuario", usuario.getId());
        query.setParameter("idRutina", rutina.getIdRutina());

        try {
            return query.getSingleResult();
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
        String hql = "DELETE FROM UsuarioRutina ur WHERE ur.rutina.idRutina = :idRutina AND ur.usuario.id = :idUsuario";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idRutina", rutina.getIdRutina());
        query.setParameter("idUsuario", usuario.getId());
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

    @Override
    public Rutina getRutinaActivaDelUsuario(Usuario usuario) {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario.id = :userId AND ur.fechaInicio BETWEEN :startOfDay AND :endOfDay AND activo = 1";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, Rutina.class);
        query.setParameter("userId", usuario.getId());
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);

        return (Rutina) query.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public void liberarRutinaActivaDelUsuario(Usuario usuario) {
        Usuario usuarioBuscado = this.getUsuarioPorId(usuario.getId());
        // Obtiene la rutina activa del usuario
        Rutina rutinaActiva = this.getRutinaActivaDelUsuario(usuarioBuscado);

        // Busca la instancia de UsuarioRutina asociada al usuario y a la rutina activa
        UsuarioRutina usuarioRutinaBuscado = this.buscarUsuarioRutinaPorUsuarioYRutina(usuarioBuscado, rutinaActiva);

        // Verifica si se encontró la instancia de UsuarioRutina
        if (usuarioRutinaBuscado != null) {
            // Establece el campo "activo" en 0
            usuarioRutinaBuscado.setActivo(false);

            // Guarda los cambios en la base de datos
            this.actualizarUsuarioRutina(usuarioRutinaBuscado);
        }
    }

    @Override
    public UsuarioRutina buscarUsuarioRutinaPorUsuarioYRutina(Usuario usuario, Rutina rutina) {
        String hql = "SELECT ur FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.rutina = :rutina";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, UsuarioRutina.class);
        query.setParameter("usuario", usuario);
        query.setParameter("rutina", rutina);

        return (UsuarioRutina) query.uniqueResult();
    }

    @Override
    public void actualizarUsuarioRutina(UsuarioRutina usuarioRutina) {
        this.sessionFactory.getCurrentSession().update(usuarioRutina);
    }

}
