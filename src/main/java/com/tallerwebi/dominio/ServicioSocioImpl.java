package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioSocio")
@Transactional
public class ServicioSocioImpl implements ServicioSocio {

  private RepositorioSocio repositorioSocio;

  @Autowired
  public ServicioSocioImpl(RepositorioSocio repositorioSocio) {
    this.repositorioSocio = repositorioSocio;
  }

  @Override
  public Socio buscarPorUsuario(Long usuarioId) {
    if (usuarioId == null) {
      return null;
    }
    return repositorioSocio.buscarPorUsuario(usuarioId);
  }
}
