package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DatosDeCompra;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class RepositorioDatosDeCompra {

    private final SessionFactory sessionFactory;

    public RepositorioDatosDeCompra(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void guardar(DatosDeCompra datosCompraPendiente) {
        if (datosCompraPendiente == null) {
            throw new IllegalArgumentException("DatosDeCompra no puede ser null");
        }
        Session session = null;

        try {
            session = sessionFactory.getCurrentSession();

            // Guardar la entidad
            session.save(datosCompraPendiente);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar datos de compra", e);
        }
    }
}
