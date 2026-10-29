# Desarrollo con specs y TDD en VS Code + GitHub Copilot

Cómo desarrollar una spec de `specs/` con Copilot en este proyecto. El flujo es el mismo que en
[antigravity.md](antigravity.md).

## 1. Qué carga Copilot automáticamente
- [AGENTS.md](AGENTS.md) en la raíz del workspace (setting `chat.useAgentsMdFile`, activo por defecto).
  Desde ahí el agente llega a `.ai-guidelines/` y a `specs/`.
- Otros archivos de personalización (este proyecto no los usa):
  - `.github/copilot-instructions.md`: instrucciones de todo el proyecto.
  - `.github/instructions/*.instructions.md`: instrucciones que se aplican según `applyTo` (por ejemplo `src/test/**`).
  - `.github/agents/*.agent.md`: agentes propios.
  - `.github/prompts/*.prompt.md`: prompts reutilizables que se invocan con `/nombre`.

## 2. Agentes integrados
Se eligen en el selector de agentes de la vista **Chat**.

| Agente | Qué hace | Modifica archivos |
| :--- | :--- | :--- |
| **Ask** | Responde preguntas sobre el código y la spec | No |
| **Plan** | Investiga el proyecto y arma un plan de implementación (también con `/plan`) | No |
| **Agent** | Implementa: edita varios archivos, ejecuta la terminal y corrige errores | Sí |

- **Chat en línea** (`Ctrl+I`): edición puntual sobre el archivo o la selección abierta.
- El antiguo modo **Edit** fue eliminado en VS Code 1.126; su uso lo cubre Agent.
- El plan de **Plan** queda en la memoria de la sesión. **Start Implementation** pasa el plan a **Agent** y empieza a implementarlo.

## 3. Modelos
- Se eligen en el selector de modelos del chat. **Auto** elige el modelo según la tarea.
- Hay modelos de Anthropic (Claude Haiku, Sonnet, Opus), OpenAI (GPT) y Google (Gemini), entre otros.
  La lista disponible depende del plan de Copilot.

## 4. Aprobaciones
- Agent pide confirmación antes de ejecutar comandos de terminal y herramientas.
- Recomendado: aprobar cada comando a mano, para ver qué ejecuta el agente.

## 5. Cómo desarrollar una spec
Requisito: la spec `specs/<funcionalidad>.md` está redactada a partir de [specs/plantilla.md](specs/plantilla.md)
y aprobada. Si depende de otra spec, esa tiene que estar implementada antes.

### Paso 1 — Ask: entender la spec
> Leé `specs/<funcionalidad>.md`. ¿Qué clases nuevas hacen falta, en qué paquetes, y qué casos de prueba hay que
> escribir en cada capa?

### Paso 2 — Plan: armar el plan
> /plan Implementá `specs/<funcionalidad>.md` siguiendo `AGENTS.md`. El plan tiene que recorrer los casos de prueba
> de la sección 6 en el orden de capas de `.ai-guidelines/workflow.md`, con un ciclo Red-Green-Refactor por caso,
> y terminar con `mvn verify`.

Revisar el plan contra la spec: que no falten casos, que no agregue nada fuera de alcance y que no use Spring Boot.
Corregirlo en el chat hasta aprobarlo.

### Paso 3 — Agent: implementar
Hacer clic en **Start Implementation**. Durante la ejecución, verificar que cada test se ejecute y falle antes de
escribir el código que lo hace pasar.

### Paso 4 — Verificar
- `mvn verify` en verde.
- Si la spec tiene E2E, con la app levantada (`docker compose up -d mysql` y `mvn clean jetty:run`):
  `mvn test -Dtest="<ClaseE2E>"`.
- Recorrer los escenarios de la spec en el navegador, en `http://localhost:8080/spring/<ruta>`.
