package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Especie;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.RepositorioSocio;
import com.tallerwebi.dominio.Socio;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioSocioTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioSocio repositorioSocio;

  @BeforeEach
  public void init() {
    repositorioSocio = new RepositorioSocioImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnSocioJuntoConSuUsuarioYSuMascota() {
    Socio socio = dadoUnSocioConMascota("30123456", "juan@test.com");

    repositorioSocio.guardar(socio);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Socio obtenido = repositorioSocio.buscarPorDni("30123456");
    assertThat(obtenido, is(notNullValue()));
    assertThat(obtenido.getUsuario().getEmail(), equalTo("juan@test.com"));
    assertThat(obtenido.getMascotas(), hasSize(1));
    assertThat(obtenido.getMascotas().get(0).getNombre(), equalTo("Firulais"));
  }

  @Test
  @Transactional
  public void buscarPorDniInexistenteDeberiaDevolverNull() {
    assertThat(repositorioSocio.buscarPorDni("99999999"), is(nullValue()));
  }

  private Socio dadoUnSocioConMascota(String dni, String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123456");
    usuario.setRol("SOCIO");

    Socio socio = new Socio();
    socio.setNombre("Juan");
    socio.setApellido("Pérez");
    socio.setDni(dni);
    socio.setUsuario(usuario);

    Mascota mascota = new Mascota();
    mascota.setNombre("Firulais");
    mascota.setEspecie(Especie.PERRO);
    socio.agregarMascota(mascota);
    return socio;
  }
}
