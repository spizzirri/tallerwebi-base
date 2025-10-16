package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Subasta;
import com.tallerwebi.dominio.RepositorioSubasta;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("repositorioSubasta")
public class RepositorioSubastaImpl implements RepositorioSubasta {
    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioSubastaImpl(SessionFactory sessionFactory) {this.sessionFactory = sessionFactory;}

    public void guardar(Subasta subasta) {
        sessionFactory.getCurrentSession().save(subasta);
    }

    public LocalDateTime obtenerTiempoFin(Integer indicador){
        LocalDateTime temp = LocalDateTime.now();
        LocalDateTime result;
        switch(indicador){
            case 0:
                result = temp.plusHours(12);
                break;
            case 1:
                result = temp.plusDays(1);
                break;
            case 2:
                result = temp.plusDays(3);
                break;
            case 3:
                result = temp.plusDays(7);
                break;
            default:
                result = temp;
        }
        return result;
    }

    public Subasta obtenerSubasta(Long id){
        return (Subasta) sessionFactory.getCurrentSession().createCriteria(Subasta.class)
                .add(Restrictions.eq("id", id))
                .uniqueResult();
    }

    @Override
    public List<Subasta> buscarSubastasPorCreador(String emailCreador) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(Subasta.class)
                .createAlias("creador", "u")
                .add(Restrictions.eq("u.email", emailCreador))
                .list();
    }

}
