# Spec: Listar comentarios

> Spec de la demo. Se ejecuta igual en VS Code + Copilot y en Antigravity.
> Las reglas de desarrollo (TDD, convenciones, calidad) están en `AGENTS.md`.
> **Requiere** `agregar-comentario.md` implementada: reutiliza `Comentario`, `ServicioComentario`,
> `RepositorioComentario`, `ControladorComentario` y `comentarios.html`.

## 1. Contexto
- **Feature**: mostrar los comentarios guardados debajo del formulario de `comentarios.html`.
- **Vista**: `GET /comentarios` y la respuesta de `POST /publicar-comentario` incluyen el listado.

## 2. Reglas de negocio
- **RN1**: el listado muestra los comentarios del más nuevo al más viejo (por `id` descendente).
- **RN2**: el contenido se muestra escapado (`th:text`); nunca se interpreta como HTML.

## 3. Escenarios (Given / When / Then)

### E1: Listado con comentarios
- **Given** existen comentarios guardados
- **When** el usuario accede a `/comentarios`
- **Then** ve una tabla con el contenido de cada comentario, del más nuevo al más viejo

### E2: Listado vacío
- **Given** no existen comentarios
- **When** el usuario accede a `/comentarios`
- **Then** ve el texto "No hay comentarios disponibles" y no ve la tabla

### E3: Error al recuperar comentarios
- **Given** el repositorio falla al listar
- **When** el usuario accede a `/comentarios`
- **Then** ve el texto "No se pudieron cargar los comentarios" y el formulario sigue disponible

### E4: Comentario recién publicado
- **Given** el usuario está en `/comentarios`
- **When** publica un comentario válido
- **Then** el comentario aparece primero en el listado

### E5: Contenido con HTML
- **Given** existe el comentario `<script>alert(1)</script>`
- **When** el usuario accede a `/comentarios`
- **Then** el listado muestra ese texto literal y no se ejecuta ningún script

## 4. Fuera de alcance
- Cambios en las reglas de publicación (spec `agregar-comentario.md`).
- Paginación, búsqueda o filtros.
- Mostrar autor o fecha.

## 5. Diseño por capas (extiende lo creado por `agregar-comentario.md`)
| Capa | Elemento |
| :--- | :--- |
| Dominio | `RepositorioComentario`: agrega `listarTodos()` |
| Dominio | `ServicioComentario` / `ServicioComentarioImpl`: agrega `listar()` |
| Infraestructura | `RepositorioComentarioImpl`: implementa `listarTodos()` |
| Presentación | `ControladorComentario`: agrega el listado al modelo en `GET /comentarios` y `POST /publicar-comentario` |
| Vista | `comentarios.html`: tabla o mensaje de listado vacío o de error |

## 6. Casos de prueba
Nombres con el formato del proyecto: `[accion]Deberia[resultado]`.

### Dominio — `dominio/ServicioComentarioTest` (unitario, Mockito)
| Caso | Cubre |
| :--- | :--- |
| `listarComentariosDeberiaDevolverLosDelRepositorio` | E1 |
| `listarComentariosSinDatosDeberiaDevolverListaVacia` | E2 |

### Infraestructura — `infraestructura/RepositorioComentarioTest` (Hibernate + HSQLDB, `@Transactional @Rollback`)
| Caso | Cubre |
| :--- | :--- |
| `listarTodosDeberiaDevolverlosDelMasNuevoAlMasViejo` | RN1 |
| `listarTodosSinComentariosDeberiaDevolverListaVacia` | E2 |

### Presentación — `presentacion/ControladorComentarioTest` (unitario, `new` + mock del servicio)
| Caso | Cubre |
| :--- | :--- |
| `irAComentariosDeberiaIncluirElListadoEnElModelo` | E1 |
| `irAComentariosSiElServicioFallaDeberiaMostrarMensajeDeError` | E3 |
| `publicarComentarioValidoDeberiaIncluirElListadoEnElModelo` | E4 |

### Integración — `integracion/ControladorComentarioTest` (MockMvc + `SpringWebTestConfig` + `HibernateTestConfig`)
| Caso | Cubre |
| :--- | :--- |
| `getComentariosDeberiaRenderizarLaTablaConLosComentarios` | E1 |
| `getComentariosSinDatosDeberiaMostrarNoHayComentariosDisponibles` | E2 |
| `postComentarioValidoDeberiaMostrarloPrimeroEnElListado` | E4 |
| `getComentariosConHtmlDeberiaMostrarloEscapado` | E5 |

### E2E — `punta_a_punta/VistaComentariosE2E` (Playwright, app levantada)
| Caso | Cubre |
| :--- | :--- |
| `deberiaPublicarUnComentarioYVerloPrimeroEnElListado` | E4 |

## 7. Criterios de aceptación
- Todos los casos de la sección 6 existen y pasan, y los de `agregar-comentario.md` siguen pasando.
- `mvn verify` termina en verde (Checkstyle, PMD, CPD y JaCoCo ≥ 80 % de líneas).
- No se agregan dependencias al `pom.xml`.
