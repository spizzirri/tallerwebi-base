package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.DatosDeRegistroInvalidos;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioVeterinaria")
@Transactional
public class ServicioVeterinariaImpl implements ServicioVeterinaria {

  private RepositorioVeterinaria repositorioVeterinaria;
  private RepositorioServicio repositorioServicio;
  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioVeterinariaImpl(
    RepositorioVeterinaria repositorioVeterinaria,
    RepositorioServicio repositorioServicio,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioVeterinaria = repositorioVeterinaria;
    this.repositorioServicio = repositorioServicio;
    this.repositorioUsuario = repositorioUsuario;
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

  @Override
  public Veterinaria buscarPorUsuario(Long usuarioId) {
    return usuarioId == null ? null : repositorioVeterinaria.buscarPorUsuario(usuarioId);
  }

  @Override
  public void registrar(Veterinaria veterinaria, List<Long> serviciosIds)
    throws DatosDeRegistroInvalidos, UsuarioExistente {
    validarDatos(veterinaria, serviciosIds);
    validarCredenciales(veterinaria);
    validarUsuarioNuevo(veterinaria.getUsuario().getEmail());
    String cuit = normalizarCuit(veterinaria.getCuit());
    if (repositorioVeterinaria.buscarPorCuit(cuit) != null) {
      throw new DatosDeRegistroInvalidos("Ya existe una veterinaria con ese CUIT/CUIL");
    }

    veterinaria.setCuit(cuit);
    veterinaria.setNombre(veterinaria.getNombre().trim());
    veterinaria.setDireccion(veterinaria.getDireccion().trim());
    veterinaria.setTelefono(veterinaria.getTelefono().trim());
    veterinaria.getUsuario().setEmail(veterinaria.getUsuario().getEmail().trim());
    veterinaria.getUsuario().setRol(Usuario.ROL_VETERINARIA);
    veterinaria.getUsuario().activar();
    veterinaria.getServicios().addAll(buscarServicios(serviciosIds));
    repositorioVeterinaria.guardar(veterinaria);
  }

  @Override
  public void actualizarPerfil(
    Long usuarioId,
    Veterinaria datos,
    String email,
    List<Long> serviciosIds
  ) throws DatosDeRegistroInvalidos, UsuarioExistente {
    validarDatos(datos, serviciosIds);
    validarEmail(email);
    Veterinaria veterinaria = buscarPorUsuario(usuarioId);
    if (veterinaria == null) {
      throw new DatosDeRegistroInvalidos("No se pudo identificar a la veterinaria");
    }
    Usuario usuarioExistente = repositorioUsuario.buscar(email);
    if (usuarioExistente != null && !usuarioExistente.getId().equals(usuarioId)) {
      throw new UsuarioExistente();
    }
    String cuit = normalizarCuit(datos.getCuit());
    Veterinaria veterinariaConCuit = repositorioVeterinaria.buscarPorCuit(cuit);
    if (veterinariaConCuit != null && !veterinariaConCuit.getId().equals(veterinaria.getId())) {
      throw new DatosDeRegistroInvalidos("Ya existe una veterinaria con ese CUIT/CUIL");
    }

    veterinaria.setNombre(datos.getNombre().trim());
    veterinaria.setDireccion(datos.getDireccion().trim());
    veterinaria.setTelefono(datos.getTelefono().trim());
    veterinaria.setCuit(cuit);
    veterinaria.setHoraApertura(datos.getHoraApertura());
    veterinaria.setHoraCierre(datos.getHoraCierre());
    veterinaria.setDiasAtencion(datos.getDiasAtencion());
    veterinaria.getServicios().clear();
    veterinaria.getServicios().addAll(buscarServicios(serviciosIds));
    veterinaria.getUsuario().setEmail(email.trim());
  }

  private void validarDatos(Veterinaria veterinaria, List<Long> serviciosIds)
    throws DatosDeRegistroInvalidos {
    validarDatosBasicos(veterinaria);
    validarCuit(veterinaria.getCuit());
    validarHorario(veterinaria.getHoraApertura(), veterinaria.getHoraCierre());
    validarDias(veterinaria.getDiasAtencion());
    validarServiciosSeleccionados(serviciosIds);
  }

  private void validarDatosBasicos(Veterinaria veterinaria) throws DatosDeRegistroInvalidos {
    if (veterinaria == null) {
      throw new DatosDeRegistroInvalidos("Faltan los datos de la veterinaria");
    }
    if (
      vacio(veterinaria.getNombre()) ||
      vacio(veterinaria.getDireccion()) ||
      vacio(veterinaria.getTelefono())
    ) {
      throw new DatosDeRegistroInvalidos("Nombre, dirección y teléfono son obligatorios");
    }
  }

  private void validarDias(Set<DayOfWeek> dias) throws DatosDeRegistroInvalidos {
    if (dias == null || dias.isEmpty()) {
      throw new DatosDeRegistroInvalidos("Elegí al menos un día de atención");
    }
  }

  private void validarServiciosSeleccionados(List<Long> serviciosIds)
    throws DatosDeRegistroInvalidos {
    if (serviciosIds == null || serviciosIds.isEmpty()) {
      throw new DatosDeRegistroInvalidos("Elegí al menos un servicio");
    }
  }

  private void validarUsuarioNuevo(String email) throws DatosDeRegistroInvalidos, UsuarioExistente {
    validarEmail(email);
    if (repositorioUsuario.buscar(email.trim()) != null) {
      throw new UsuarioExistente();
    }
  }

  private void validarCredenciales(Veterinaria veterinaria) throws DatosDeRegistroInvalidos {
    if (veterinaria.getUsuario() == null || vacio(veterinaria.getUsuario().getPassword())) {
      throw new DatosDeRegistroInvalidos("La contraseña es obligatoria");
    }
  }

  private void validarEmail(String email) throws DatosDeRegistroInvalidos {
    if (vacio(email) || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      throw new DatosDeRegistroInvalidos("Ingresá un email válido");
    }
  }

  private void validarCuit(String cuit) throws DatosDeRegistroInvalidos {
    if (vacio(cuit) || !normalizarCuit(cuit).matches("\\d{11}")) {
      throw new DatosDeRegistroInvalidos("El CUIT/CUIL debe tener 11 números");
    }
  }

  private String normalizarCuit(String cuit) {
    return cuit == null ? "" : cuit.replaceAll("[^\\d]", "");
  }

  private void validarHorario(java.time.LocalTime apertura, java.time.LocalTime cierre)
    throws DatosDeRegistroInvalidos {
    if (
      apertura == null ||
      cierre == null ||
      !apertura.isBefore(cierre) ||
      apertura.getMinute() % 30 != 0 ||
      cierre.getMinute() % 30 != 0
    ) {
      throw new DatosDeRegistroInvalidos("Elegí horarios válidos en intervalos de 30 minutos");
    }
  }

  private List<Servicio> buscarServicios(List<Long> serviciosIds) throws DatosDeRegistroInvalidos {
    List<Servicio> servicios = serviciosIds
      .stream()
      .distinct()
      .map(repositorioServicio::buscarPorId)
      .collect(Collectors.toList());
    if (servicios.stream().anyMatch(servicio -> servicio == null)) {
      throw new DatosDeRegistroInvalidos("Uno de los servicios elegidos no existe");
    }
    return servicios;
  }

  private boolean vacio(String valor) {
    return valor == null || valor.isBlank();
  }
}
