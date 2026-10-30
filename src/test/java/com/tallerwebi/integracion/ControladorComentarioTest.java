package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorComentarioTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  @Transactional
  @Rollback
  public void getComentariosDeberiaResponder200YRenderizarElFormulario() throws Exception {
    // ejecucion
    this.mockMvc.perform(get("/comentarios"))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("name=\"contenido\"")))
      .andExpect(content().string(containsString("accept-charset=\"UTF-8\"")))
      .andExpect(content().string(containsString("data-max-code-points=\"2000\"")));
  }

  @Test
  @Transactional
  @Rollback
  public void postComentarioValidoDeberiaMostrarComentarioPublicado() throws Exception {
    // preparacion
    String contenido = "Comentario guardado desde la integración";

    // ejecucion
    this.mockMvc.perform(post("/publicar-comentario").param("contenido", contenido))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("Comentario publicado")));

    // validacion
    Long cantidad =
      this.sessionFactory.getCurrentSession()
        .createQuery(
          "select count(c.id) from Comentario c where c.contenido = :contenido",
          Long.class
        )
        .setParameter("contenido", contenido)
        .getSingleResult();
    assertThat(cantidad, equalTo(1L));
  }

  @Test
  @Transactional
  @Rollback
  public void postComentarioVacioDeberiaMostrarMensajeDeError() throws Exception {
    // ejecucion
    this.mockMvc.perform(post("/publicar-comentario").param("contenido", ""))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("El comentario no puede estar vacío")));

    // validacion
    Long cantidad =
      this.sessionFactory.getCurrentSession()
        .createQuery("select count(c.id) from Comentario c", Long.class)
        .getSingleResult();
    assertThat(cantidad, equalTo(0L));
  }

  @Test
  @Transactional
  @Rollback
  public void postComentarioDe2001CaracteresDeberiaMostrarMensajeDeError() throws Exception {
    // preparacion
    String contenido = "a".repeat(2001);

    // ejecucion
    this.mockMvc.perform(post("/publicar-comentario").param("contenido", contenido))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(
        content().string(containsString("El comentario no puede superar los 2000 caracteres"))
      );

    // validacion
    Long cantidad =
      this.sessionFactory.getCurrentSession()
        .createQuery("select count(c.id) from Comentario c", Long.class)
        .getSingleResult();
    assertThat(cantidad, equalTo(0L));
  }

  @Test
  @Transactional
  @Rollback
  public void postComentarioDe2000PuntosDeCodigoDeberiaMostrarComentarioPublicado()
    throws Exception {
    // preparacion
    String contenido = "a".repeat(2000);

    // ejecucion
    this.mockMvc.perform(post("/publicar-comentario").param("contenido", contenido))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("Comentario publicado")));

    // validacion
    Long cantidad =
      this.sessionFactory.getCurrentSession()
        .createQuery(
          "select count(c.id) from Comentario c where c.contenido = :contenido",
          Long.class
        )
        .setParameter("contenido", contenido)
        .getSingleResult();
    assertThat(cantidad, equalTo(1L));
  }
}
