package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioPlan")
@Transactional
public class ServicioPlanImpl implements ServicioPlan {

  private RepositorioPlan repositorioPlan;

  @Autowired
  public ServicioPlanImpl(RepositorioPlan repositorioPlan) {
    this.repositorioPlan = repositorioPlan;
  }

  @Override
  public List<Plan> listar() {
    return repositorioPlan.listar();
  }
}
