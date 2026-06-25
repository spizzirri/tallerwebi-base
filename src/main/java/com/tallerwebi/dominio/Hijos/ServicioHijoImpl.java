package com.tallerwebi.dominio.Hijos;

import com.tallerwebi.dominio.AliasDeRetiro.ServicioGeneradorAlias;
import com.tallerwebi.dominio.SubidaDeImgs.ServicioImagenes;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import com.tallerwebi.dominio.excepcion.HijoNoEncontradoException;
import java.util.List;
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
    // Si subieron una foto, la procesamos y guardamos la URL
    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
      String rutaGuardarEnHosting = servicioImagenes.subirImagenHijo(
        fotoPerfil,
        "KionetTWI/img_hijos"
      );
      hijo.setFotoPerfil(rutaGuardarEnHosting);
    }

    String alias = servicioGeneradorAlias.generarAliasDisponible();

    hijo.setAliasRetiro(alias);

    //    String alias;
    //
    //    do {
    //      alias = servicioGeneradorAlias.generarAlias();
    //    } while (repoHijo.existeAlias(alias));

    //    hijo.setAliasRetiro(alias);

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

    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
      String rutaGuardarEnHosting = servicioImagenes.subirImagenHijo(
        fotoPerfil,
        "KionetTWI/img_hijos"
      );
      hijoExistente.setFotoPerfil(rutaGuardarEnHosting);
    }

    repoHijo.modificar(hijoExistente);
  }

    @Override
    public void eliminarHijo(Long hijoId, Usuario usuario) {
        Hijo hijoExistente = repoHijo.buscarPorId(hijoId);

        if (hijoExistente == null || !hijoExistente.getPadre().getId().equals(usuario.getId())) {
            throw new HijoNoEncontradoException();
        }
        repoHijo.eliminar(hijoExistente);
    }
}
