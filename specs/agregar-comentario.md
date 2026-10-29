# Spec: Agregar comentario

> Spec de la demo. Se ejecuta igual en VS Code + Copilot y en Antigravity.
> Las reglas de desarrollo (TDD, convenciones, calidad) están en `AGENTS.md`.
> Es la primera spec: `listar-comentarios.md` se apoya en lo que esta crea.

## 1. Contexto
- **Feature**: publicar un comentario de texto.
- **Vista**: `comentarios.html` (Thymeleaf), en `GET /comentarios`, con un formulario de `textarea` (`maxlength="2000"`).
- **Publicación**: `POST /publicar-comentario`, que vuelve a mostrar `comentarios.html` con el resultado.

## 2. Reglas de negocio
- **RN1**: el contenido es obligatorio. Vacío, nulo o solo espacios es inválido.
- **RN2**: el contenido tiene como máximo 2000 caracteres. 2000 es válido; 2001 es inválido.

## 3. Escenarios (Given / When / Then)

### E1: Acceso al formulario
- **Given** la aplicación está levantada
- **When** el usuario accede a `/comentarios`
- **Then** ve el formulario para publicar un comentario

### E2: Publicación válida
- **Given** el usuario está en `/comentarios`
- **When** publica un comentario de entre 1 y 2000 caracteres
- **Then** ve el texto "Comentario publicado" y el comentario queda guardado

### E3: Publicación vacía
- **Given** el usuario está en `/comentarios`
- **When** publica un comentario vacío o solo con espacios
- **Then** ve el texto "El comentario no puede estar vacío" y no se guarda nada

### E4: Publicación demasiado larga
- **Given** el usuario está en `/comentarios`
- **When** publica un comentario de 2001 caracteres o más
- **Then** ve el texto "El comentario no puede superar los 2000 caracteres" y no se guarda nada

> E4 solo es alcanzable salteando el `maxlength` del navegador; se cubre en integración, no en E2E.

## 4. Fuera de alcance
- Mostrar los comentarios guardados (spec `listar-comentarios.md`).
- Asociar el comentario a un usuario o exigir login.
- Editar o eliminar comentarios.
- Fecha de publicación.

## 5. Diseño por capas (respeta la arquitectura existente)
| Capa | Elemento |
| :--- | :--- |
| Dominio | `Comentario` (`@Entity`: `id`, `contenido` con `length = 2000`) |
| Dominio | `ServicioComentario` / `ServicioComentarioImpl`: `publicar(String contenido)` |
| Dominio | `excepcion/ComentarioInvalido` (mensaje = texto a mostrar) |
| Dominio | `RepositorioComentario`: `guardar(Comentario)` |
| Infraestructura | `RepositorioComentarioImpl` (Hibernate `SessionFactory`) |
| Presentación | `ControladorComentario`: `GET /comentarios`, `POST /publicar-comentario` |
| Vista | `comentarios.html` |

## 6. Casos de prueba
Nombres con el formato del proyecto: `[accion]Deberia[resultado]`.

### Dominio — `dominio/ServicioComentarioTest` (unitario, Mockito)
| Caso | Cubre |
| :--- | :--- |
| `publicarComentarioValidoDeberiaGuardarloEnElRepositorio` | E2 |
| `publicarComentarioDeUnCaracterDeberiaGuardarlo` | RN1 límite |
| `publicarComentarioDe2000CaracteresDeberiaGuardarlo` | RN2 límite |
| `publicarComentarioDe2001CaracteresDeberiaLanzarComentarioInvalidoSinGuardar` | E4 |
| `publicarComentarioVacioDeberiaLanzarComentarioInvalidoSinGuardar` | E3 |
| `publicarComentarioSoloConEspaciosDeberiaLanzarComentarioInvalidoSinGuardar` | E3 |
| `publicarComentarioNuloDeberiaLanzarComentarioInvalidoSinGuardar` | RN1 |

### Infraestructura — `infraestructura/RepositorioComentarioTest` (Hibernate + HSQLDB, `@Transactional @Rollback`)
| Caso | Cubre |
| :--- | :--- |
| `guardarComentarioDeberiaPersistirlo` | E2 |
| `guardarComentarioDe2000CaracteresDeberiaPersistirloCompleto` | RN2 |

### Presentación — `presentacion/ControladorComentarioTest` (unitario, `new` + mock del servicio)
| Caso | Cubre |
| :--- | :--- |
| `irAComentariosDeberiaDevolverLaVistaComentarios` | E1 |
| `publicarComentarioValidoDeberiaMostrarMensajeDeExito` | E2 |
| `publicarComentarioInvalidoDeberiaMostrarElMensajeDeLaExcepcion` | E3, E4 |

### Integración — `integracion/ControladorComentarioTest` (MockMvc + `SpringWebTestConfig` + `HibernateTestConfig`)
| Caso | Cubre |
| :--- | :--- |
| `getComentariosDeberiaResponder200YRenderizarElFormulario` | E1 |
| `postComentarioValidoDeberiaMostrarComentarioPublicado` | E2 |
| `postComentarioVacioDeberiaMostrarMensajeDeError` | E3 |
| `postComentarioDe2001CaracteresDeberiaMostrarMensajeDeError` | E4 |

### E2E — `punta_a_punta/VistaComentariosE2E` (Playwright, app levantada)
| Caso | Cubre |
| :--- | :--- |
| `deberiaPublicarUnComentarioYVerElMensajeDeExito` | E2 |
| `deberiaVerUnErrorAlPublicarUnComentarioVacio` | E3 |

## 7. Criterios de aceptación
- Todos los casos de la sección 6 existen y pasan.
- `mvn verify` termina en verde (Checkstyle, PMD, CPD y JaCoCo ≥ 80 % de líneas).
- No se agregan dependencias al `pom.xml`.
