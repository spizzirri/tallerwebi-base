# Spec: [Nombre de la funcionalidad]

> Las reglas de desarrollo (TDD, convenciones, calidad) están en `AGENTS.md`.
> [Si depende de otra spec, indicarlo acá.]

## 1. Contexto
- **Feature**: [qué se construye]
- **Vista / rutas**: [template y endpoints]

## 2. Reglas de negocio
- **RN1**: [regla verificable, con sus límites exactos]

## 3. Escenarios (Given / When / Then)

### E1: [Nombre]
- **Given** [estado inicial]
- **When** [acción]
- **Then** [resultado observable, con el texto exacto que ve el usuario]

## 4. Fuera de alcance
- [Lo que explícitamente no se hace]

## 5. Diseño por capas (respeta la arquitectura existente)
| Capa | Elemento |
| :--- | :--- |
| Dominio | [entidad, servicio, excepciones, interfaz de repositorio] |
| Infraestructura | [implementación de repositorio] |
| Presentación | [controlador y rutas] |
| Vista | [template] |

## 6. Casos de prueba
Nombres con el formato del proyecto: `[accion]Deberia[resultado]`. Cada caso indica qué regla o escenario cubre.

### Dominio — `dominio/[Clase]Test` (unitario, Mockito)
| Caso | Cubre |
| :--- | :--- |
| `[accion]Deberia[resultado]` | E1 |

### Infraestructura — `infraestructura/[Clase]Test` (Hibernate + HSQLDB, `@Transactional @Rollback`)
| Caso | Cubre |
| :--- | :--- |

### Presentación — `presentacion/[Clase]Test` (unitario, `new` + mock del servicio)
| Caso | Cubre |
| :--- | :--- |

### Integración — `integracion/[Clase]Test` (MockMvc + `SpringWebTestConfig` + `HibernateTestConfig`)
| Caso | Cubre |
| :--- | :--- |

### E2E — `punta_a_punta/Vista[Pantalla]E2E` (Playwright, app levantada)
| Caso | Cubre |
| :--- | :--- |

## 7. Criterios de aceptación
- Todos los casos de la sección 6 existen y pasan.
- `mvn verify` termina en verde (Checkstyle, PMD, CPD y JaCoCo ≥ 80 % de líneas).
- No se agregan dependencias al `pom.xml`.
