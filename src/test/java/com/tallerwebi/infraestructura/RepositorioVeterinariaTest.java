package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.RepositorioVeterinaria;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.Veterinaria;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioVeterinariaTest {

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  @Transactional
  @Rollback
  public void guardarYBuscarPorCuitYUsuarioDeberiaRecuperarLaVeterinaria() {
    RepositorioVeterinaria repositorio = new RepositorioVeterinariaImpl(sessionFactory);
    Usuario usuario = new Usuario();
    usuario.setEmail("veterinaria-repo@test.com");
    usuario.setPassword("secreto");
    usuario.setRol(Usuario.ROL_VETERINARIA);
    Veterinaria veterinaria = new Veterinaria();
    veterinaria.setNombre("Veterinaria de prueba");
    veterinaria.setCuit("30765432109");
    veterinaria.setDireccion("Calle 10");
    veterinaria.setTelefono("4444-1000");
    veterinaria.setHoraApertura(LocalTime.of(9, 0));
    veterinaria.setHoraCierre(LocalTime.of(18, 0));
    veterinaria.setDiasAtencion(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));
    veterinaria.setUsuario(usuario);

    repositorio.guardar(veterinaria);
    sessionFactory.getCurrentSession().flush();

    Veterinaria porCuit = repositorio.buscarPorCuit("30765432109");
    Veterinaria porUsuario = repositorio.buscarPorUsuario(usuario.getId());
    assertThat(porCuit.getId(), is(veterinaria.getId()));
    assertThat(porUsuario.getCuit(), is("30765432109"));
    assertThat(porUsuario.getDiasAtencion(), is(veterinaria.getDiasAtencion()));
  }
}
