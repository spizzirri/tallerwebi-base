# Protocolo de Desarrollo y Workflow

Este documento define el estándar para desarrollar funcionalidades en `tallerwebi-base` utilizando Antigravity IDE. Todo desarrollo debe seguir el modelo **TDD (Red-Green-Refactor)**.

## 1. Modelo de Desarrollo: TDD (Ciclo de trabajo)
El ciclo de trabajo es innegociable y debe ser respetado en todas las tareas:
- **Red**: Crear test que falle.
- **Green**: Escribir código mínimo que haga pasar el test.
- **Refactor**: Mejorar código sin cambiar comportamiento.

## 2. Selector de Modo según Tarea

| Complejidad / Riesgo | Modo a utilizar | Justificación |
| :--- | :--- | :--- |
| **Alta (>3 archivos, Arquitectura)** | **Modo Plan** | Requiere aprobación humana del diseño y pasos previos. |
| **Baja / Rutina** | **Modo Agente** | Autonomía para prototipado rápido y corrección de bugs. |
| **Crítica (Configuración, DB, Security)** | **Modo Ask** | Requiere supervisión humana obligatoria antes de ejecutar cambios. |

## 3. Flujo de Trabajo para Nuevas Funcionalidades
1. **Definición de Especificaciones (Specs)**: Crear un archivo en `.ai-guidelines/specs/` usando la plantilla `template.md` definiendo los criterios de aceptación (Given-When-Then).
2. **Selección de Modo**: Identificar si la tarea es de alta complejidad (Plan), rutinaria (Agente) o crítica (Ask).
3. **Setup de Contexto**: Instruir al agente obligatoriamente: *"Consulta los lineamientos en '.ai-guidelines/tdd/' y la especificación funcional en '.ai-guidelines/specs/[TU_SPEC].md' para asegurar el cumplimiento del estándar y requerimientos"*.
4. **Ejecución**: Aplicar ciclo Red-Green-Refactor basándose exclusivamente en los casos definidos en la especificación.
