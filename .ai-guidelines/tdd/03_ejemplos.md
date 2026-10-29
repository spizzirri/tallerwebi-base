# Ejemplos por tipo de test

Todos los ejemplos son extractos de tests existentes del proyecto y muestran la estructura de cada tipo.
Los nombres de los tests nuevos siguen [02_reglas.md](02_reglas.md), aunque algún extracto use otro formato.

## Unitario de dominio — `dominio/ServicioLoginTest.java`
```java
@BeforeEach
public void init() {
  this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
  this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuarioMock);
}

@Test
public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
  // preparacion
  Usuario usuario = new Usuario();
  usuario.setEmail("existe@test.com");
  when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
    .thenReturn(new Usuario());

  // ejecucion
  assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));

  // validacion
  verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
}
```

## Unitario de presentación — `presentacion/ControladorLoginTest.java`
```java
@Test
public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente() {
  // preparacion
  when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

  // ejecucion
  ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

  // validacion
  assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
  assertThat(
    modelAndView.getModel().get("error").toString(),
    equalToIgnoringCase("Usuario o clave incorrecta")
  );
}
```

## Infraestructura — `infraestructura/RepositorioUsuarioTest.java`
```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioUsuarioTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioUsuario repositorioUsuario;

  @BeforeEach
  public void init() {
    repositorioUsuario = new RepositorioUsuarioImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnNuevoUsuario() {
    // preparacion / ejecucion / validacion con métodos auxiliares dadoQue…, cuando…, entonces…
  }
}
```

## Integración web — `integracion/ControladorLoginTest.java`
```java
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorLoginTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaPaginaLoginCuandoSeNavegaALLogin() throws Exception {
    MvcResult result = this.mockMvc.perform(get("/login")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
  }
}
```
Para validar el HTML renderizado: `result.getResponse().getContentAsString()` con `containsString(...)`.

## E2E — `punta_a_punta/VistaLoginE2E.java` + `vistas/VistaLogin.java`
```java
@Test
void deberiaNavegarAlHomeSiElUsuarioExiste() throws MalformedURLException {
  dadoQueElUsuarioCargaSusDatosDeLoginCon("test@unlam.edu.ar", "test");
  cuandoElUsuarioTocaElBotonDeLogin();
  entoncesDeberiaSerRedirigidoALaVistaDeHome();
}
```
```java
public class VistaLogin extends VistaWeb {

  public VistaLogin(Page page) {
    super(page);
    page.navigate("localhost:8080/spring/login");
  }

  public void darClickEnIniciarSesion() {
    this.darClickEnElElemento("#btn-login");
  }
}
```
