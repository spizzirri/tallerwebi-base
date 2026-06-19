# Contexto de Pruebas y Arquitectura (TDD)

Este documento define el alcance y el propósito de las pruebas en `tallerwebi-base` basándose en la arquitectura por capas.

## 1. Capas Arquitectónicas
El sistema se divide estrictamente para facilitar las pruebas unitarias y de integración:

- **Dominio**: Contiene la lógica de negocio, POJOs y servicios. Es independiente de frameworks externos. Las pruebas aquí son **Unitarias** y deben ser rápidas.
- **Infraestructura**: Implementación de repositorios y acceso a datos. Se prueba la integración con la base de datos (h2/sql). Son **Pruebas de Integración**.
- **Presentación**: Controladores Spring MVC. Se prueba la interacción HTTP. Son **Pruebas de Integración (Controller)**.
- **E2E (End-to-End)**: Flujos completos desde la interfaz/REST hasta la persistencia.

## 3. Ubicación de las Pruebas
Para mantener el orden, los tests se organizan en carpetas según su propósito y el nivel de integración:

- **`src/test/java/com/tallerwebi/[capa]/`**: Contiene las **Pruebas Unitarias** (POJO/Mockito) de cada capa.
    - Ejemplo: `ControladorLoginTest` en `presentacion/` para testear lógica de `ModelAndView`.
- **`src/test/java/com/tallerwebi/integracion/`**: Contiene las **Pruebas de Integración** que cargan configuraciones de Spring o Hibernate.
    - Ejemplo: `@WebMvcTest` o pruebas que usan `SpringWebTestConfig`.
- **`src/test/java/com/tallerwebi/punta_a_punta/`**: Contiene las pruebas **E2E** (Playwright/Selenium) que requieren el servidor corriendo.
- **`src/test/java/com/tallerwebi/infraestructura/`**: Aunque la infraestructura es integradora por naturaleza, si usas `@DataJpaTest` en aislamiento pueden residir aquí o en la carpeta de integración según la complejidad del setup.
