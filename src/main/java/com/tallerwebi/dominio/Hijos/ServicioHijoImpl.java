package com.tallerwebi.dominio.Hijos;

import com.tallerwebi.dominio.AliasDeRetiro.ServicioGeneradorAlias;
import com.tallerwebi.dominio.Usuario.Usuario;
import com.tallerwebi.dominio.excepcion.HijoExistenteException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioHijo")
@Transactional
public class ServicioHijoImpl implements ServicioHijo {

  private final RepositorioHijo repoHijo;
  private final ServicioGeneradorAlias servicioGeneradorAlias;

  @Autowired
  public ServicioHijoImpl(
    RepositorioHijo repositorioHijo,
    ServicioGeneradorAlias servicioGeneradorAlias
  ) {
    this.repoHijo = repositorioHijo;
    this.servicioGeneradorAlias = servicioGeneradorAlias;
  }

  @Override
  public List<Hijo> obtenerHijosPorUsuario(Long idUsuario) {
    return this.repoHijo.listarHijos(idUsuario);
  }

  @Override
  public void guardarHijo(Hijo hijo, Usuario usuario) {
    if (repoHijo.existeHijoPorDni(hijo.getDni())) {
      throw new HijoExistenteException();
    }

    hijo.setPadre(usuario);

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
}
