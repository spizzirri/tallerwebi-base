package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.tallerwebi.dominio.Comentario;
import com.tallerwebi.dominio.RepositorioComentario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
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
public class RepositorioComentarioTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioComentario repositorioComentario;

  @BeforeEach
  public void init() {
    this.repositorioComentario = new RepositorioComentarioImpl(this.sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void guardarComentarioDeberiaPersistirlo() {
    // preparacion
    Comentario comentario = new Comentario();
    comentario.setContenido("Comentario para persistir");

    // ejecucion
    this.repositorioComentario.guardar(comentario);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    // validacion
    Comentario persistido =
      this.sessionFactory.getCurrentSession().get(Comentario.class, comentario.getId());
    assertThat(persistido.getContenido(), equalTo(comentario.getContenido()));
  }

  @Test
  @Transactional
  @Rollback
  public void guardarComentarioDe2000CaracteresDeberiaPersistirloCompleto() {
    // preparacion
    Comentario comentario = new Comentario();
    comentario.setContenido("a".repeat(2000));

    // ejecucion
    this.repositorioComentario.guardar(comentario);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    // validacion
    Comentario persistido =
      this.sessionFactory.getCurrentSession().get(Comentario.class, comentario.getId());
    assertThat(persistido.getContenido(), equalTo(comentario.getContenido()));
    assertThat(persistido.getContenido().length(), equalTo(2000));
  }

  @Test
  @Transactional
  @Rollback
  public void listarTodosDeberiaDevolverlosDelMasNuevoAlMasViejo() {
    // preparacion
    Comentario primero = new Comentario();
    primero.setContenido("Primer comentario");
    Comentario segundo = new Comentario();
    segundo.setContenido("Segundo comentario");
    this.sessionFactory.getCurrentSession().persist(primero);
    this.sessionFactory.getCurrentSession().persist(segundo);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    // ejecucion
    List<Comentario> resultado = this.repositorioComentario.listarTodos();

    // validacion
    assertThat(resultado, hasSize(2));
    assertThat(resultado.get(0).getId(), equalTo(segundo.getId()));
    assertThat(resultado.get(1).getId(), equalTo(primero.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void listarTodosSinComentariosDeberiaDevolverListaVacia() {
    // ejecucion
    List<Comentario> resultado = this.repositorioComentario.listarTodos();

    // validacion
    assertThat(resultado, equalTo(List.of()));
  }
}
