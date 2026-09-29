package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioVeterinaria")
@Transactional
public class ServicioVeterinariaImpl implements ServicioVeterinaria {

  private RepositorioVeterinaria repositorioVeterinaria;
  private RepositorioServicio repositorioServicio;

  @Autowired
  public ServicioVeterinariaImpl(
    RepositorioVeterinaria repositorioVeterinaria,
    RepositorioServicio repositorioServicio
  ) {
    this.repositorioVeterinaria = repositorioVeterinaria;
    this.repositorioServicio = repositorioServicio;
  }

  @Override
  public List<Veterinaria> listar(Long servicioId) {
    List<Veterinaria> veterinarias = repositorioVeterinaria.listar();
    Servicio servicio = servicioId == null ? null : repositorioServicio.buscarPorId(servicioId);
    if (servicio == null) {
      return veterinarias;
    }
    return veterinarias.stream().filter(v -> v.ofrece(servicio)).collect(Collectors.toList());
  }

  @Override
  public Veterinaria buscarPorId(Long id) {
    return id == null ? null : repositorioVeterinaria.buscarPorId(id);
  }
}
