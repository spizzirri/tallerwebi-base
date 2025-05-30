package com.tallerwebi.infraestructura;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Carta;
import com.tallerwebi.dominio.RepositorioCarta;

@Repository
public class RepositorioCartaImpl implements RepositorioCarta{

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioCartaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Boolean crear(Carta carta) {
        this.sessionFactory.getCurrentSession().save(carta);
        return true;
    }

    @Override
    public Carta obtenerPor(Long id) {

        String hql = "FROM Carta WHERE id = :id";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id", id);
        return (Carta)query.getSingleResult();
    }

    @Override
    public List<Carta> obtener() {

        String hql = "FROM Carta";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        return query.getResultList();
    }

    @Override
    public void actualizar(Carta carta) {
        String hql = "UPDATE Carta SET nombre = :nombre FROM Carta WHERE id = :id ";
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("id", carta.getId());
        query.setParameter("nombre", carta.getNombre());
        int cantidadDeActualizaciones = query.executeUpdate();

        if(cantidadDeActualizaciones > 1){
            // rollback
            // throw new MuchosRegistrosAfectados("Actualizo mas de uno");
        }
    }
}
