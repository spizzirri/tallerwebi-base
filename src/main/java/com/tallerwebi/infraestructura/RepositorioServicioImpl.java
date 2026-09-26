package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioServicio;
import com.tallerwebi.dominio.Servicio;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioServicio")
public class RepositorioServicioImpl
  extends RepositorioBase<Servicio>
  implements RepositorioServicio {

  @Autowired
  public RepositorioServicioImpl(SessionFactory sessionFactory) {
    super(sessionFactory, Servicio.class);
  }

  @Override
  public List<Servicio> listar() {
    return listarOrdenadoPor("nivelRequerido, e.nombre");
  }
}
