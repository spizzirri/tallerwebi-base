# Plantilla de Especificación (Spec)

Utiliza este formato para definir los requerimientos funcionales antes de iniciar cualquier desarrollo. Esto servirá de base para que el agente genere los tests (TDD).

## 1. Contexto de la Funcionalidad
- **Feature**: [Nombre]
- **Objetivo**: [Breve descripción]

## 2. Escenarios (BDD - Given-When-Then)
### Escenario: [Nombre]
- **Given**: [Contexto]
- **When**: [Acción]
- **Then**: [Resultado]

## 3. Casos Borde y Flujos Alternativos
- [Escenario]

## 4. Plan de Pruebas (Technical Mapping)
*Mapeo de pruebas según la arquitectura por capas:*

### Capa Presentación (Controlador)
- **deberia...**: [Descripción de test de controlador - Mockeando servicio]

### Capa Dominio (Servicio)
- **deberia...**: [Descripción de test unitario de lógica de negocio]

### Capa Infraestructura (Repositorio)
- **deberia...**: [Descripción de test de integración con BD]
