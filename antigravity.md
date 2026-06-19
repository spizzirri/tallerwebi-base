# Modos de trabajo en Google Antigravity

La interacción con la IA en el entorno Antigravity se estructura a través de dos modos de trabajo fundamentales, seleccionables en el panel derecho. Para entender cómo aplicar estos modos en el desarrollo diario del proyecto `tallerwebi-base`, consulta siempre el **Protocolo de Desarrollo** en `.ai-guidelines/workflow.md`.

## 1. Planning Mode
Diseñado para abordar tareas complejas, de arquitectura o cambios que impacten en múltiples archivos. Antes de ejecutar, la IA analiza y genera un plan de implementación para revisión humana.
- **Uso**: Tareas de alta complejidad (>3 archivos), nuevas funcionalidades.

## 2. Fast Mode
Ejecución directa y rápida para tareas que no requieren un plan estructurado previo.
- **Uso**: Tareas rutinarias, corrección de bugs simples, pequeños cambios en un solo archivo.

---

### Política de Supervisión: "Always Ask"
Independientemente del modo elegido (Planning o Fast), el sistema implementa una capa de seguridad para acciones sensibles. El agente detendrá el flujo y solicitará confirmación interactiva para:
- Modificaciones en archivos críticos (configuración, esquemas de BD).
- Ejecución de comandos externos.
- Operaciones que afecten fuera del ámbito de trabajo.

---

## Lineamientos y Protocolo de Desarrollo
Para asegurar la correcta aplicación de TDD y el uso adecuado de los modos según la tarea, el agente debe consultar siempre los lineamientos definidos en el proyecto:
- **Protocolo de Workflow**: `.ai-guidelines/workflow.md`
- **Lineamientos TDD**: `.ai-guidelines/tdd/` (01_contexto.md, 02_reglas.md, 03_ejemplos.md)

---

## Caso de Uso: Implementación de "Listado de Comentarios"

Para implementar la funcionalidad de mostrar un listado de comentarios (entidad `Comentario`, máx 2000 caracteres), el protocolo es:

### Paso 1: Iniciación en Planning Mode
*   **Selección de Modo**: **Planning Mode** (debido a que la tarea impacta en >3 archivos).
*   **Prompt**: "Implementa la funcionalidad descrita en `.ai-guidelines/specs/listar_comentarios.md`. Debes modificar el controlador, servicio, repositorio existente y crear/actualizar la vista. **Antes de comenzar, consulta los lineamientos en '.ai-guidelines/workflow.md' y '.ai-guidelines/tdd/' para asegurar el cumplimiento del estándar TDD.**"
*   **Acción**: El agente analizará el alcance y generará un *Implementation Plan*. Revisa que este contemple el ciclo Red-Green-Refactor.

### Paso 2: Ejecución y TDD
*   **Aprobación**: Tras revisar el plan, haz clic en "Proceed".
*   **Desarrollo**: El agente ejecutará los pasos. Supervisa que para cada componente se siga el orden:
    1.  **Red**: Crear primero el test que falle.
    2.  **Green**: Implementar la lógica mínima.
    3.  **Refactor**: Limpiar y optimizar.

#### Roadmap Técnico de Implementación:
Para cumplir con `listar_comentarios.md`, el agente debe seguir este orden:

1.  **Capa de Datos (Repositorio)**:
    - `Test`: `RepositorioComentarioTest` -> `deberiaGuardarYRecuperarComentarioDeHasta2000Caracteres`.
    - `Impl`: Crear entidad `Comentario` (campo `contenido` tipo TEXT/LOB) y `RepositorioComentario`.
2.  **Capa de Negocio (Servicio)**:
    - `Test`: `ServicioComentarioTest` -> `deberiaRetornarListaDeComentariosCuandoElRepositorioDevuelveDatos`.
    - `Impl`: `ServicioComentarioImpl` inyectando el repositorio.
3.  **Capa de Presentación (Controlador - Unitario)**:
    - `Test`: `ControladorComentarioTest` -> `deberiaRetornarVistaComentariosCuandoSeAccedeAlListado`.
    - `Impl`: `ControladorComentario` llamando al servicio y devolviendo `ModelAndView`.
4.  **Capa de Presentación (Integración - Web)**:
    - `Test`: `ControladorComentarioTest` (usando `MockMvc`) -> `deberiaRenderizarTablaConComentariosEnLaVista`.
    - `Impl`: Crear/Modificar template Thymeleaf (`listado-comentarios.html`) con la lógica para iterar comentarios o mostrar el mensaje "No hay comentarios disponibles".

### Paso 3: Validación Crítica (Always Ask)
*   **Acción**: Si el agente requiere cambios críticos en base de datos o configuración, se activará la política de supervisión. Revisa y confirma la solicitud.

### Paso 4: Cierre
*   **Finalización**: Revisa el *Walkthrough* final y valida la funcionalidad en la interfaz.

---

## Cómo iniciar una nueva Feature desde cero

Para garantizar el cumplimiento de estándares, sigue estos pasos obligatorios antes de usar el panel de Antigravity:

### 1. Definición de la Especificación (Human Task)
- Crea un archivo nuevo en `.ai-guidelines/specs/` usando `template.md`.
- Define los escenarios de aceptación (BDD - Given-When-Then).

### 2. Selección del Modo (Panel IDE)
- **Planning Mode**: Para funcionalidades nuevas (alta complejidad).
- **Fast Mode**: Para tareas menores o correcciones.

### 3. El "Golden Prompt" (Iniciación en el panel)
> "Actúa como un desarrollador senior experto en este proyecto. Vamos a implementar la funcionalidad descrita en `.ai-guidelines/specs/listar_comentarios.md`.
> 
> **Antes de realizar cualquier cambio:**
> 1. Lee la especificación funcional en `.ai-guidelines/specs/listar_comentarios.md`.
> 2. Lee los lineamientos en `.ai-guidelines/workflow.md` y `.ai-guidelines/tdd/`.
> 3. Genera un plan de implementación (si estás en Planning Mode) que respete el ciclo TDD (Red-Green-Refactor).
>
> Una vez aprobado, ejecuta el desarrollo asegurando que cada componente tenga su respectivo test siguiendo los ejemplos en `.ai-guidelines/tdd/03_ejemplos.md`."

### 4. Supervisión y Validación
- **Planning Mode**: Asegúrate de que el primer paso sea siempre la creación de un test que falle (Red).
- **Ejecución**: Monitorea que el agente respete el ciclo Red-Green-Refactor.
- **Validación Final**: Ejecuta las pruebas existentes y nuevas tras finalizar.
