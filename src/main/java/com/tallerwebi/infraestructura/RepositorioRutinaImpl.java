package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.excepcion.UsuarioRutinaNoEncontradoException;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.usuario.UsuarioRutina;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.EstadoEjercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.presentacion.DatosRutina;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
        this.sessionFactory.getCurrentSession().saveOrUpdate(rutinaAAsignar);
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

        // Buscar si ya existe una relación entre el usuario y la rutina
        String hql = "FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.rutina = :rutina";
        UsuarioRutina usuarioRutinaExistente = this.sessionFactory.getCurrentSession()
                .createQuery(hql, UsuarioRutina.class)
                .setParameter("usuario", usuario)
                .setParameter("rutina", rutina)
                .uniqueResult();

        if (usuarioRutinaExistente != null) {
            // Si existe, actualizar el estado y la fecha de inicio
            usuarioRutinaExistente.setFechaInicio(new Date());
            usuarioRutinaExistente.setActivo(true);
            this.sessionFactory.getCurrentSession().update(usuarioRutinaExistente);

        } else {
            // Si no existe, crear una nueva relación
            UsuarioRutina usuarioRutina = new UsuarioRutina();
            usuarioRutina.setUsuario(usuario);
            usuarioRutina.setRutina(rutina);
            usuarioRutina.setFechaInicio(new Date());
            usuarioRutina.setActivo(true);
            this.sessionFactory.getCurrentSession().save(usuarioRutina);

        }

        // Inicializar los estados de los ejercicios
        this.inicializarEstadosEjercicios(rutina, usuario);
    }


    public EstadoEjercicio findEstadoEjercicioByUsuarioAndEjercicio(Usuario usuario, Ejercicio ejercicio) {
        String hql = "FROM EstadoEjercicio WHERE usuario = :usuario AND ejercicio = :ejercicio";
        return sessionFactory.getCurrentSession().createQuery(hql, EstadoEjercicio.class)
                .setParameter("usuario", usuario)
                .setParameter("ejercicio", ejercicio)
                .uniqueResult();
    }

    public void saveEstadoEjercicio(EstadoEjercicio estadoEjercicio) {
        sessionFactory.getCurrentSession().save(estadoEjercicio);
    }

    public List<Ejercicio> findEjerciciosByRutina(Rutina rutina) {
        String sql = "SELECT e.* FROM Ejercicio e " +
                "JOIN rutina_ejercicio re ON e.idEjercicio = re.ejercicios_idEjercicio " +
                "WHERE re.Rutina_idRutina = :rutinaId";
        return sessionFactory.getCurrentSession().createNativeQuery(sql, Ejercicio.class)
                .setParameter("rutinaId", rutina.getIdRutina())
                .list();
    }

    private void inicializarEstadosEjercicios(Rutina rutina, Usuario usuario) {

        String sql = "SELECT e.* FROM ejercicio e " +
                "JOIN rutina_ejercicio re ON e.idEjercicio = re.ejercicios_idEjercicio " +
                "WHERE re.Rutina_idRutina = :rutinaId";

        List<Ejercicio> ejercicios = this.sessionFactory.getCurrentSession()
                .createNativeQuery(sql, Ejercicio.class)
                .setParameter("rutinaId", rutina.getIdRutina())
                .list();

        for (Ejercicio ejercicio : ejercicios) {

            // Verificar si el estado del ejercicio ya existe para este usuario
            String hql = "FROM EstadoEjercicio ee WHERE ee.usuario = :usuario AND ee.ejercicio = :ejercicio";
            EstadoEjercicio estadoExistente = this.sessionFactory.getCurrentSession()
                    .createQuery(hql, EstadoEjercicio.class)
                    .setParameter("usuario", usuario)
                    .setParameter("ejercicio", ejercicio)
                    .uniqueResult();

            if (estadoExistente != null) {
                // Si existe, actualizar el estado a NO_EMPEZADO solo si estaba COMPLETO o INCOMPLETO
                if (estadoExistente.getEstado() == EstadoEjercicio.Estado.COMPLETO ||
                        estadoExistente.getEstado() == EstadoEjercicio.Estado.INCOMPLETO) {
                    estadoExistente.setEstado(EstadoEjercicio.Estado.NO_EMPEZADO);
                    this.sessionFactory.getCurrentSession().update(estadoExistente);
                }
            } else {
                // Si no existe, crear un nuevo registro con estado NO_EMPEZADO
                EstadoEjercicio nuevoEstadoEjercicio = new EstadoEjercicio(usuario, ejercicio, EstadoEjercicio.Estado.NO_EMPEZADO);
                this.sessionFactory.getCurrentSession().save(nuevoEstadoEjercicio);
            }
        }
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
    @Transactional
    public Rutina getRutinaActualDelUsuario(Usuario usuario) {
        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.activo = true";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Rutina.class)
                .setParameter("usuario", usuario)
                .uniqueResult();
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

        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario.id = :userId AND ur.activo = true AND ur.fechaInicio BETWEEN :startOfDay AND :endOfDay";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, Rutina.class);
        query.setParameter("userId", usuario.getId());
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);

        return (Rutina) query.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public void liberarRutinaActivaDelUsuario(Usuario usuario) {
        System.out.println("entro al metodo del repo");
        String hql = "FROM UsuarioRutina ur WHERE ur.usuario.id = :usuarioId AND ur.activo = true ";
        Query<UsuarioRutina> query = sessionFactory.getCurrentSession().createQuery(hql, UsuarioRutina.class);
        query.setParameter("usuarioId", usuario.getId());
        UsuarioRutina usuarioRutinaBuscado = query.uniqueResult();


        if (usuarioRutinaBuscado != null) {
            usuarioRutinaBuscado.setActivo(false);
            this.sessionFactory.getCurrentSession().saveOrUpdate(usuarioRutinaBuscado);
            System.out.println("Encontro el usuarioRutina");
        }else {
            System.out.println("no hace nada");
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
    public List<UsuarioRutina> buscarListaDeUsuarioRutinaPorUsuarioYRutina(Usuario usuario, Rutina rutina) {
        String hql = "SELECT ur FROM UsuarioRutina ur WHERE ur.usuario.id = :userId AND ur.rutina.id = :rutinaId";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, UsuarioRutina.class);
        query.setParameter("userId", usuario.getId());
        query.setParameter("rutinaId", rutina.getIdRutina());

        return query.getResultList();
}


    @Override
    public UsuarioRutina buscarUsuarioRutinaActivoPorUsuarioYRutina(Usuario usuario, Rutina rutina) {
        String hql = "SELECT ur FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.rutina = :rutina AND ur.activo = true ";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, UsuarioRutina.class);
        query.setParameter("usuario", usuario);
        query.setParameter("rutina", rutina);

        return (UsuarioRutina) query.uniqueResult();
    }

    @Override
    public UsuarioRutina buscarUltimoUsuarioRutinaInactivoPorUsuarioYRutina(Usuario usuario, Rutina rutina) {
        String hql = "SELECT ur FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.rutina = :rutina AND ur.activo = false ORDER BY ur.fechaInicio DESC";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, UsuarioRutina.class);
        query.setParameter("usuario", usuario);
        query.setParameter("rutina", rutina);
        query.setMaxResults(1);

        return (UsuarioRutina) query.uniqueResult();
    }


    @Override
    public void actualizarUsuarioRutina(UsuarioRutina usuarioRutina) {
        this.sessionFactory.getCurrentSession().update(usuarioRutina);
    }

    @Override
    public void guardarUsuarioRutina(UsuarioRutina usuarioRutina) {
        sessionFactory.getCurrentSession().saveOrUpdate(usuarioRutina);
    }

    @Override
    public Rutina getUltimaRutinaRealizadaPorUsuario(Usuario usuario) {
        String hql = "SELECT ur.rutina FROM UsuarioRutina ur WHERE ur.usuario = :usuario AND ur.activo = false ORDER BY ur.fechaInicio DESC";
        Query<Rutina> query = sessionFactory.getCurrentSession().createQuery(hql, Rutina.class);
        query.setParameter("usuario", usuario);

        return query.setMaxResults(1).uniqueResult();
    }

    @Override
    public void actualizarEstadoEjercicio(Usuario usuario, Long idEjercicio, EstadoEjercicio.Estado estado) {
        EstadoEjercicio estadoEjercicio = this.buscarEstadoEjercicioPorUsuarioYEjercicio(usuario,this.getEjercicioById(idEjercicio));
        estadoEjercicio.setEstado(estado);
        sessionFactory.getCurrentSession().saveOrUpdate(estadoEjercicio);
    }

    @Override
    public List<EstadoEjercicio> getEstadoEjercicioList(Usuario usuario, DatosRutina rutina) {
        String sql = "SELECT ee.* FROM estadoEjercicio ee " +
                "JOIN rutina_ejercicio re ON ee.ejercicio_idEjercicio = re.ejercicios_idEjercicio " +
                "WHERE re.Rutina_idRutina = :rutinaId AND ee.usuario_id = :usuarioId";

        return sessionFactory.getCurrentSession()
                .createNativeQuery(sql, EstadoEjercicio.class)
                .setParameter("rutinaId", rutina.getId())
                .setParameter("usuarioId", usuario.getId())
                .list();
    }


    @Override
    public void guardarEstadoEjercicio(EstadoEjercicio estadoEjercicio) {
        sessionFactory.getCurrentSession().saveOrUpdate(estadoEjercicio);
    }

    @Override
    public EstadoEjercicio buscarEstadoEjercicioPorUsuarioYEjercicio(Usuario usuario, Ejercicio ejercicio) {
        String hql = "FROM EstadoEjercicio ee WHERE ee.usuario = :usuario AND ee.ejercicio = :ejercicio";
        Query query = sessionFactory.getCurrentSession().createQuery(hql, EstadoEjercicio.class);
        query.setParameter("usuario", usuario);
        query.setParameter("ejercicio", ejercicio);

        return (EstadoEjercicio) query.uniqueResult();
    }

    @Override
    public void setObjetivoUsuario(Objetivo objetivo, Usuario usuario) {
        usuario.setObjetivo(objetivo);
        this.sessionFactory.getCurrentSession().saveOrUpdate(usuario);
    }


}
