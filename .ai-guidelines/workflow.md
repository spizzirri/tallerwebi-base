# Protocolo de trabajo

## 1. Flujo para una funcionalidad
1. **Spec**: la persona redacta `specs/<funcionalidad>.md` a partir de [specs/plantilla.md](../specs/plantilla.md)
   (reglas, escenarios Given/When/Then, fuera de alcance, diseño y casos de prueba).
2. **Plan**: el agente propone un plan que recorre los casos de prueba de la spec, capa por capa.
3. **Aprobación**: la persona revisa el plan y lo corrige antes de que se toque el código.
4. **Implementación TDD**: por cada caso de prueba de la spec:
   - **Red**: escribir el test y ejecutarlo; tiene que fallar por la razón esperada.
   - **Green**: escribir el código mínimo para que pase.
   - **Refactor**: mejorar el código sin cambiar el comportamiento, con los tests en verde.
5. **Verificación**: `mvn verify` en verde y, si la spec tiene E2E, ejecutarlos con la app levantada.

## 2. Orden de implementación por capas
Dominio (entidad, excepciones, servicio) → Infraestructura (repositorio) → Presentación (controlador) →
Vista (Thymeleaf) → Integración (MockMvc) → E2E (Playwright).

## 3. Comandos
| Acción | Comando |
| :--- | :--- |
| Tests unitarios y de integración + calidad (Checkstyle, PMD, CPD, JaCoCo ≥ 80 %) | `mvn verify` |
| Un test puntual | `mvn test -Dtest="ServicioComentarioTest"` |
| Levantar la base de datos | `docker compose up -d mysql` |
| Levantar la app | `mvn clean jetty:run` |
| E2E (con la app levantada) | `mvn test -Dtest="VistaComentariosE2E"` |

- `mvn validate` solo ejecuta Checkstyle, PMD y CPD; no corre tests ni cobertura.
- Los E2E (`*E2E.java`) no se ejecutan con `mvn verify`: Surefire solo incluye `*Test.java`.
- El formato lo aplica Prettier durante el build; Checkstyle no controla el largo de línea.

## 4. Qué modo usar según la tarea
| Tarea | VS Code + Copilot | Antigravity |
| :--- | :--- | :--- |
| Entender la spec o el código, sin cambios | Agente **Ask** | Modo **Fast**, pidiendo solo análisis |
| Funcionalidad nueva o cambio en varios archivos | Agente **Plan** → **Start Implementation** (Agent) | Modo **Planning** (Implementation Plan → **Proceed**) |
| Cambio puntual en un archivo | Chat en línea (`Ctrl+I`) | Modo **Fast** |
