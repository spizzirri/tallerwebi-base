package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.EjercicioNoExisteEnRutinaException;
import com.tallerwebi.dominio.excepcion.RutinaNoEncontradaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoTieneLaRutinaBuscadaException;
import com.tallerwebi.dominio.excepcion.UsuarioSinRutinasException;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioRutinaImpl implements RepositorioRutina {
    private List<Rutina> rutinas;

    private SessionFactory sessionFactory;

    public RepositorioRutinaImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public RepositorioRutinaImpl(){
    }
    @Override
    public List<Rutina> getRutinas() {
        return this.rutinas;
    }


    @Override
    public Rutina getRutinaByObjetivo(Objetivo objetivo) {
        Rutina rutinaObtenida = null;

        for(Rutina rutina : this.rutinas){
            if(rutina.getObjetivo().equals(objetivo)){
                rutinaObtenida = rutina;
            }
        }

        return rutinaObtenida;

    }

    @Override
    public List<Rutina> getRutinasByObjetivo(Objetivo objetivo) {

        String hql = "FROM Rutina WHERE objetivo = :objetivo";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("objetivo",objetivo);

        return query.getResultList();
    }

    @Override
    public Rutina getRutinaParaUsuario(Usuario usuario) {
        Rutina rutinaObtenida = null;

        for(Rutina rutina : this.rutinas){
            if(rutina.getObjetivo().equals(usuario.getObjetivo())){
                rutinaObtenida = rutina;
            }
        }

        return rutinaObtenida;
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
        rutina.getEjercicios().add(ejercicio);
        this.sessionFactory.getCurrentSession().save(rutina);
    }

    @Override
    public void actualizar(Rutina rutina) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(rutina);
    }

    @Override
    public void actualizar(Ejercicio ejercicio) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(ejercicio);
    }

    @Override
    public Ejercicio buscarEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina) throws EjercicioNoExisteEnRutinaException {
        String hql = "SELECT e FROM Rutina r JOIN r.ejercicios e WHERE r.id = :rutinaId AND e.id = :ejercicioId";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("rutinaId",rutina.getIdRutina());
        query.setParameter("ejercicioId",ejercicio.getIdEjercicio());

        try {
            return (Ejercicio) query.getSingleResult();
        } catch (Exception e) {
            throw new EjercicioNoExisteEnRutinaException(); // o lanza una excepción personalizada si prefieres manejarlo de esa manera
        }
    }

    @Override
    public void asignarRutinaAUsuario(Rutina rutina, Usuario usuario) {
        this.guardarRutina(rutina);
        this.guardarUsuario(usuario);
        usuario.getRutinas().add(rutina);
        this.sessionFactory.getCurrentSession().update(usuario);


    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        this.sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public List<Rutina> getRutinasDeUsuario(Usuario usuario) throws UsuarioSinRutinasException {
        String hql = "SELECT r FROM Usuario u JOIN u.rutinas r WHERE u.id = :idUsuario ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idUsuario",usuario.getId());

        return (List<Rutina>) query.getResultList();

    }

    @Override
    public Rutina buscarRutinaEnUsuario(Rutina rutina, Usuario usuario) throws UsuarioNoTieneLaRutinaBuscadaException {
        String hql = "SELECT r FROM Usuario u JOIN u.rutinas r WHERE u.id = :idUsuario AND r.idRutina = :idRutina ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idUsuario",usuario.getId());
        query.setParameter("idRutina",rutina.getIdRutina());

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            throw new UsuarioNoTieneLaRutinaBuscadaException();
        }

    }

    @Override
    public Rutina buscarRutinaPorId(Long idRutina) throws RutinaNoEncontradaException {
        String hql = "FROM Rutina WHERE idRutina = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id",idRutina);

        try {
            return (Rutina) query.getSingleResult();
        } catch (Exception e) {
            throw new RutinaNoEncontradaException();
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
}
