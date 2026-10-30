package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.Comentario;
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
import org.springframework.test.web.servlet.MvcResult;
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

  @Test
  @Transactional
  @Rollback
  public void getComentariosDeberiaRenderizarLaTablaConLosComentarios() throws Exception {
    // preparacion
    Comentario comentario1 = new Comentario();
    comentario1.setContenido("Primer comentario para listar");
    Comentario comentario2 = new Comentario();
    comentario2.setContenido("Segundo comentario para listar");
    this.sessionFactory.getCurrentSession().persist(comentario1);
    this.sessionFactory.getCurrentSession().persist(comentario2);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    // ejecucion
    this.mockMvc.perform(get("/comentarios"))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("<table")))
      .andExpect(content().string(containsString("Primer comentario para listar")))
      .andExpect(content().string(containsString("Segundo comentario para listar")));
  }

  @Test
  @Transactional
  @Rollback
  public void getComentariosSinDatosDeberiaMostrarNoHayComentariosDisponibles() throws Exception {
    // ejecucion
    this.mockMvc.perform(get("/comentarios"))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("No hay comentarios disponibles")))
      .andExpect(content().string(not(containsString("<table"))));
  }

  @Test
  @Transactional
  @Rollback
  public void postComentarioValidoDeberiaMostrarloPrimeroEnElListado() throws Exception {
    // preparacion
    Comentario previo = new Comentario();
    previo.setContenido("Comentario mas viejo");
    this.sessionFactory.getCurrentSession().persist(previo);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    String nuevo = "Comentario mas nuevo";

    // ejecucion
    MvcResult result =
      this.mockMvc.perform(post("/publicar-comentario").param("contenido", nuevo))
        .andExpect(status().isOk())
        .andExpect(view().name("comentarios"))
        .andReturn();

    // validacion
    String responseBody = result.getResponse().getContentAsString();
    int indexNuevo = responseBody.indexOf(nuevo);
    int indexViejo = responseBody.indexOf("Comentario mas viejo");
    assertThat(indexNuevo >= 0, equalTo(true));
    assertThat(indexViejo >= 0, equalTo(true));
    assertThat(indexNuevo < indexViejo, equalTo(true));
  }

  @Test
  @Transactional
  @Rollback
  public void getComentariosConHtmlDeberiaMostrarloEscapado() throws Exception {
    // preparacion
    String contenidoHtml = "<script>alert(1)</script>";
    Comentario comentario = new Comentario();
    comentario.setContenido(contenidoHtml);
    this.sessionFactory.getCurrentSession().persist(comentario);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    // ejecucion
    this.mockMvc.perform(get("/comentarios"))
      .andExpect(status().isOk())
      .andExpect(view().name("comentarios"))
      .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
  }
}
