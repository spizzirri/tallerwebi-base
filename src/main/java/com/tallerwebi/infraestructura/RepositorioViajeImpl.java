package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.Viaje;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class RepositorioViajeImpl implements RepositorioViaje {

    private  SessionFactory sessionFactory;

    @Autowired
    public RepositorioViajeImpl(SessionFactory sessionFactory){

        this.sessionFactory = sessionFactory;

    }
    @Override
    public void guardar(Viaje viaje) {
        sessionFactory.getCurrentSession().save(viaje);
    }

    @Override
    public Viaje buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Viaje.class,id);
    }


    @Override
    public List<Viaje> buscarPorDestino(String destino) {
        CriteriaBuilder builder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<Viaje> criteria = builder.createQuery(Viaje.class);
        Root<Viaje> root = criteria.from(Viaje.class);
        criteria.select(root).where(builder.equal(root.get("destino"), destino));

        return sessionFactory.getCurrentSession().createQuery(criteria).getResultList();
    }

    @Override
    public void actualizar(Viaje viaje) {
        sessionFactory.getCurrentSession().save(viaje);
    }

    @Override
    public List<Viaje> listarViajes() {
        return sessionFactory.getCurrentSession().createQuery("FROM Viaje", Viaje.class).list();
    }

    @Override
    public List<Viaje> buscarPorOrigen(String origen) {
        return sessionFactory.getCurrentSession().createQuery("FROM Viaje V WHERE V.origen = :origen_viaje", Viaje.class).setParameter("origen_viaje",origen).list();
    }

    @Override
    public List<Viaje> buscarPorFecha(String fechaHora) {
        return sessionFactory.getCurrentSession().createCriteria(Viaje.class)
                .add(Restrictions.eq("fecha_hora",fechaHora))
                .list();
    }

    @Override
    public void eliminar(Viaje viaje) {sessionFactory.getCurrentSession().delete(viaje);}

    @Override
    public List<Viaje> buscarPorUsuario(Long idUsuario) {
        return sessionFactory.getCurrentSession().createCriteria(Viaje.class)
                .add(Restrictions.eq("id_usuario",idUsuario))
                .list();

    }

    @Override
    public List buscarPorOrigenDestinoYfecha(String origen, String destino, String fechaHora) {
        return sessionFactory.getCurrentSession().createCriteria(Viaje.class)
                .add(Restrictions.eq("origen",origen))
                .add(Restrictions.eq("destino", destino))
                .add(Restrictions.eq("fecha_hora",fechaHora))
                .list();
                    }
}
