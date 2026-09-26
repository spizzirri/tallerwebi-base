package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioVeterinaria;
import com.tallerwebi.dominio.Veterinaria;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioVeterinaria")
public class RepositorioVeterinariaImpl
  extends RepositorioBase<Veterinaria>
  implements RepositorioVeterinaria {

  @Autowired
  public RepositorioVeterinariaImpl(SessionFactory sessionFactory) {
    super(sessionFactory, Veterinaria.class);
  }

  @Override
  public List<Veterinaria> listar() {
    return listarOrdenadoPor("nombre");
  }
}
