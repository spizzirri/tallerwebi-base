# Instrucciones para agentes de IA

Proyecto Java 25 + Spring MVC (sin Spring Boot) + Hibernate + Thymeleaf, empaquetado con Maven y servido con Jetty
en `http://localhost:8080/spring`.

## Antes de cualquier tarea
1. Leer el protocolo de trabajo: [.ai-guidelines/workflow.md](.ai-guidelines/workflow.md).
2. Si la tarea implementa una funcionalidad, leer su spec en [specs/](specs/). Sin spec aprobada no se escribe código.
3. Antes de escribir o modificar tests, leer:
   - [.ai-guidelines/tdd/01_contexto.md](.ai-guidelines/tdd/01_contexto.md): capas y ubicación de cada tipo de test.
   - [.ai-guidelines/tdd/02_reglas.md](.ai-guidelines/tdd/02_reglas.md): convenciones y stack de test.
   - [.ai-guidelines/tdd/03_ejemplos.md](.ai-guidelines/tdd/03_ejemplos.md): ejemplo por capa tomado del proyecto.

## Reglas que no se negocian
- Implementar solo lo que pide la spec: cada caso de prueba de la spec, nada más.
- Ciclo TDD por caso: test que falla (Red) → código mínimo (Green) → refactor.
- No usar Spring Boot ni sus anotaciones (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`).
- No agregar dependencias al `pom.xml` sin aprobación humana.
- No ejecutar comandos de git que modifiquen el repositorio.
- La tarea está terminada cuando `mvn verify` termina en verde.
