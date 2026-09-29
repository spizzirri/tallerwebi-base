package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.SocioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioRegistro")
@Transactional
public class ServicioRegistroImpl implements ServicioRegistro {

  public static final String ROL_SOCIO = "SOCIO";

  private RepositorioSocio repositorioSocio;
  private RepositorioUsuario repositorioUsuario;
  private RepositorioPlan repositorioPlan;

  @Autowired
  public ServicioRegistroImpl(
    RepositorioSocio repositorioSocio,
    RepositorioUsuario repositorioUsuario,
    RepositorioPlan repositorioPlan
  ) {
    this.repositorioSocio = repositorioSocio;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioPlan = repositorioPlan;
  }

  @Override
  public void registrarSocio(Socio socio, Mascota mascota, Long planId)
    throws UsuarioExistente, SocioExistente, DatosDeRegistroInvalidos {
    validarSocio(socio);
    validarMascota(mascota);
    Plan plan = planId == null ? null : repositorioPlan.buscarPorId(planId);
    if (plan == null) {
      throw new DatosDeRegistroInvalidos("Elegí un plan para tu mascota");
    }

    if (repositorioUsuario.buscar(socio.getUsuario().getEmail()) != null) {
      throw new UsuarioExistente();
    }
    if (repositorioSocio.buscarPorDni(socio.getDni()) != null) {
      throw new SocioExistente();
    }

    socio.getUsuario().setRol(ROL_SOCIO);
    socio.getUsuario().activar();
    mascota.setPlan(plan);
    socio.agregarMascota(mascota);
    repositorioSocio.guardar(socio);
  }

  private void validarSocio(Socio socio) throws DatosDeRegistroInvalidos {
    if (algunoVacio(socio.getNombre(), socio.getApellido(), socio.getDni())) {
      throw new DatosDeRegistroInvalidos("Nombre, apellido y DNI son obligatorios");
    }
    if (!socio.getDni().matches("\\d{7,8}")) {
      throw new DatosDeRegistroInvalidos("El DNI debe tener 7 u 8 números");
    }
    validarCredenciales(socio.getUsuario());
  }

  private void validarCredenciales(Usuario usuario) throws DatosDeRegistroInvalidos {
    if (usuario == null || algunoVacio(usuario.getEmail(), usuario.getPassword())) {
      throw new DatosDeRegistroInvalidos("Email y contraseña son obligatorios");
    }
  }

  private void validarMascota(Mascota mascota) throws DatosDeRegistroInvalidos {
    if (mascota == null || algunoVacio(mascota.getNombre()) || mascota.getEspecie() == null) {
      throw new DatosDeRegistroInvalidos("Tenés que registrar al menos una mascota");
    }
    validarPeso(mascota.getPeso());
    validarFechaNacimiento(mascota.getFechaNacimiento());
  }

  private void validarPeso(Double peso) throws DatosDeRegistroInvalidos {
    if (peso != null && peso <= 0) {
      throw new DatosDeRegistroInvalidos("El peso de la mascota debe ser mayor a cero");
    }
  }

  private void validarFechaNacimiento(LocalDate fecha) throws DatosDeRegistroInvalidos {
    if (fecha != null && fecha.isAfter(LocalDate.now())) {
      throw new DatosDeRegistroInvalidos("La fecha de nacimiento no puede ser futura");
    }
  }

  private boolean algunoVacio(String... valores) {
    for (String valor : valores) {
      if (valor == null || valor.isBlank()) {
        return true;
      }
    }
    return false;
  }
}
