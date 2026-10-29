# Desarrollo con specs y TDD en Antigravity IDE

Cómo desarrollar una spec de `specs/` con Antigravity en este proyecto. El flujo es el mismo que en
[vscode-copilot.md](vscode-copilot.md).

## 1. Qué carga Antigravity automáticamente
- [AGENTS.md](AGENTS.md) en la raíz del workspace. Desde ahí el agente llega a `.ai-guidelines/` y a `specs/`.
- Otros archivos de personalización (este proyecto no los usa):
  - `GEMINI.md` o `.agents/AGENTS.md`: equivalentes a `AGENTS.md`.
  - `.agents/rules/*.md`: reglas con encabezado YAML `trigger` (`always_on`, `glob`, `model_decision`, `manual`).
  - `.agents/skills/<nombre>/SKILL.md`: procedimientos reutilizables. Los workflows (`.agents/workflows/`)
    dejaron de funcionar el 19/10/2026 y se migran a skills con `/migrate-workflows`.

## 2. Modos del agente
Se eligen en el panel del agente.

| Modo | Qué hace | Cuándo |
| :--- | :--- | :--- |
| **Planning** | Analiza, genera un **Implementation Plan** y una lista de tareas, y espera revisión antes de ejecutar | Funcionalidades nuevas o cambios en varios archivos |
| **Fast** | Ejecuta directamente, sin plan previo | Consultas y cambios puntuales |

- También se puede pedir un plan con `/plan`.
- El Implementation Plan se comenta en línea (seleccionando texto o pasos). **Proceed** lo aprueba y el agente lo ejecuta.
- Al terminar, el agente entrega un **Walkthrough** con lo que hizo y cómo lo verificó.

## 3. Modelos
- Se eligen en el selector de modelos del panel del agente: Gemini (Pro y Flash), Claude (Sonnet y Opus) y GPT-OSS.
- La disponibilidad depende del plan de Google AI, y el uso tiene límites cada 5 horas y semanales.
- Claude Sonnet 4.6, Claude Opus 4.6 y GPT-OSS 120B se retiran el 2/11/2026.

## 4. Revisión y permisos
Se configuran en **Open Antigravity Settings** (paleta de comandos, `Ctrl+Shift+P`), pantalla **General**:
- **Plan Review Policy** (sección *Agent Behavior*): **Always Ask** o Always Proceed.
- **Permission Preset** (sección *Permission Settings*): **Request Review**, Default, Auto (Preview) o Turbo,
  más listas de comandos permitidos y bloqueados.
- Recomendado: **Always Ask** para los planes y **Request Review** para los comandos, para revisar cada plan y
  cada comando antes de que se ejecute.

## 5. Navegador integrado
- El agente usa el Google Chrome instalado, con un perfil separado del personal.
- Puede abrir la app, interactuar con la página y guardar capturas y grabaciones como evidencia.
- Se activa o desactiva con **Enable Browser Tools**, en **Open Antigravity Settings** → **General**.

## 6. Cómo desarrollar una spec
Requisito: la spec `specs/<funcionalidad>.md` está redactada a partir de [specs/plantilla.md](specs/plantilla.md)
y aprobada. Si depende de otra spec, esa tiene que estar implementada antes.

### Paso 1 — Fast: entender la spec
> Sin modificar archivos: leé `specs/<funcionalidad>.md`. ¿Qué clases nuevas hacen falta, en qué paquetes, y qué
> casos de prueba hay que escribir en cada capa?

### Paso 2 — Planning: armar el plan
> Implementá `specs/<funcionalidad>.md` siguiendo `AGENTS.md`. El plan tiene que recorrer los casos de prueba de la
> sección 6 en el orden de capas de `.ai-guidelines/workflow.md`, con un ciclo Red-Green-Refactor por caso, y
> terminar con `mvn verify`.

Revisar el Implementation Plan contra la spec: que no falten casos, que no agregue nada fuera de alcance y que no use
Spring Boot. Comentarlo en línea hasta aprobarlo.

### Paso 3 — Implementar
Hacer clic en **Proceed**. Durante la ejecución, verificar que cada test se ejecute y falle antes de escribir el
código que lo hace pasar.

### Paso 4 — Verificar
- `mvn verify` en verde.
- Si la spec tiene E2E, con la app levantada (`docker compose up -d mysql` y `mvn clean jetty:run`):
  `mvn test -Dtest="<ClaseE2E>"`.
- Pedirle al agente que recorra con el navegador integrado los escenarios de la spec en
  `http://localhost:8080/spring/<ruta>` y revisar la grabación al pie del paso de navegador o en los
  artefactos de la conversación.
