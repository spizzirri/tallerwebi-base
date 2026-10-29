# Reglas de testing

## 1. Stack
- JUnit 5 (`org.junit.jupiter.api`).
- Mockito (`mock`, `when`, `verify`) para dependencias.
- Hamcrest (`org.hamcrest.MatcherAssert.assertThat` + `org.hamcrest.Matchers`) para validar resultados.
- `assertThrows` de JUnit para excepciones.
- Spring Test (`SpringExtension`, `@ContextConfiguration`, `@WebAppConfiguration`, `MockMvc`) para integración.
- Playwright para E2E.
- Prohibido: Spring Boot y sus anotaciones (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`), H2, Selenium.

## 2. Nombres
- Clases: `<ClaseProbada>Test` (`ServicioComentarioTest`); E2E: `Vista<Pantalla>E2E` (`VistaComentariosE2E`).
- Métodos unitarios, de infraestructura y de integración: `[accion]Deberia[resultadoEsperado]`.
  - Ejemplo del proyecto: `consultarUsuarioDeberiaLlamarAlRepositorio`.
- Métodos E2E: `deberia[ResultadoEsperado]`, con pasos `dadoQue…`, `cuando…`, `entonces…`
  (como `VistaLoginE2E`).
- Los nombres de los tests son los que define la sección "Casos de prueba" de la spec.

## 3. Estructura de cada test
Tres bloques separados por comentarios, en este orden:
```java
// preparacion
// ejecucion
// validacion
```
Usar `this.` para referenciar los atributos de la clase de test.

## 4. Reglas por tipo
- **Dominio y presentación**: nunca levantan el contexto de Spring.
- **Infraestructura**: cada test con `@Transactional` y `@Rollback`; no dependen de datos de otros tests.
- **Integración**: validan status HTTP, nombre de vista y textos del HTML renderizado.
- **E2E**: interactúan solo a través de page objects en `punta_a_punta/vistas/`.
- Cada caso borde de la spec (límites, vacíos, nulos, errores) tiene su propio test.

## 5. Calidad
- `mvn verify` en verde: Checkstyle (`checkstyle-base.xml`), PMD (`pmd-reglas-de-codigo.xml`), CPD y JaCoCo ≥ 80 % de líneas.
