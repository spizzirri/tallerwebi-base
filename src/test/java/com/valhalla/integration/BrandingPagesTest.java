package com.valhalla.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Branding as the served pages actually show it (AC-01 a AC-06). The token guarantees live in
 * {@code BrandTokensTest}; this one checks that each route renders the brand, in all three
 * navbar states, because the navbar has three separate branches.
 */
@WebIntegrationTest
public class BrandingPagesTest {

  private static final String BRAND = "PlanIt";

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    mockMvc =
      MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
  }

  // I-01 (AC-01, AC-02): anónimo.
  @Test
  public void shouldBrandTheNavbarForAnonymousVisitors() throws Exception {
    assertNavbarBrand(mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn());
  }

  // I-02 (AC-01): usuario autenticado.
  @Test
  public void shouldBrandTheNavbarForAUser() throws Exception {
    assertNavbarBrand(
      mockMvc
        .perform(
          get("/explore")
            .with(
              user("user@unlam.edu.ar").authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
            )
        )
        .andExpect(status().isOk())
        .andReturn()
    );
  }

  // I-03 (AC-01): admin.
  @Test
  public void shouldBrandTheNavbarForAnAdmin() throws Exception {
    assertNavbarBrand(
      mockMvc
        .perform(
          get("/admin/users")
            .with(
              user("admin@unlam.edu.ar")
                .authorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"))
            )
        )
        .andExpect(status().isOk())
        .andReturn()
    );
  }

  // I-04 (AC-02): el título de login.
  @Test
  public void shouldNotTitleTheLoginPageWithTheRepoName() throws Exception {
    String title = titleOf(mockMvc.perform(get("/auth/login")).andReturn());

    assertThat(title, containsString(BRAND));
    assertThat(title, not(containsString("Valhalla")));
  }

  // I-05 (AC-02): el título de register.
  @Test
  public void shouldNotTitleTheRegisterPageWithTheRepoName() throws Exception {
    String title = titleOf(mockMvc.perform(get("/auth/register")).andReturn());

    assertThat(title, containsString(BRAND));
    assertThat(title, not(containsString("Valhalla")));
  }

  // AC-05 y AC-06: el favicon y la tipografía llegan por el head compartido, así que
  // cualquier vista los tiene. Se verifica en dos rutas distintas para que una vista
  // no pueda cumplir el criterio sola.
  @Test
  public void shouldServeTheFaviconAndTheFontFromTheSharedHead() throws Exception {
    for (MvcResult result : new MvcResult[] {
      mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn(),
      mockMvc.perform(get("/auth/login")).andExpect(status().isOk()).andReturn(),
    }) {
      assertThat(body(result), containsString("<link rel=\"icon\""));
      assertThat(body(result), containsString("/css/brand.css"));
      // El bloque que el browser build de Tailwind lee del DOM. Con otro type, o
      // ausente, las utilidades de marca no se generan y el navbar queda sin color.
      assertThat(body(result), containsString("<style type=\"text/tailwindcss\">"));
      assertThat(body(result), containsString("@theme"));
    }

    // La hoja se sirve de verdad y trae los tokens: el link no alcanza, y un 404 acá
    // deja la página sin marca sin que ninguna vista lo note.
    String css = mockMvc
      .perform(get("/css/brand.css"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat(css, containsString("--typeface-sans"));
    assertThat(css, containsString("--typeface-serif"));
    assertThat(css, containsString("--primary"));
    assertThat(css, containsString("--secondary"));
  }

  // [STD] Los módulos JS llevan los handlers de los botones (openParticipants, openRequests).
  // Sin Cache-Control el navegador les aplica frescura heurística y puede servir un
  // plans-detail.js viejo contra un template nuevo: los botones nuevos quedan sin handler y no
  // hacen nada. no-cache obliga a revalidar en cada carga (304 si el archivo no cambió).
  @Test
  public void shouldMakeStaticModulesRevalidateOnEveryLoad() throws Exception {
    mockMvc
      .perform(get("/js/pages/plans-detail.js"))
      .andExpect(status().isOk())
      .andExpect(header().string("Cache-Control", containsString("no-cache")));
    mockMvc
      .perform(get("/css/brand.css"))
      .andExpect(status().isOk())
      .andExpect(header().string("Cache-Control", containsString("no-cache")));
  }

  // I-06 (AC-03, AC-04): el mapa del marker y el del panel salen del token, no de un hex.
  @Test
  public void shouldTakeTheCategoryColorFromTheToken() throws Exception {
    String body = body(
      mockMvc
        .perform(
          get("/explore")
            .with(
              user("user@unlam.edu.ar").authorities(AuthorityUtils.createAuthorityList("ROLE_USER"))
            )
        )
        .andExpect(status().isOk())
        .andReturn()
    );

    assertThat(body, containsString("getPropertyValue"));
    assertThat(body, not(containsString("'#e74c3c'")));
  }

  // El recurso estático tiene que servirse de verdad, no solo estar linkeado.
  @Test
  public void shouldServeTheFaviconFile() throws Exception {
    mockMvc
      .perform(get("/images/favicon.svg"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
  }

  // Los iconos y la imagen de compartir se descargan fuera del navegador, desde scrapers
  // que no ejecutan la página: un 404 acá no lo nota ningún test de vista, se nota el link
  // compartido. El manifest además tiene que servirse con su propio content type o el
  // navegador no lo trata como manifest.
  @Test
  public void shouldServeTheInstallAndShareAssets() throws Exception {
    mockMvc
      .perform(get("/images/og-image.png"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("image/png"));
    mockMvc
      .perform(get("/images/apple-touch-icon.png"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("image/png"));
    mockMvc
      .perform(get("/manifest.json"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith("application/json"));
  }

  // El manifest no es decorativo: name, start_url e íconos son lo que el navegador usa al
  // ofrecer "agregar a pantalla de inicio", y un JSON inválido hace que lo rechace entero.
  @Test
  public void shouldShipAUsableWebManifest() throws Exception {
    String manifest = mockMvc
      .perform(get("/manifest.json"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var parsed = new com.fasterxml.jackson.databind.ObjectMapper().readTree(manifest);

    org.junit.jupiter.api.Assertions.assertEquals(BRAND, parsed.get("short_name").asText());
    org.junit.jupiter.api.Assertions.assertEquals("/", parsed.get("start_url").asText());
    assertThat(parsed.get("theme_color").asText(), containsString("#"));
    org.junit.jupiter.api.Assertions.assertTrue(
      parsed.get("icons").size() > 0,
      "el manifest tiene que declarar al menos un ícono"
    );
  }

  // El head compartido declara la vista previa. Que la URL de la imagen salga absoluta
  // depende del host de la request: se asserta la forma acá, y que el host y el puerto
  // sean los de verdad lo cubre BrandingViewE2E, que corre contra un servidor real.
  @Test
  public void shouldRenderTheSharePreviewAndInstallIconsInTheHead() throws Exception {
    String head = body(mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn());

    assertThat(head, containsString("property=\"og:site_name\""));
    assertThat(head, containsString("property=\"og:image\""));
    assertThat(head, containsString("name=\"twitter:card\""));
    assertThat(head, containsString("name=\"theme-color\""));
    assertThat(head, containsString("rel=\"manifest\""));
    assertThat(head, containsString("rel=\"apple-touch-icon\""));
    // La expresión de Thymeleaf tiene que haberse resuelto, no quedar literal en el HTML.
    assertThat(head, not(containsString("th:content")));
    assertThat(head, not(containsString("@siteOrigin")));

    java.util.regex.Pattern absoluteImage = java.util.regex.Pattern.compile(
      "https?://[^/]+:\\d+/images/og-image\\.png"
    );
    // Open Graph se declara con property= y Twitter con name=: el patrón acepta ambos.
    for (String property : new String[] { "og:image", "twitter:image" }) {
      java.util.regex.Matcher matcher = java.util.regex.Pattern
        .compile("<meta (?:property|name)=\"" + property + "\" content=\"([^\"]+)\"")
        .matcher(head);
      org.junit.jupiter.api.Assertions.assertTrue(matcher.find(), "falta " + property);
      org.junit.jupiter.api.Assertions.assertTrue(
        absoluteImage.matcher(matcher.group(1)).matches(),
        property + " tiene que ser absoluta con host y puerto, llegó: " + matcher.group(1)
      );
    }
  }

  private void assertNavbarBrand(MvcResult result) throws Exception {
    String body = body(result);

    assertThat(body, containsString(BRAND));
    assertThat(body, containsString("navbar-brand"));
    assertThat(body, not(containsString("UNLAM")));
  }

  private String titleOf(MvcResult result) throws Exception {
    var matcher = java.util.regex.Pattern
      .compile("<title>(.*?)</title>", java.util.regex.Pattern.DOTALL)
      .matcher(body(result));
    org.junit.jupiter.api.Assertions.assertTrue(matcher.find(), "la respuesta no trae <title>");
    return matcher.group(1).trim();
  }

  private String body(MvcResult result) throws Exception {
    return result.getResponse().getContentAsString();
  }
}
