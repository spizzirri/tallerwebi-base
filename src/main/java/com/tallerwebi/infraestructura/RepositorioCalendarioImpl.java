package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.ItemRendimiento;
import com.tallerwebi.dominio.calendario.RepositorioCalendario;
import com.tallerwebi.dominio.calendario.TipoRendimiento;
import com.tallerwebi.dominio.usuario.Usuario;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class RepositorioCalendarioImpl implements RepositorioCalendario {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioCalendarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<ItemRendimiento> obtenerItemsRendimiento() {
        return this.sessionFactory.getCurrentSession()
                .createQuery("FROM ItemRendimiento", ItemRendimiento.class)
                .getResultList();
    }

    @Override
    public void guardar(ItemRendimiento itemRendimiento) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(itemRendimiento);
    }

    @Override
    public boolean existeItemRendimientoPorFecha(LocalDate fecha) {
        String hql = "SELECT count(*) FROM ItemRendimiento WHERE fecha = :fecha";
        Long count = this.sessionFactory.getCurrentSession()
                .createQuery(hql, Long.class)
                .setParameter("fecha", fecha)
                .uniqueResult();
        return count > 0;
    }

    @Override
    public ItemRendimiento obtenerItemMasSeleccionado() {
        String hql = "SELECT ir.tipoRendimiento, COUNT(ir) FROM ItemRendimiento ir " +
                "WHERE ir.tipoRendimiento != :descanso " +
                "GROUP BY ir.tipoRendimiento " +
                "ORDER BY COUNT(ir) DESC";

        List<Object[]> results = this.sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("descanso", TipoRendimiento.DESCANSO)
                .setMaxResults(1)
                .getResultList();

        if (results.isEmpty()) {
            return null;
        }
        TipoRendimiento tipoRendimientoMasSeleccionado = (TipoRendimiento) results.get(0)[0];

        hql = "FROM ItemRendimiento ir WHERE ir.tipoRendimiento = :tipoRendimiento ORDER BY ir.fecha DESC";
        List<ItemRendimiento> items = this.sessionFactory.getCurrentSession()
                .createQuery(hql, ItemRendimiento.class)
                .setParameter("tipoRendimiento", tipoRendimientoMasSeleccionado)
                .setMaxResults(1)
                .getResultList();

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public ItemRendimiento getItemRendimientoDeUsuarioHoyPorId(Long usuarioId) {
        String hql = "SELECT u FROM Usuario u JOIN FETCH u.itemsRendimiento WHERE u.id = :usuarioId";
        Query<Usuario> query = sessionFactory.getCurrentSession().createQuery(hql, Usuario.class);
        query.setParameter("usuarioId", usuarioId);

        Usuario usuario = query.uniqueResult();

        if (usuario != null) {
            LocalDate today = LocalDate.now();
            for (ItemRendimiento item : usuario.getItemsRendimiento()) {
                if (item.getFecha().equals(today)) {
                    return item;
                }
            }
        }

        return null;
    }


    @Override
    public Usuario getUsuarioById(Long id) {
        String hql = "SELECT u FROM Usuario u LEFT JOIN FETCH u.itemsRendimiento WHERE u.id = :id";
        return this.sessionFactory.getCurrentSession()
                .createQuery(hql, Usuario.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public void guardarRendimientoEnUsuario(ItemRendimiento rendimiento, Usuario usuario) {
        Usuario usuarioBuscado = this.getUsuarioById(usuario.getId());
        usuarioBuscado.getItemsRendimiento().add(rendimiento);
        this.sessionFactory.getCurrentSession().saveOrUpdate(usuarioBuscado);
    }

    @Override
    public List<ItemRendimiento> getItemsRendimientoDeUsuario(Usuario usuario) {
        Usuario usuarioConItems = this.getUsuarioById(usuario.getId());
        return usuarioConItems.getItemsRendimiento();
    }
}


