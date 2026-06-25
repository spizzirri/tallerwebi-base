package com.tallerwebi.dominio.Hijos;

import com.tallerwebi.dominio.AliasDeRetiro.ServicioGeneradorAlias;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenes;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.AliasExistenteException;
import com.tallerwebi.dominio.excepcion.AliasVacioException;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import com.tallerwebi.dominio.excepcion.HijoNoEncontradoException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("servicioHijo")
@Transactional
public class ServicioHijoImpl implements ServicioHijo {

  private final RepositorioHijo repoHijo;
  private final ServicioGeneradorAlias servicioGeneradorAlias;
  private final ServicioImagenes servicioImagenes;

  @Autowired
  public ServicioHijoImpl(
    RepositorioHijo repositorioHijo,
    ServicioGeneradorAlias servicioGeneradorAlias,
    ServicioImagenes servicioImagenes
  ) {
    this.repoHijo = repositorioHijo;
    this.servicioGeneradorAlias = servicioGeneradorAlias;
    this.servicioImagenes = servicioImagenes;
  }

  @Override
  public List<Hijo> obtenerHijosPorUsuario(Long idUsuario) {
    return this.repoHijo.listarHijos(idUsuario);
  }

  @Override
  public void guardarHijo(Hijo hijo, MultipartFile fotoPerfil, Usuario usuario) {
    if (repoHijo.existeHijoPorDni(hijo.getDni())) {
      throw new HijoExistenteException();
    }

    hijo.setPadre(usuario);

    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
      String rutaGuardarEnHosting = servicioImagenes.subirImagenHijo(
        fotoPerfil,
        "KionetTWI/img_hijos"
      );
      hijo.setFotoPerfil(rutaGuardarEnHosting);
    }

    String alias = servicioGeneradorAlias.generarAliasDisponible().toUpperCase(Locale.ROOT);
    hijo.setAliasRetiro(alias);

    repoHijo.guardar(hijo);
  }

  @Override
  public void editarHijo(Long idHijo, Hijo datosNuevos, MultipartFile fotoPerfil, Usuario usuario) {
    Hijo hijoExistente = repoHijo.buscarPorId(idHijo);

    if (hijoExistente == null || !hijoExistente.getPadre().getId().equals(usuario.getId())) {
      throw new HijoNoEncontradoException();
    }

    hijoExistente.setNombre(datosNuevos.getNombre());
    hijoExistente.setApellido(datosNuevos.getApellido());
    hijoExistente.setFechaNac(datosNuevos.getFechaNac());
    hijoExistente.setCurso(datosNuevos.getCurso());
    hijoExistente.setDni(datosNuevos.getDni());

    // Encapsulado limpio para PMD
    this.procesarAliasEnEdicion(hijoExistente, datosNuevos.getAliasRetiro());
    this.procesarFotoEnEdicion(hijoExistente, fotoPerfil);

    repoHijo.modificar(hijoExistente);
  }

  @Override
  public void actualizarAlias(Long hijoId, String aliasRetiro, Usuario usuario) {
    validarAliasVacio(aliasRetiro);

    Hijo hijo = repoHijo.buscarPorId(hijoId);

    if (hijo == null) {
      throw new HijoNoEncontradoException();
    }

    if (!hijo.getPadre().getId().equals(usuario.getId())) {
      throw new RuntimeException("No puede modificar este hijo");
    }

    String aliasEnMayuscula = aliasRetiro.toUpperCase(Locale.ROOT);

    // Evita lanzar excepción si el alias ya lo tenía este mismo hijo
    if (repoHijo.existeAlias(aliasEnMayuscula) && !aliasEnMayuscula.equals(hijo.getAliasRetiro())) {
      throw new AliasExistenteException("El alias ya está en uso");
    }

    hijo.setAliasRetiro(aliasEnMayuscula);
    repoHijo.guardar(hijo);
  }

  @Override
  public void eliminarHijo(Long hijoId, Usuario usuario) {
    Hijo hijoExistente = repoHijo.buscarPorId(hijoId);

    if (hijoExistente == null || !hijoExistente.getPadre().getId().equals(usuario.getId())) {
      throw new HijoNoEncontradoException();
    }
    repoHijo.eliminar(hijoExistente);
  }

  //---- MÉTODOS AUXILIARES PRIVADOS ----

  private void validarAliasVacio(String aliasRetiro) {
    if (aliasRetiro == null || aliasRetiro.trim().isEmpty()) {
      throw new AliasVacioException("El alias no puede estar vacío");
    }
  }

  private void procesarAliasEnEdicion(Hijo hijoExistente, String aliasNuevo) {
    if (aliasNuevo == null || aliasNuevo.trim().isEmpty()) {
      return;
    }

    String aliasEditadoMayuscula = aliasNuevo.toUpperCase(Locale.ROOT);

    if (aliasEditadoMayuscula.equals(hijoExistente.getAliasRetiro())) {
      return;
    }

    if (repoHijo.existeAlias(aliasEditadoMayuscula)) {
      throw new AliasExistenteException("El alias ya está en uso");
    }

    hijoExistente.setAliasRetiro(aliasEditadoMayuscula);
  }

  private void procesarFotoEnEdicion(Hijo hijoExistente, MultipartFile fotoPerfil) {
    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
      String rutaGuardarEnHosting = servicioImagenes.subirImagenHijo(
        fotoPerfil,
        "KionetTWI/img_hijos"
      );
      hijoExistente.setFotoPerfil(rutaGuardarEnHosting);
    }
  }
}
