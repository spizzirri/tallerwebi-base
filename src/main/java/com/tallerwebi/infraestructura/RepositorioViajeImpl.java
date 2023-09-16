package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioViaje;
import com.tallerwebi.dominio.Viaje;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class RepositorioViajeImpl implements RepositorioViaje {

    private  SessionFactory sessionFactory;
    private List<Viaje> viajesEncontrados;

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

}
