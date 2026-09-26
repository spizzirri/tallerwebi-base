package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioCobertura")
@Transactional
public class ServicioCoberturaImpl implements ServicioCobertura {

  private RepositorioServicio repositorioServicio;

  @Autowired
  public ServicioCoberturaImpl(RepositorioServicio repositorioServicio) {
    this.repositorioServicio = repositorioServicio;
  }

  @Override
  public List<Servicio> listarServicios() {
    return repositorioServicio.listar();
  }

  @Override
  public List<ItemCobertura> coberturaDe(Mascota mascota, FiltroCobertura filtro) {
    FiltroCobertura filtroAplicado = filtro == null ? FiltroCobertura.TODOS : filtro;
    return repositorioServicio
      .listar()
      .stream()
      .map(s -> new ItemCobertura(s, mascota != null && mascota.tieneCubierto(s)))
      .filter(filtroAplicado::acepta)
      .collect(Collectors.toList());
  }

  @Override
  public List<ItemCobertura> coberturaEn(Veterinaria veterinaria, Mascota mascota) {
    if (veterinaria == null) {
      return List.of();
    }
    return veterinaria
      .getServicios()
      .stream()
      .map(s -> new ItemCobertura(s, mascota != null && mascota.tieneCubierto(s)))
      .collect(Collectors.toList());
  }
}
