# Arquitectura Trimodal de IA en VS Code: Ask, Plan & Agent

> **🚀 Guía de Inicio Rápido (Workflow IA + TDD)**
> 1. **Setup:** Lee `CONTRIBUTING_TDD.md` (es la ley de calidad del proyecto).
> 2. **Ask (Validación):** Usa el panel de *Copilot Chat* para aclarar dudas.
> 3. **Plan (Arquitectura):** Usa el prompt de la **Sección 5 (Paso 2)** para generar tu `PLAN.md`.
> 4. **Agent (Ejecución):** Usa `Ctrl + I` (*Inline Chat*) para implementar el `PLAN.md` paso a paso según la **Sección 5 (Paso 3)**.

---

## 1. ¿Qué es y cómo se usa cada modo?

### Modo Ask (Consulta / Solo Lectura)
Es un chat convencional supervitaminado por el contexto de tu entorno de desarrollo. Está diseñado para responder preguntas sin alterar una sola línea de tu proyecto.

### Modo Edit (Edición Directa / Quirúrgica)
Es el "carril rápido" para cambios específicos.
*   **Cómo funciona:** Se aplica directamente sobre el archivo que estás editando o la selección de código activa.
*   **Cuándo usarlo:** Cambios puntuales (cambiar un nombre de variable, implementar un constructor, corregir un error de sintaxis).
*   **Ventaja:** Mucho más ligero en tokens que el modo Agent.

### Modo Plan (Diseño / Arquitectura - *Actividad Estratégica*)
No es una interfaz aislada, sino una **etapa obligatoria** previa a una ejecución compleja.
*   **Cómo funciona:** Antes de que la IA toque el código, le ordenas: *"Crea un plan en un archivo .md antes de realizar cualquier cambio"*.
*   **Cuándo usarlo:** Siempre que la tarea involucre múltiples archivos o riesgos de romper las reglas de calidad (Checkstyle/PMD).

### Modo Agent (Ejecución Autónoma)
Es el modo de máxima autonomía. No solo piensa el código, sino que lo ejecuta. Puede modificar múltiples componentes, abrir la terminal y autocorregirse.

---

## 2. Comparativa de Capacidades

| Característica | Modo Ask | Modo Edit | Modo Plan | Modo Agent |
| :--- | :--- | :--- | :--- | :--- |
| **Objetivo** | Informar | Editar punto | Estructurar | Ejecutar |
| **Alcance** | Proyecto (contexto) | Un archivo / selección | Proyecto (estrategia) | Proyecto (múltiples) |
| **Consumo Tokens**| Bajo | Medio | Moderado | Muy Alto |
| **Modelo Rec.** | Haiku | Haiku | Sonnet / Haiku | Sonnet / Haiku |

> **Nota sobre modelos:**
> - **Para Ask/Edit:** Haiku es ideal por velocidad y eficiencia.
> - **Para Plan/Agent:** Se recomienda **Sonnet** si está disponible, ya que requiere mayor capacidad de razonamiento lógico y coherencia estructural. Usa Haiku como alternativa si buscas máxima velocidad.

---

## 3. Guía Práctica: Adaptación para Plan Gratuito (Sin Composer)

Si no cuentas con *Copilot Composer*, puedes aplicar la arquitectura **Ask-Plan-Agent** utilizando las herramientas estándar de VS Code:

| Fase | Interfaz de VS Code | Acción Recomendada |
| :--- | :--- | :--- |
| **Ask** | Panel lateral: **Copilot Chat** | Consultar, validar viabilidad y entender el contexto. |
| **Plan** | Archivo `.md` (e.g. `PLAN.md`) | Escribir manualmente la estrategia paso a paso. |
| **Agent** | **Inline Chat** (`Ctrl + I`) | Ejecutar el plan por partes (código o archivo). |

### Instrucciones paso a paso:
1. **Ask:** Panel lateral para dudas (`pom.xml`, `CONTRIBUTING_TDD.md`).
2. **Plan:** Crear `PLAN.md` manualmente.
3. **Agent:** Abrir archivo destino, presionar `Ctrl + I` (**Inline Chat**) y pedir implementación paso a paso.

---

## 4. El Flujo de trabajo óptimo (Ask ➔ Plan ➔ Agent)

Al obligar a la IA a escribir un plan antes de tocar el código, reduces drásticamente la tasa de fallos.

### Enfoque progresivo:
1. **[Modo Ask]**: Valida la viabilidad.
2. **[Modo Plan]**: Genera el Markdown y cuestiona la lógica.
3. **[Modo Agent]**: Ejecuta el plan paso a paso.

---

## 5. Ejemplo Práctico: Implementación aplicando TDD y Lineamientos (tallerwebi-base)

**Objetivo:** Desarrollar la funcionalidad de crear un comentario.
**Estructura del objetivo final:**
1. Entidad `Comentario`.
2. `RepositorioComentario` (Interfaz + Test + Impl).
3. `ServicioComentario` (Interfaz + Test + Impl).
4. `ControladorComentario` (Test + Impl).
5. Vista `comentario.html`.

**Sigue este proceso paso a paso para completar el objetivo:**


### Paso 1: [Modo Ask] - Validación y Contexto
*   **Prompt (Panel Copilot Chat):** "Analiza `CONTRIBUTING_TDD.md`. Necesito implementar una funcionalidad para guardar comentarios con un maximo de 2000 caracteres. ¿Dónde debo ubicar la entidad, el repositorio, el servicio y el controlador? ¿Qué convenciones de nombres y de tests debo usar para cumplir con los estándares del proyecto (Checkstyle/PMD)?"

### Paso 2: [Modo Plan] - Arquitectura (Actividad Estratégica)
*   **Acción:** Copia este prompt en **Copilot Chat** (Modelo: **Claude 3.5 Haiku**), reemplazando `[Funcionalidad]` por lo que desees desarrollar:
    *   **Prompt:** "Actúa como Arquitecto de Software Senior en un proyecto Java Spring MVC. Redacta un archivo `PLAN.md` estructurado como checklist para implementar la funcionalidad de agregar comentario.
        REQUISITOS DEL PLAN:
        1. Orden de desarrollo TDD: Entidad -> Repositorio (test/impl) -> Servicio (test/impl) -> Controlador (test/impl) -> Vista (Thymeleaf).
        2. Especificación de Tests: Para CADA componente, lista explícitamente:
           - Casos de Éxito: crear comentario con 500 caracteres. crear comentario con 2000 caracteres (maximo permitido).
           - Casos de Error/Borde: comentario vacio. comentario con espacios en blanco. comentario con mas de 5000 caracteres.
        3. Calidad: El plan debe garantizar el cumplimiento de los lineamientos en `CONTRIBUTING_TDD.md` (JaCoCo 80%, Checkstyle, PMD).
        4. Vista: Incluir la validación del mensaje de éxito en la UI.
        El plan debe ser accionable y listo para ser ejecutado paso a paso."
*   **Resultado:** Copia la respuesta en un archivo `PLAN.md` en la raíz.


### Paso 3: [Modo Agent - Ejecución] (Master Prompt con Inline Chat `Ctrl+I`)
*   **Acción:** En lugar de darle pasos pequeños, usa este **Master Prompt** que invoca la `Regla de Oro` de `CONTRIBUTING_TDD.md`:
    *   **Prompt (en Inline Chat):** "Implementa [Componente] aplicando la **Regla de Oro: Ciclo TDD Completo** descrita en `CONTRIBUTING_TDD.md`.
        1. Genera primero el test fallido (Red).
        2. Luego genera el código que lo hace pasar (Green).
        3. Aplica refactor y asegura que cumpla Checkstyle/PMD.
        Proporcióname el código en bloques para aplicar (`Aceptar Diff`) de forma secuencial."
*   **Resultado:** La IA te guiará y generará los bloques de código en el orden lógico del ciclo TDD.

