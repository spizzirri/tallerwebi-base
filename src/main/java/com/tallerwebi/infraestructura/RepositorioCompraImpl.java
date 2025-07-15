package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioCompra;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.UsuarioDto;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository("repositorioCompra")
public class RepositorioCompraImpl implements RepositorioCompra {

    private SessionFactory sessionFactory;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public RepositorioCompraImpl(SessionFactory sessionFactory, RepositorioUsuario repositorioUsuario) {
        this.sessionFactory = sessionFactory;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public void guardarCompraDeUsuario(Compra compra) {
        sessionFactory.getCurrentSession().save(compra);
        }

    @Override
    public void guardarComonentesEnCompraComponente(CompraComponente compraComponente) {
        sessionFactory.getCurrentSession().save(compraComponente);
    }

    public Compra obtenerUltimaCompraDeUsuarioLogueado(UsuarioDto usuarioLogueado) {
        Usuario usuario = repositorioUsuario.buscarUsuario(usuarioLogueado.getEmail());

        String hql = "SELECT DISTINCT C FROM Compra C " +
                "LEFT JOIN FETCH C.productosComprados pc " +
                "LEFT JOIN FETCH pc.componente " +
                "WHERE C.idUsuario = :usuario " +
                "ORDER BY C.fecha DESC";

        return this.sessionFactory.getCurrentSession()
                .createQuery(hql, Compra.class)
                .setParameter("usuario", usuario.getId())
                .setMaxResults(1)
                .uniqueResult(); // o getSingleResult() con try-catch si preferís
    }

    public List<Compra> obtenerCompraDeUsuarioLogueado(UsuarioDto usuarioLogueado) {
        Usuario usuario = repositorioUsuario.buscarUsuario(usuarioLogueado.getEmail());
        String hql = "SELECT DISTINCT C FROM Compra C " +
                "LEFT JOIN FETCH C.productosComprados pc " +
                "LEFT JOIN FETCH pc.componente " +
                " WHERE C.idUsuario = :usuario ORDER BY C.fecha DESC";


        Query query = this.sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("usuario", usuario);

        return query.getResultList();
    }
}
