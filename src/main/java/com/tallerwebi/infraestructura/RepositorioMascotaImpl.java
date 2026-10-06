package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.RepositorioMascota;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

// com.tallerwebi.infraestructura.RepositorioMascotaImpl
@Repository("repositorioMascota")
public class RepositorioMascotaImpl extends RepositorioBase<Mascota> implements RepositorioMascota {

  @Autowired
  public RepositorioMascotaImpl(SessionFactory sessionFactory) {
    super(sessionFactory, Mascota.class);
  }
}
