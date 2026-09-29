package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Plan;
import com.tallerwebi.dominio.RepositorioPlan;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPlan")
public class RepositorioPlanImpl extends RepositorioBase<Plan> implements RepositorioPlan {

  @Autowired
  public RepositorioPlanImpl(SessionFactory sessionFactory) {
    super(sessionFactory, Plan.class);
  }

  @Override
  public List<Plan> listar() {
    return listarOrdenadoPor("nivel");
  }
}
