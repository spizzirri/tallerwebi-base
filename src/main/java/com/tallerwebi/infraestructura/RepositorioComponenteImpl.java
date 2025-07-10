package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioComponente;
import com.tallerwebi.dominio.entidades.Componente;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository("repositorioComponente")
public class RepositorioComponenteImpl implements RepositorioComponente {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioComponenteImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Componente> obtenerComponentesPorTipo(String tipo) {

        String hql = "FROM " + tipo + " c WHERE c.precio > 0";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }
    @Override
    public Componente buscarComponenteConImagenesPorId(Long id) {
        // La magia está en "LEFT JOIN FETCH c.imagenes"
        String jpql = "SELECT c FROM Componente c LEFT JOIN FETCH c.imagenes WHERE c.id = :id";
        return (Componente) sessionFactory.getCurrentSession()
                .createQuery(jpql, Componente.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoEnStock(String tipo) {

        String hql = "FROM " + tipo + " c WHERE c.stock > 0";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoEnStockOrdenadosPorPrecio(String tipo) {
        String hql = "FROM " + tipo + " c WHERE c.stock > 0 ORDER BY c.precio ASC"; // o DESC si querés de mayor a menor
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoYFiltradosPorNombreEnStockOrdenadosPorPrecio(String tipo, String nombre) {
        String hql = "FROM " + tipo + " c WHERE c.stock > 0 AND lower(c.nombre) LIKE lower(:nombre) ORDER BY c.precio ASC"; // o DESC si querés de mayor a menor
        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {

        String hql = "FROM Componente WHERE id = :idComponente";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("idComponente", idComponente);

        return (Componente)query.getSingleResult();
    }

    @Override
    public List<Componente> obtenerComponentesEnStock() {
        String hql = "FROM Componente where stock > 0 AND precio > 0";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public boolean descontarStockDeUnComponente(Long componenteId, Integer cantidadARestar) {
        String hql = "UPDATE Componente SET stock = stock - :cantidadARestar WHERE id = :idComponente AND stock >= :cantidadARestar";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("cantidadARestar", cantidadARestar);
        query.setParameter("idComponente", componenteId);
        int filasActualizadas = query.executeUpdate();
        return filasActualizadas > 0;
    }

    @Override
    public boolean devolverStockDeUnComponente(Long componenteId, Integer cantidadASumar) {
        String hql = "UPDATE Componente SET stock = stock + :cantidadASumar WHERE id = :idComponente";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("cantidadASumar", cantidadASumar);
        query.setParameter("idComponente", componenteId);
        int filasActualizadas = query.executeUpdate();
        return filasActualizadas > 0;
    }

    @Override
    public List<Componente> obtenerComponentes() {
        String hql = "FROM Componente";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorQuery(String nombre) {
        String hql = "FROM Componente WHERE lower(nombre) LIKE lower(:nombre) AND precio > 0";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesMenoresDelPrecioPorParametro(Double precio) {

        String hql = "FROM Componente WHERE precio < :precio";

        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("precio", precio);

        return query.getResultList();
    }

    @Override
    public List<Componente> obtenerComponentesPorTipoYPorQuery(String tipo, String nombre) throws ClassNotFoundException {
        Class<?> tipoClase = Class.forName("com.tallerwebi.dominio.entidades." + tipo);
        String hql = "FROM " + tipo + " c WHERE lower(c.nombre) LIKE lower(:nombre) AND c.precio > 0";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
//        query.setParameter("tipo", tipoClase);
        query.setParameter("nombre", "%" + nombre + "%");
        return query.getResultList();
    }
}
